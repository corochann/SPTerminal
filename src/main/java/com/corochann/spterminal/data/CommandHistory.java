package com.corochann.spterminal.data;

import com.corochann.spterminal.util.MyUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Vector;

import static com.corochann.spterminal.config.ProjectConfig.CACHE_FOLDER;

/**
 * Command history data
 * It should be maintained independently for each port.
 */
public class CommandHistory implements Serializable {

    public static final String PREFIX = CommandHistory.class.getName();
    private static int commandHistoryVecMaxSize = 10000;   // The value must be more than 10 for now, see refresh()
    //TODO: Not implemented yet refresh() for map.
    private static int commandHistoryMapMaxSize = 10000;    // The value must be more than 10 for now, see refresh()

    /* Attribute */
    private String portName;  // portName which this CommandHistory belongs to
    /** Command history, newer(recent) command is added to last */
    public Vector<String> commandHistoryVec = null;
    /** Key: command, Value: System epoch time for understanding which command executed in most recent */
    public HashMap<String, Long> commandHistoryMap = null;

    private CommandHistory(String portName) {
        /* Init */
        this.commandHistoryVec = new Vector<>();
        this.commandHistoryMap = new HashMap<>();
        this.portName = portName;
    }

    public void insertCommand(String command) {
        commandHistoryVec.add(command);
        commandHistoryMap.put(command, System.currentTimeMillis());
        refresh();
    }

    /**
     * Refresh {@link #commandHistoryVec} and {@link #commandHistoryMap} size
     * to keep performance
     */
    public void refresh() {
        if (commandHistoryVec.size() > commandHistoryVecMaxSize) {
            /* Remove 10 elements at once, for considering efficiency */
            commandHistoryVec.subList(0, 10).clear();
        }

        if (commandHistoryMap.size() > commandHistoryMapMaxSize) {
            //TODO: Not implemented yet.
            // Check value, system epoch time, to sort most recently used command.
            // Dispose the value which is used in old time.
        }
    }

    public void save() {
        String saveFilePath = getPath(portName);
        System.out.println("Saving CommandHistory to " + saveFilePath);
        try {
            FileOutputStream fos = new FileOutputStream(saveFilePath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CommandHistory load(String portName) {
        String loadFilePath = getPath(portName);
        System.out.println("Loading CommandHistory from " + loadFilePath);
        CommandHistory loadObject = null;
        try {
            FileInputStream fis = new FileInputStream(loadFilePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            loadObject = (CommandHistory)ois.readObject();
            ois.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        if (loadObject == null) {
            loadObject = new CommandHistory(portName);  // Initialize for first time usage.
        }
        return loadObject;
    }

    /**
     * Constructs & returns a path that this CommandHistory instance is saved.
     * Path is unique for each portName
     * @param portName
     * @return
     */
    private static String getPath(String portName) {
        String saveDirPath = CACHE_FOLDER + "/" + portName;
        MyUtils.prepareDir(saveDirPath);
        return saveDirPath + "/" + PREFIX;  // Use PREFIX as filename.
    }
}
