/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.astmlight;

import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.converters.PluginPropertiesConverter;
import com.mirth.connect.plugins.ServicePlugin;
import com.thoughtworks.xstream.XStream;

import java.util.Map;
import java.util.Properties;

public class AstmLightServicePlugin implements ServicePlugin {

    @Override
    public String getPluginPointName() {
        return "ASTM Light Service Plugin";
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void init(Properties properties) {
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        XStream xstream = serializer.getXStream();
        xstream.registerLocalConverter(AstmLightReceiverProperties.class, "responseConnectorPluginProperties", new PluginPropertiesConverter(serializer.getNormalizedVersion(), xstream.getMapper()));
    }

    @Override
    public void update(Properties properties) {}

    @Override
    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        return null;
    }
    
    @Override
    public Map<String, Object> getObjectsForSwaggerExamples() {
    	// TODO Auto-generated method stub
    	return null;
    }
}