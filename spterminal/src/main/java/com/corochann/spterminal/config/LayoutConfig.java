package com.corochann.spterminal.config;

import com.corochann.spterminal.util.MyUtils;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Layout configuration, this is preference class.
 * preference class owns values in Attributes, which can be stored/loaded in XML format.
 */
public class LayoutConfig implements Serializable {
    /*--- Attributes ---*/
    /** Automatically update each layout size once user changes the actual size in application */
    private boolean autoUpdate = true;


    private int frameWidth = 1080;
    private int frameHeight = 980;

    private int terminalSplitPaneLocation = 700; // See TerminalPanel initial value
    private int rightVerticalSplitPaneLocation = 600; // See TerminalPanel initial value

    /*--- CONSTANTS ---*/

    /*--- Save & Load ---*/
    /**
     * save this preference
     */
    public synchronized void save() {
        System.out.println("Saving LayoutConfig to " + ProjectConfig.LAYOUT_CONFIG_XML_PATH);
        try {
            MyUtils.writeToXML(this, ProjectConfig.LAYOUT_CONFIG_XML_PATH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * load this preference
     */
    public static synchronized LayoutConfig load() {
        LayoutConfig layoutConfig;
        try {
            layoutConfig = (LayoutConfig) MyUtils.readFromXML(ProjectConfig.LAYOUT_CONFIG_XML_PATH);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println(ProjectConfig.LAYOUT_CONFIG_XML_PATH + " not found, default value will be used.");
            layoutConfig = new LayoutConfig();
        }
        return layoutConfig;
    }

    /*--- Getter and Setter ---*/

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    public int getRightVerticalSplitPaneLocation() {
        return rightVerticalSplitPaneLocation;
    }

    public void setRightVerticalSplitPaneLocation(int rightVerticalSplitPaneLocation) {
        this.rightVerticalSplitPaneLocation = rightVerticalSplitPaneLocation;
    }

    public int getTerminalSplitPaneLocation() {
        return terminalSplitPaneLocation;
    }

    public void setTerminalSplitPaneLocation(int terminalSplitPaneLocation) {
        this.terminalSplitPaneLocation = terminalSplitPaneLocation;
    }

    /*--- util ---*/

}
