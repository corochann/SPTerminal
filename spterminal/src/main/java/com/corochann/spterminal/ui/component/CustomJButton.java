package com.corochann.spterminal.ui.component;

import com.corochann.spterminal.config.style.StyleConfig;

import javax.swing.*;

/**
 * Customized JButton, according to StyleConfig
 * styleConfig can be null if no style setup is necessary
 */
public class CustomJButton extends JButton {

    public CustomJButton(StyleConfig styleConfig) {
        super();
        Initialize(styleConfig);
    }

    public CustomJButton(String text, StyleConfig styleConfig) {
        super(text);
        Initialize(styleConfig);
    }

    private void Initialize(StyleConfig styleConfig) {
        //this.setContentAreaFilled(false);
        //this.setOpaque(true);
        if (styleConfig != null) {
            if (styleConfig.getBaseBackGroundColor() != null) {
                this.setBackground(styleConfig.getBaseBackGroundColor());
            }
            if (styleConfig.getBaseForeGroundColor() != null) {
                this.setForeground(styleConfig.getBaseForeGroundColor());
            }
        }
    }
}
