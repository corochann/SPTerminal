package com.corochann.spterminal.ui.component;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

/**
 * Customized JFormattedTextField
 * It has fixed height (not increase the height).
 */
public class CustomJFormattedTextField extends JFormattedTextField {
    private int fixedHeight;

    public CustomJFormattedTextField(String text) {
        super(text);
        fixedHeight = this.getPreferredSize().height;
        Initialize();
    }

    public CustomJFormattedTextField(NumberFormat nf) {
        super(nf);
        fixedHeight = this.getPreferredSize().height;
        Initialize();
    }

    public CustomJFormattedTextField() {
        super();
        fixedHeight = this.getPreferredSize().height;
        Initialize();
    }

    private void Initialize() {
        // TextField is expected to input one line text,
        // set maximum height to prevent height increasing.
        this.setMaximumSize(new Dimension(
                this.getMaximumSize().width,
                fixedHeight
        )); // keep width same, and set maximum height as current height
    }

    public void setMinimumWidth(int width) {
        setMinimumSize(new Dimension(width, fixedHeight));
    }

    public void setMaximumWidth(int width) {
        setMaximumSize(new Dimension(width, fixedHeight));
    }

    public void setPreferredWidth(int width) {
        setPreferredSize(new Dimension(width, fixedHeight));
    }
}
