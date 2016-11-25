package com.corochann.spterminal.webcamplugin.data.model;

import com.corochann.spterminal.util.MyUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;

/**
 * Model class for synchronized video & log recording.
 */
public class VideoLogRecord implements Serializable {
    /*--- Attributes ---*/
    private String videoFilename = "";
    private long videoStartTime = 0L;
    private String logFilename = "";
    private transient String parentFilePath = "";


    /*--- Constructor ---*/
    public VideoLogRecord() {

    }

    /*--- Save & Load ---*/
    /**
     * save this preference
     */
    public synchronized void save(String parentDirPath) throws FormatErrorException {
        /* Error check - abbreviation and command must contain String */
        if (videoFilename == null || videoFilename.length() == 0) {
            System.out.println("[Error] videoFileName is null or length is 0");
            throw new FormatErrorException();
        }
        if (logFilename == null || logFilename.length() == 0) {
            System.out.println("[Error] logFilename is null or length is 0");
            throw new FormatErrorException();
        }
        if (videoStartTime <= 0L) {
            System.out.println("[Error] videoStartTime is not positive value: " + videoStartTime);
            throw new FormatErrorException();
        }
        MyUtils.prepareDir(parentDirPath);
        String path = constructPath(parentDirPath);
        System.out.println("Saving VideoLogRecord to " + path);
        try {
            MyUtils.writeToXML(this, path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * load this preference
     * @param path path of record_config.xml file
     * @return
     */
    public static synchronized VideoLogRecord load(String path) {
        //String path = constructPath(parentDirPath);
        VideoLogRecord record;
        try {
            System.out.println("VideoLogRecord load " + path);
            Object obj = MyUtils.readFromXML(path, VideoLogRecord.class);
            System.out.println("casting...");
            record = (VideoLogRecord) obj;
            System.out.println("VideoLogRecord record = " + record);
            record.parentFilePath = new File(path).getParentFile().getAbsolutePath();
            System.out.println("record.parentFilePath =" +  record.parentFilePath);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println(path + " not found, return null");
            record = null;
        }
        return record;
    }

    private static String constructPath(String parentDirPath) {
        return parentDirPath + "record_config.xml";
    }

    /*--- Getter & Setter ---*/
    public String getVideoFilename() {
        return videoFilename;
    }

    public void setVideoFilename(String videoFilename) {
        this.videoFilename = videoFilename;
    }

    public long getVideoStartTime() {
        return videoStartTime;
    }

    public void setVideoStartTime(long videoStartTime) {
        this.videoStartTime = videoStartTime;
    }

    public String getLogFilename() {
        return logFilename;
    }

    public void setLogFilename(String logFilename) {
        this.logFilename = logFilename;
    }

    public String getVideoFilePath() {
        return parentFilePath + "/" + getVideoFilename();
    }

    public String getLogFilePath() {
        return parentFilePath + "/" + getLogFilename();
    }
    /*--- Exception definition ---*/
    public static class FormatErrorException extends Exception {

    }
}
