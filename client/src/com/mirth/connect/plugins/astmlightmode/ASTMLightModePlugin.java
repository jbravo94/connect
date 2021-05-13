/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.astmlightmode;

import com.mirth.connect.plugins.TransmissionModeClientProvider;
import com.mirth.connect.plugins.TransmissionModePlugin;

public class ASTMLightModePlugin extends TransmissionModePlugin {

    public ASTMLightModePlugin(String pluginName) {
        super(pluginName);
    }

    @Override
    public String getPluginPointName() {
        return ASTMLightModeProperties.PLUGIN_POINT;
    }

    @Override
    public TransmissionModeClientProvider createProvider() {
        return new ASTMLightModeClientProvider();
    }
}