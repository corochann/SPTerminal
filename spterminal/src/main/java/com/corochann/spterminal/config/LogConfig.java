package com.corochann.spterminal.config;

import com.corochann.spterminal.util.MyUtils;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Log configuration, this is preference class.
 * preference class owns values in Attributes, which can be stored/loaded in XML format.
 */
public class LogConfig implements Serializable {
    /*--- Attributes ---*/
    private boolean autoLogging = false;
    private String autoLogFileName = "'&h'_yyyyMMdd_HHmmss.'log'";

    /*--- CONSTANTS ---*/

    /*--- Save & Load ---*/
    /**
     * save this preference
     */
    public synchronized void save() {
        System.out.println("Saving LogConfig to " + ProjectConfig.LOG_CONFIG_XML_PATH);
        try {
            MyUtils.writeToXML(this, ProjectConfig.LOG_CONFIG_XML_PATH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * load this preference
     */
    public static synchronized LogConfig load() {
        LogConfig logConfig;
        try {
            logConfig = (LogConfig) MyUtils.readFromXML(ProjectConfig.LOG_CONFIG_XML_PATH);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println(ProjectConfig.LOG_CONFIG_XML_PATH + " not found, default value will be used.");
            logConfig = new LogConfig();
        }
        return logConfig;
    }

    /*--- Getter and Setter ---*/
    public boolean isAutoLogging() {
        return autoLogging;
    }

    public void setAutoLogging(boolean autoLogging) {
        this.autoLogging = autoLogging;
    }

    public String getAutoLogFileName() {
        return autoLogFileName;
    }

    public void setAutoLogFileName(String autoLogFileName) {
        this.autoLogFileName = autoLogFileName;
    }

    /*--- util ---*/
    public String constructAutoLogFilePath(String prefix, String portName) {
        SimpleDateFormat sdf = new SimpleDateFormat(autoLogFileName);
        Calendar c = Calendar.getInstance();
        String actualLogFileName = sdf.format(c.getTime()).replace("&h", portName);
        String logFilePath = ProjectConfig.LOG_FOLDER + prefix + actualLogFileName;
        return logFilePath;
    }
}
