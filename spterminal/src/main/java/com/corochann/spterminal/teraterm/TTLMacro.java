package com.corochann.spterminal.teraterm;

import com.corochann.spterminal.config.ProjectConfig;
import com.corochann.spterminal.config.teraterm.TTLMacroConfig;
import com.corochann.spterminal.util.MyUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Teraterm macro save/load utility, this is model for {@link TTLMacroConfig}
 */
public class TTLMacro implements Serializable {
    /*--- Attributes ---*/
    /** File name of this Teraterm macro, it must not contain [Space] character */
    private String fileName = "";
    /** Actual (multi-line) macro commands to be executed. */
    private String command = "";

    /*--- Constructor ---*/

    /*--- Save & Load ---*/
    /**
     * save this preference
     */
    public synchronized void save() throws FormatErrorException {
        /* Error check - abbreviation and command must contain String */
        if (fileName == null || fileName.length() == 0 || fileName.contains(" ")) {
            System.out.println("[Error] fileName is null or length is 0");
            throw new FormatErrorException();
        }
        if (command == null || command.length() == 0) {
            System.out.println("[Error] command is null or length is 0");
            throw new FormatErrorException();
        }

        MyUtils.prepareDir(ProjectConfig.TTL_PATH_PREFIX);
        String path = constructPath(fileName);
        System.out.println("Saving Teraterm macro to " + path);
        try {
            MyUtils.writeToFile(this.command, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * load this preference
     */
    public static synchronized TTLMacro load(String fileName) {
        String path = constructPath(fileName);
        TTLMacro ttlMacro = new TTLMacro();
        try {
            ttlMacro.fileName = fileName;
            ttlMacro.command = MyUtils.readFromFile(path);
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println(path + " not found, return null");
            ttlMacro = null;
        }
        return ttlMacro;
    }

    /**
     * Delete xml file
     */
    public static void deleteFile(String fileName) {
        String path = constructPath(fileName);
        if(new File(path).delete()) {
            System.out.println("Deleted teraterm macro file: " + path);
        } else {
            System.out.println("Deleting teraterm macro file failed: " + path);
        }
    }

    public static String constructPath(String fileName) {
        return ProjectConfig.TTL_PATH_PREFIX + fileName + ".ttl";
    }

    /*--- Getter & Setter ---*/
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    /*--- Exception definition ---*/
    public static class FormatErrorException extends Exception {

    }
}
