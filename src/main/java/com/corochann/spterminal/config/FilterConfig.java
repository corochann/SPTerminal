package com.corochann.spterminal.config;

import com.corochann.spterminal.data.model.FilterRule;

import java.io.File;
import java.util.HashMap;

/**
 * Log Filter list data, this is preference class.
 * This class is load-only. (save is done by each file, see {@link FilterRule}
 */
public class FilterConfig {
    /**
     * list of filter rules in vector format
     * key: abbreviation, value: fileName
     */
    public HashMap<String, String> filterRuleMap;

    private FilterConfig() {
        filterRuleMap = new HashMap<>();
    }

    public void addFilterRule(String fileName) {
        if (filterRuleMap == null) {
            System.out.println("[ERROR] filterRuleMap is null");
            filterRuleMap = new HashMap<>();
        }
        filterRuleMap.put(constructKey(fileName), fileName);
    }

    public static String constructKey(String fileName) {
        return fileName;
    }

    /*--- Save & Load ---*/
    // Save is done by each FilterRule independently

    /**
     * load this preference
     */
    public static FilterConfig load() {
        FilterConfig filterConfig = new FilterConfig();
        String loadFolderPath = ProjectConfig.FILTER_PATH_PREFIX;
        File filterDir = new File(loadFolderPath);
        loadFolder(filterDir, filterConfig);
        return filterConfig;
    }

    /**
     * Recursively check dir
     * @param dir
     * @param filterConfig
     */
    private static void loadFolder(File dir, FilterConfig filterConfig) {
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
                    loadFile(file, filterConfig);
                }
            }
        }
    }

    /**
     * Find filter xml file and add this to filterRuleMap (It does not load the actual content of file)
     * @param file
     * @param filterConfig
     */
    private static void loadFile(File file, FilterConfig filterConfig) {
        String fullFileName = file.getName();
        if(fullFileName.lastIndexOf(".") != -1 && fullFileName.lastIndexOf(".") != 0) {
            // Ex1. it will ignore name without ., e.g. "folder-name".
            // Ex2. it will ignore name starting from ., e.g. ".git", ".bashrc".
            String extention = fullFileName.substring(fullFileName.lastIndexOf(".")+1);
            String fileName = fullFileName.substring(0, fullFileName.lastIndexOf("."));
            if (extention.equals("xml")) {
                //TTLMacro ttlMacro = TTLMacro.load(fileName);
                filterConfig.addFilterRule(fileName);
            }
        }
    }
}
