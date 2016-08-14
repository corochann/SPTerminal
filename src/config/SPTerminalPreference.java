package config;

import java.io.*;
import java.util.HashMap;

/**
 * Loads/Saves Project configuration.
 *
 */
public class SPTerminalPreference {

    private static SPTerminalPreference uniqueInstance = null;

    private HashMap<String, String> preferenceMap = null;

    private SPTerminalPreference() {

    }

    public static synchronized SPTerminalPreference getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new SPTerminalPreference();
            uniqueInstance.initilize();
        }
        return uniqueInstance;
    }

    /** Initilization */
    public void initilize() {
        loadPreference();
    }

    /** Finalization */
    public void finalize() {
        savePreference();
        uniqueInstance = null;
    }

    /**
     * Load preference from SPTerminal.ini
     * SPTerminal.ini should have the following form to store information
     * "key=value"
     * This key and value is extracted in {@link #preferenceMap} and can be used in the project.
     */
    public synchronized void loadPreference() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(ProjectConfig.SPTERMINAL_INI_PATH));
            String line;
            preferenceMap = new HashMap<>();  // Initialize
            while ((line = br.readLine()) != null) {
                if (line.contains("=")) {
                    String[] keyValueArray = line.split("=");
                    String key = keyValueArray[0].trim();
                    String value = keyValueArray[1].trim();
                    System.out.println("key: " + key + ",value: " + value);
                    preferenceMap.put(key, value);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save preference to SPTerminal.ini
     * preference infomation, key and value of {@link #preferenceMap}, is saved to SPTerminal.ini in the following form
     * "key=value"
     */
    public synchronized void savePreference() {
        if (preferenceMap != null) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(ProjectConfig.SPTERMINAL_INI_PATH));
                for (String key : preferenceMap.keySet()) {
                    String value = preferenceMap.get(key);
                    System.out.println("save -> " + key + "=" + value); // [DEBUG]
                    bw.write(key + "=" + value + "\n");
                    bw.flush();
                }
                bw.close();
                System.out.println(ProjectConfig.SPTERMINAL_INI_PATH + " successfully saved.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** get String value from preference */
    public String getStringValue(String key, String defaultValue) {
        if (preferenceMap != null && preferenceMap.containsKey(key)) {
            return preferenceMap.get(key);
        } else {
            System.out.println("key " + key + " not found!");
            return defaultValue;
        }
    }

    /** get Integer value from preference */
    public Integer getIntValue(String key, Integer defaultValue) {
        if (preferenceMap != null && preferenceMap.containsKey(key)) {
            return Integer.parseInt(preferenceMap.get(key));
        } else {
            System.out.println("key " + key + " not found!");
            return defaultValue;
        }
    }

    /**
     * set String value to preference,
     * if key already exists it will be replaced by new value
     * @param key
     * @param value
     */
    public void setStringValue(String key, String value) {
        if (preferenceMap == null) {
            System.out.println("[ERROR] preferenceMap is null");
        } else {
            preferenceMap.put(key, value);
        }
    }

    /**
     * set Integer value to preference,
     * if key already exists it will be replaced by new value
     * @param key
     * @param value
     */
    public void setIntValue(String key, Integer value) {
        if (preferenceMap == null) {
            System.out.println("[ERROR] preferenceMap is null");
        } else {
            preferenceMap.put(key, value.toString());
        }
    }
}
