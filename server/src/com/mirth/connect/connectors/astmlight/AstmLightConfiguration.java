/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.astmlight;

import com.mirth.connect.donkey.server.channel.Connector;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public interface AstmLightConfiguration {

    public void configureConnectorDeploy(Connector connector) throws Exception;

    public ServerSocket createServerSocket(int port, int backlog) throws IOException;

    public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException;

    public Socket createSocket() throws IOException;

    public Socket createResponseSocket() throws IOException;

    public Map<String, Object> getSocketInformation(Socket socket);
}