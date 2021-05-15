/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.astmlightmode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.framemode.FrameModeProperties;
import com.mirth.connect.plugins.FrameTransmissionModeClientProvider;

public class ASTMLightModeClientProvider extends FrameTransmissionModeClientProvider {

    static final String CHANGE_START_BYTES_COMMAND = "changeStartBytes";
    static final String CHANGE_END_BYTES_COMMAND = "changeEndBytes";

    protected ASTMLightModeSettingsPanel settingsPanel;
    private ASTMLightModeProperties ASTMLightModeProperties;

    @Override
    public void initialize(ActionListener actionListener) {
        super.initialize(actionListener);
        settingsPanel = new ASTMLightModeSettingsPanel(this);
        super.settingsPanel.switchComponent(settingsPanel);
        setProperties(new ASTMLightModeProperties());
    }

    @Override
    public TransmissionModeProperties getProperties() {
        FrameModeProperties frameModeProperties = (FrameModeProperties) super.getProperties();
        ASTMLightModeProperties.setStartOfMessageBytes(frameModeProperties.getStartOfMessageBytes());
        ASTMLightModeProperties.setEndOfMessageBytes(frameModeProperties.getEndOfMessageBytes());
        return ASTMLightModeProperties;
    }

    @Override
    public TransmissionModeProperties getDefaultProperties() {
        return new ASTMLightModeProperties();
    }

    @Override
    public void setProperties(TransmissionModeProperties properties) {
        super.setProperties(properties);

        if (properties instanceof ASTMLightModeProperties) {
            ASTMLightModeProperties = (ASTMLightModeProperties) properties;
        } else {
            ASTMLightModeProperties = new ASTMLightModeProperties();
            FrameModeProperties frameModeProperties = (FrameModeProperties) properties;
            ASTMLightModeProperties.setStartOfMessageBytes(frameModeProperties.getStartOfMessageBytes());
            ASTMLightModeProperties.setEndOfMessageBytes(frameModeProperties.getEndOfMessageBytes());
        }

        changeSampleValue();
    }

    @Override
    public JComponent getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public String getSampleLabel() {
        return "ASTM Sample Frame:";
    }

    @Override
    public String getSampleValue() {
        return ("<html><b><i>Not Available</i></b></html>").trim();
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().equals(CHANGE_START_BYTES_COMMAND)) {
            super.settingsPanel.startOfMessageBytesField.setText(((JTextField) evt.getSource()).getText());
        } else if (evt.getActionCommand().equals(CHANGE_END_BYTES_COMMAND)) {
            super.settingsPanel.endOfMessageBytesField.setText(((JTextField) evt.getSource()).getText());
        } else {
            ASTMLightModeSettingsDialog settingsDialog = new ASTMLightModeSettingsDialog(this);
            settingsDialog.setProperties(ASTMLightModeProperties);
            settingsDialog.setVisible(true);

            if (settingsDialog.isSaved()) {
                setProperties(settingsDialog.getProperties());
            } else {
                setProperties(ASTMLightModeProperties);
            }
        }
    }
}