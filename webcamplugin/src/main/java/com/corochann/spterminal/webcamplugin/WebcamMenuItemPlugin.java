package com.corochann.spterminal.webcamplugin;

import com.corochann.spterminal.plugin.MenuItemPlugin;

import java.awt.event.ActionEvent;

/**
 * Webcam plugin to support play & record feature
 */
public class WebcamMenuItemPlugin implements MenuItemPlugin {
    @Override
    public String getText() {
        return "Webcam";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("plugin called!");
        new WebcamRecoderFrame().showFrame();
    }
}
