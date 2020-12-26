/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.astmlightmode;

import com.mirth.connect.donkey.server.message.batch.BatchStreamReader;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.framemode.FrameStreamHandler;
import com.mirth.connect.model.transmission.framemode.FrameStreamHandlerException;
import com.mirth.connect.util.TcpUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.util.Arrays;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ASTMLightv2StreamHandler extends FrameStreamHandler {

    private Logger logger = Logger.getLogger(this.getClass());

    private ByteArrayOutputStream capturedBytes; // The bytes captured so far by the reader, not including any in the end bytes buffer.
    private List<Byte> endBytesBuffer; // An interim buffer of bytes used to capture the ending byte sequence.
    private byte lastByte; // The last byte returned from getNextByte.
    private boolean streamDone; // This is true if an EOF has been read in, or if the ending byte sequence has been detected.

    private boolean checkStartOfMessageBytes;
    private int currentByte;

    private byte[] ackBytes;
    private byte[] nackBytes;
    private int maxRetries;
    private boolean committed;

    public ASTMLightv2StreamHandler(InputStream inputStream, OutputStream outputStream, BatchStreamReader batchStreamReader, TransmissionModeProperties transmissionModeProperties) {
        super(inputStream, outputStream, batchStreamReader, transmissionModeProperties);
        ASTMLightModeProperties props = (ASTMLightModeProperties) transmissionModeProperties;
        ackBytes = TcpUtil.stringToByteArray(props.getAckBytes());
        nackBytes = TcpUtil.stringToByteArray(props.getNackBytes());
        maxRetries = NumberUtils.toInt(props.getMaxRetries());
        committed = false;

        this.checkStartOfMessageBytes = true;
        this.streamDone = false;
    }

    @Override
    public void commit(boolean success) throws IOException {
        // In case batching is occurring, we only want to send the acknowledgement back once
        if (!committed) {
            // Write either an ACK or NACK depending on whether the engine was able to commit the data to memory
            super.write(success ? ackBytes : nackBytes);
            committed = true;
        }
    }

    @Override
    public void write(byte[] data) throws IOException {
        boolean done = false;
        Exception firstCause = null;
        int retryCount = 0;

        while (!done) {
            // Write the data as per normal MLLPv1
            super.write(data);

            try {
                // Attempt to retrieve an acknowledgement
                byte[] response = super.read();
                // Reset the streamDone flag so the input stream can be read from again
                reset();

                if (response == null) {
                    /*
                     * Nothing was captured but an exception also was not thrown, so assume this is
                     * due to a previous read encountering an EOF and marking the stream as done.
                     * Reset the FrameStreamHandler's flags and try once more.
                     */
                    response = super.read();
                }

                if (Arrays.areEqual(response, ackBytes)) {
                    done = true;
                } else if (Arrays.areEqual(response, nackBytes)) {
                    throw new ASTMLightv2StreamHandlerException("Negative commit acknowledgement received.");
                } else {
                    throw new ASTMLightv2StreamHandlerException("Invalid acknowledgement block received.");
                }
            } catch (IOException e) {
                if (firstCause == null) {
                    firstCause = e;
                }

                if (maxRetries > 0 && retryCount++ == maxRetries) {
                    throw new ASTMLightv2StreamHandlerException("Maximum retry count reached. First cause: " + firstCause.getMessage(), firstCause);
                }
            }
        }
    }

    @Override
    public byte[] read() throws IOException {
        if (streamDone || inputStream == null) {
            return null;
        }

        capturedBytes = new ByteArrayOutputStream();
        List<Byte> firstBytes = new ArrayList<Byte>();
        // A List is used here to allow the buffer to simulate a "shifting window" of potential bytes.
        endBytesBuffer = new ArrayList<Byte>();

        try {
            // Skip to the beginning of the message
            if (checkStartOfMessageBytes) {
                int i = 0;

                while (i < startOfMessageBytes.length) {
                    currentByte = inputStream.read();
                    logger.trace("Checking for start of message bytes, currentByte: " + currentByte);

                    if (currentByte != -1) {
                        if (firstBytes.size() < startOfMessageBytes.length) {
                            firstBytes.add((byte) currentByte);
                        }

                        if (currentByte == (int) (startOfMessageBytes[i] & 0xFF)) {
                            i++;
                        } else {
                            i = 0;
                        }
                    } else {
                        streamDone = true;
                        if (firstBytes.size() > 0) {
                            throw new FrameStreamHandlerException(true, startOfMessageBytes, ArrayUtils.toPrimitive(firstBytes.toArray(new Byte[0])));
                        } else {
                            // The input stream ended before the begin bytes were detected, so return null
                            return null;
                        }
                    }
                }

                // Begin bytes were found
                checkStartOfMessageBytes = false;
            }

            // Allow the handler to initialize anything it needs to (e.g. mark the input stream)
            batchStreamReader.initialize();

            // Iterate while there are still bytes to read, or if we're checking for end bytes and its buffer is not empty
            while ((currentByte = batchStreamReader.getNextByte()) != -1 || (endOfMessageBytes.length > 0 && !endBytesBuffer.isEmpty())) {
                // If the input stream is done, get the byte from the buffer instead
                if (currentByte == -1) {
                    currentByte = endBytesBuffer.remove(0);
                    streamDone = true;
                } else {
                    lastByte = (byte) currentByte;
                }

                // Check to see if an end frame has been received
                if (endOfMessageBytes.length > 0 && !streamDone) {
                    if (endBytesBuffer.size() == endOfMessageBytes.length) {
                        // Shift the buffer window over one, popping the first element and writing it to the output stream
                        capturedBytes.write(endBytesBuffer.remove(0));
                    }

                    // Add the byte to the buffer
                    endBytesBuffer.add((byte) currentByte);

                    // Check to see if the current buffer window equals the ending byte sequence
                    boolean endBytesFound = true;
                    for (int i = 0; i <= endBytesBuffer.size() - 1; i++) {
                        if (endBytesBuffer.get(i) != endOfMessageBytes[i]) {
                            endBytesFound = false;
                            break;
                        }
                    }

                    if (endBytesFound) {
                        // Ending bytes sequence has been detected
                        streamDone = true;
                        return capturedBytes.toByteArray();
                    }
                } else {
                    // Add the byte to the main output stream
                    capturedBytes.write(currentByte);
                }

                if (!streamDone) {
                    // Allow subclass to check the current byte stream and return immediately
                    byte[] returnBytes = batchStreamReader.checkForIntermediateMessage(capturedBytes, endBytesBuffer, lastByte);
                    if (returnBytes != null) {
                        return returnBytes;
                    }
                }
            }
        } catch (Throwable e) {
            if (!returnDataOnException) {
                if (e instanceof IOException) {
                    // If an IOException occurred and we're not allowing data to return, throw the exception

                    if (checkStartOfMessageBytes && firstBytes.size() > 0) {
                        // At least some bytes have been read, but the start of message bytes were not detected
                        throw new FrameStreamHandlerException(true, startOfMessageBytes, ArrayUtils.toPrimitive(firstBytes.toArray(new Byte[0])), e);
                    }
                    if (capturedBytes.size() + endBytesBuffer.size() > 0 && endOfMessageBytes.length > 0) {
                        // At least some bytes have been captured, but the end of message bytes were not detected
                        throw new FrameStreamHandlerException(false, endOfMessageBytes, getLastBytes(), e);
                    }
                    throw (IOException) e;
                } else {
                    // If any other Throwable was caught, return null to indicate that we're done
                    return null;
                }
            }
        }

        if (endOfMessageBytes.length > 0) {
            // If we got here, then the end of message bytes were not captured
            throw new FrameStreamHandlerException(false, endOfMessageBytes, getLastBytes());
        } else {
            /*
             * If we got here, no end of message bytes were expected, but we should reset the check
             * flag so that the next time a read is performed, it will attempt to capture the
             * starting bytes again.
             */
            checkStartOfMessageBytes = true;
        }

        // Flush the buffer to the main output stream
        for (Byte bufByte : endBytesBuffer) {
            capturedBytes.write(bufByte);
        }

        return capturedBytes.size() > 0 ? capturedBytes.toByteArray() : null;
    }

    private byte[] getLastBytes() {
        int capturedBytesLength = capturedBytes != null ? capturedBytes.size() : 0;
        int endBytesBufferLength = endBytesBuffer != null ? endBytesBuffer.size() : 0;
        // If the total bytes read is less than the number of expected end bytes, use the smaller value
        byte[] lastBytes = new byte[Math.min(capturedBytesLength + endBytesBufferLength, endOfMessageBytes.length)];
        int index = 0;

        // Add any captured bytes, leaving room for the end bytes buffer
        if (capturedBytes != null) {
            byte[] capturedByteArray = capturedBytes.toByteArray();

            for (int i = capturedBytesLength - lastBytes.length + endBytesBufferLength; i >= 0 && i < capturedBytesLength; i++) {
                lastBytes[index++] = capturedByteArray[i];
            }
        }

        // Fill the remainder of the array with the end bytes buffer
        if (endBytesBuffer != null) {
            for (byte b : endBytesBuffer) {
                lastBytes[index++] = b;
            }
        }

        return lastBytes;
    }
}
