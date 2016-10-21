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

    /* ANSI escape SGR (Select Graphic Rendition) Color.
     * Refer https://en.wikipedia.org/wiki/ANSI_escape_code#Colors for details
     */
    private Color ansiBlack = new Color(0, 0, 0);             // n=0, intensity=normal
    private Color ansiRed = new Color(205, 0, 0);             // n=1, intensity=normal
    private Color ansiGreen = new Color(0, 205, 0);           // n=2, intensity=normal
    private Color ansiYellow = new Color(205, 205, 0);        // n=3, intensity=normal
    private Color ansiBlue = new Color(0, 0, 238);            // n=4, intensity=normal
    private Color ansiMagenta = new Color(205, 0, 205);       // n=5, intensity=normal
    private Color ansiCyan = new Color(0, 205, 205);          // n=6, intensity=normal
    private Color ansiWhite = new Color(229, 229, 229);       // n=7, intensity=normal
    private Color ansiBrightBlack = new Color(127, 127, 127); // n=0, intensity=bright
    private Color ansiBrightRed = new Color(255, 0, 0);       // n=1, intensity=bright
    private Color ansiBrightGreen = new Color(0, 255, 0);     // n=2, intensity=bright
    private Color ansiBrightYellow = new Color(255, 255, 0);  // n=3, intensity=bright
    private Color ansiBrightBlue = new Color(0, 0, 252);      // n=4, intensity=bright
    private Color ansiBrightMagenta = new Color(255, 0, 255); // n=5, intensity=bright
    private Color ansiBrightCyan = new Color(0, 255, 255);    // n=6, intensity=bright
    private Color ansiBrightWhite = new Color(255, 255, 255); // n=7, intensity=bright

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

    public Color getAnsiBrightWhite() {
        return ansiBrightWhite;
    }

    public void setAnsiBrightWhite(Color ansiBrightWhite) {
        this.ansiBrightWhite = ansiBrightWhite;
    }

    public Color getAnsiBrightCyan() {
        return ansiBrightCyan;
    }

    public void setAnsiBrightCyan(Color ansiBrightCyan) {
        this.ansiBrightCyan = ansiBrightCyan;
    }

    public Color getAnsiBrightMagenta() {
        return ansiBrightMagenta;
    }

    public void setAnsiBrightMagenta(Color ansiBrightMagenta) {
        this.ansiBrightMagenta = ansiBrightMagenta;
    }

    public Color getAnsiBrightBlue() {
        return ansiBrightBlue;
    }

    public void setAnsiBrightBlue(Color ansiBrightBlue) {
        this.ansiBrightBlue = ansiBrightBlue;
    }

    public Color getAnsiBrightYellow() {
        return ansiBrightYellow;
    }

    public void setAnsiBrightYellow(Color ansiBrightYellow) {
        this.ansiBrightYellow = ansiBrightYellow;
    }

    public Color getAnsiBrightGreen() {
        return ansiBrightGreen;
    }

    public void setAnsiBrightGreen(Color ansiBrightGreen) {
        this.ansiBrightGreen = ansiBrightGreen;
    }

    public Color getAnsiBrightRed() {
        return ansiBrightRed;
    }

    public void setAnsiBrightRed(Color ansiBrightRed) {
        this.ansiBrightRed = ansiBrightRed;
    }

    public Color getAnsiBrightBlack() {
        return ansiBrightBlack;
    }

    public void setAnsiBrightBlack(Color ansiBrightBlack) {
        this.ansiBrightBlack = ansiBrightBlack;
    }

    public Color getAnsiWhite() {
        return ansiWhite;
    }

    public void setAnsiWhite(Color ansiWhite) {
        this.ansiWhite = ansiWhite;
    }

    public Color getAnsiCyan() {
        return ansiCyan;
    }

    public void setAnsiCyan(Color ansiCyan) {
        this.ansiCyan = ansiCyan;
    }

    public Color getAnsiMagenta() {
        return ansiMagenta;
    }

    public void setAnsiMagenta(Color ansiMagenta) {
        this.ansiMagenta = ansiMagenta;
    }

    public Color getAnsiBlue() {
        return ansiBlue;
    }

    public void setAnsiBlue(Color ansiBlue) {
        this.ansiBlue = ansiBlue;
    }

    public Color getAnsiYellow() {
        return ansiYellow;
    }

    public void setAnsiYellow(Color ansiYellow) {
        this.ansiYellow = ansiYellow;
    }

    public Color getAnsiGreen() {
        return ansiGreen;
    }

    public void setAnsiGreen(Color ansiGreen) {
        this.ansiGreen = ansiGreen;
    }

    public Color getAnsiRed() {
        return ansiRed;
    }

    public void setAnsiRed(Color ansiRed) {
        this.ansiRed = ansiRed;
    }

    public Color getAnsiBlack() {
        return ansiBlack;
    }

    public void setAnsiBlack(Color ansiBlack) {
        this.ansiBlack = ansiBlack;
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
