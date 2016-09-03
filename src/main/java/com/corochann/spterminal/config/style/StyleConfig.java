package com.corochann.spterminal.config.style;

import com.corochann.spterminal.config.ProjectConfig;
import com.corochann.spterminal.util.MyUtils;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.Serializable;

/**
 * Style configuration, this is model for {@link StyleConfig}
 * {@link StyleConfig} decides the following UI attribute
 * - Color
 * - Font
 */
public class StyleConfig implements Serializable {

    /*--- CONSTANTS ---*/
    public static final String STYLE_DEFAULT = "default";
    public static final String STYLE_DARK = "dark";

    public static final String[] STYLE_LIST = {
            STYLE_DEFAULT,
            STYLE_DARK
    };

    /*--- Attributes ---*/
    // right side value is considered as "default" value.
    // null value means use default Look and Feel.

    /* General */
    private Color baseForeGroundColor = null; // Background color
    private Color baseBackGroundColor = null; // Text color
    private Font baseFont = null;  // Font for terminal
    private Font terminalFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);  // Font for terminal

    /* SPTerminal */

    /* TerminalPanel */
    private Color lineHighlightColor = new Color(51, 153, 255, 30);
    private Color charHighlightColor = Color.magenta;


    /*--- Constructor ---*/


    /*--- Save & Load ---*/
    /**
     * save this preference
     */
    public synchronized void save(String styleConfigName) {
        String path = ProjectConfig.STYLE_CONFIG_PATH_PREFIX + styleConfigName + ".xml";
        System.out.println("Saving "+ styleConfigName + " StyleConfig to " + path);
        try {
            MyUtils.writeToXML(this, path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * load this preference
     */
    public static synchronized StyleConfig load(String styleConfigName) {
        String path = ProjectConfig.STYLE_CONFIG_PATH_PREFIX + styleConfigName + ".xml";
        StyleConfig styleConfig;
        try {
            styleConfig = (StyleConfig)MyUtils.readFromXML(path);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println(path + " not found, default value will be used.");
            styleConfig = new StyleConfig();
        }
        return styleConfig;
    }

    /*--- Getter & Setter ---*/
    public Color getBaseForeGroundColor() {
        return baseForeGroundColor;
    }

    public void setBaseForeGroundColor(Color baseForeGroundColor) {
        this.baseForeGroundColor = baseForeGroundColor;
    }

    public Color getBaseBackGroundColor() {
        return baseBackGroundColor;
    }

    public void setBaseBackGroundColor(Color baseBackGroundColor) {
        this.baseBackGroundColor = baseBackGroundColor;
    }

    public Font getBaseFont() {
        return baseFont;
    }

    public void setBaseFont(Font baseFont) {
        this.baseFont = baseFont;
    }

    public Font getTerminalFont() {
        return terminalFont;
    }

    public void setTerminalFont(Font terminalFont) {
        this.terminalFont = terminalFont;
    }

    public Color getLineHighlightColor() {
        return lineHighlightColor;
    }

    public void setLineHighlightColor(Color lineHighlightColor) {
        this.lineHighlightColor = lineHighlightColor;
    }

    public Color getCharHighlightColor() {
        return charHighlightColor;
    }

    public void setCharHighlightColor(Color charHighlightColor) {
        this.charHighlightColor = charHighlightColor;
    }


    public static void main(String[] args) {
        final Color COLOR_DARKGREY = Color.decode("#2B2B2B");
        final Color COLOR_WHITE = Color.decode("#A9B7C6");
        final Color COLOR_ORANGE = Color.decode("#CC7832");
        final Color COLOR_PURPLE = Color.decode("#9876AA");

        MyUtils.prepareDir(ProjectConfig.STYLE_CONFIG_PATH_PREFIX);

        /*--- 1. default style ---*/
        StyleConfig defaultStyleConfig = new StyleConfig();
        defaultStyleConfig.save(STYLE_DEFAULT);

        /*--- 2. dark style ---*/
        StyleConfig darkStyleConfig = new StyleConfig();

        darkStyleConfig.setBaseForeGroundColor(COLOR_WHITE);
        darkStyleConfig.setBaseBackGroundColor(COLOR_DARKGREY);
        //darkStyleConfig.setBaseFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); // <-- What is the best font?
        darkStyleConfig.setTerminalFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); // same as default
        darkStyleConfig.setLineHighlightColor(new Color(51, 153, 255, 50));
        darkStyleConfig.setCharHighlightColor(COLOR_PURPLE);
        darkStyleConfig.save(STYLE_DARK);
    }
}
