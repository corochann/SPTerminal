package com.corochann.spterminal.config.teraterm;

import com.corochann.spterminal.config.ProjectConfig;
import com.corochann.spterminal.teraterm.TTLMacro;

import java.io.File;
import java.util.HashMap;

/**
 * TTL Macro list data, this is preference class.
 * This class is load-only. (save is done by each file, see {@link TTLMacro}
 */
public class TTLMacroConfig {

    public static final String TTL_PREFIX = "[ttl] ";
    /** list of TTL Macro in vector format */
    public HashMap<String, String> ttlMacroMap;  // key: abbreviation, value: fileName

    private TTLMacroConfig() {
        ttlMacroMap = new HashMap<>();
    }


    public void addTTLMacro(String fileName) {
        if (ttlMacroMap == null) {
            System.out.println("[ERROR] ttlMacroMap is null");
            ttlMacroMap = new HashMap<>();
        }
        ttlMacroMap.put(constructKey(fileName), fileName);
    }

    public static String constructKey(String fileName) {
        return TTL_PREFIX + fileName;
    }

    /*--- Save & Load ---*/
    // Save is done by each SPTAlias independently

    /**
     * load this preference
     */
    public static TTLMacroConfig load() {
        TTLMacroConfig ttlMacroConfig = new TTLMacroConfig();
        String loadFolderPath = ProjectConfig.TTL_PATH_PREFIX;
        File ttlDir = new File(loadFolderPath);
        loadFolder(ttlDir, ttlMacroConfig);
        return ttlMacroConfig;
    }

    private static void loadFolder(File dir, TTLMacroConfig ttlMacroConfig) {
        File[] files = dir.listFiles();
        if(files == null) {
            // No file found under path, do nothing.
        } else {
            for (File file : files) {
                if(!file.exists()) {
                    // do nothing.
                } else if(file.isDirectory()) {
                    // do nothing
                    //loadFolder(file, ttlMacroConfig);
                } else if(file.isFile()) {
                    loadFile(file, ttlMacroConfig);
                }
            }
        }
    }

    private static void loadFile(File file, TTLMacroConfig ttlMacroConfig) {
        String fullFileName = file.getName();
        if(fullFileName.lastIndexOf(".") != -1 && fullFileName.lastIndexOf(".") != 0) {
            // Ex1. it will ignore name without ., e.g. "folder-name".
            // Ex2. it will ignore name starting from ., e.g. ".git", ".bashrc".
            String extention = fullFileName.substring(fullFileName.lastIndexOf(".")+1);
            String fileName = fullFileName.substring(0, fullFileName.lastIndexOf("."));
            if (extention.equals("ttl")) {
                //TTLMacro ttlMacro = TTLMacro.load(fileName);
                ttlMacroConfig.addTTLMacro(fileName);
            }
        }
    }
}
