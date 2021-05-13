/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.astmlightmode;

import com.mirth.connect.model.transmission.StreamHandlerException;

public class ASTMLightStreamHandlerException extends StreamHandlerException {

    public ASTMLightStreamHandlerException() {
        super();
    }

    public ASTMLightStreamHandlerException(String message) {
        super(message);
    }

    public ASTMLightStreamHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ASTMLightStreamHandlerException(Throwable cause) {
        super(cause);
    }
}
