package com.corochann.spterminal.data.model;

import com.corochann.spterminal.config.FilterConfig;
import com.corochann.spterminal.config.ProjectConfig;
import com.corochann.spterminal.util.MyUtils;

import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Vector;

/**
 * Log filter rule, this is model for {@link FilterConfig}
 */
public class FilterRule implements Serializable {
     /*--- Attributes ---*/
    /** File name of this filter rule, it CAN contain [Space] character */
    private String fileName = ""; // this is abbreviation. Real file name is {filename}.xml
    private Vector<FilterRuleElement> filterRuleVec;

    /*--- Constructor ---*/
    public FilterRule() {

    }

    /*--- Save & Load ---*/
    /**
     * save this preference
     */
    public synchronized void save() throws FormatErrorException {
        /* Error check - abbreviation and command must contain String */
        if (fileName == null || fileName.length() == 0) {
            System.out.println("[Error] fileName is null or length is 0");
            throw new FormatErrorException();
        }
        if (filterRuleVec == null || filterRuleVec.size() == 0) {
            System.out.println("[Error] filterRuleVec is null or length is 0");
            throw new FormatErrorException();
        }

        MyUtils.prepareDir(ProjectConfig.FILTER_PATH_PREFIX);
        String path = constructPath(fileName);
        System.out.println("Saving filter rule to " + path);
        try {
            MyUtils.writeToXML(this, path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * load this preference
     */
    public static synchronized FilterRule load(String fileName) {
        String path = constructPath(fileName);
        FilterRule filterRule;
        try {
            filterRule = (FilterRule)MyUtils.readFromXML(path);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println(path + " not found, return null");
            filterRule = null;
        }
        return filterRule;
    }

    /**
     * Delete xml file
     */
    public static void deleteFile(String fileName) {
        String path = constructPath(fileName);
        if(new File(path).delete()) {
            System.out.println("Deleted filter rule file: " + path);
        } else {
            System.out.println("Deleting filter rule file failed: " + path);
        }
    }

    public static String constructPath(String fileName) {
        return ProjectConfig.FILTER_PATH_PREFIX + fileName + ".xml";
    }

    /*--- Getter & Setter ---*/
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Vector<FilterRuleElement> getFilterRuleVec() {
        return filterRuleVec;
    }

    public void setFilterRuleVec(Vector<FilterRuleElement> filterRuleVec) {
        this.filterRuleVec = filterRuleVec;
    }

    /*--- Inner class ---*/
    public static class FilterRuleElement implements Serializable {
        /** remove log which starts with specified query until the end of line */
        public static final int RULE_TYPE_STARTSWITH = 1;
        /** remove whole line if specified query matches */
        public static final int RULE_TYPE_CONTAIN = 2;

        private int ruleType = -1;
        private String query = "";
        private boolean matchCase = false;
        private boolean regex = true;

        /** This void argument constructor is necessary for {@link XMLEncoder} to serialize this object */
        public FilterRuleElement() {

        }

        public FilterRuleElement(int ruleType, String query, boolean matchCase, boolean regex) {
            this.ruleType = ruleType;
            this.query = query;
            this.matchCase = matchCase;
            this.regex = regex;
        }

        public int getRuleType() {
            return ruleType;
        }

        public void setRuleType(int ruleType) {
            this.ruleType = ruleType;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public boolean isMatchCase() {
            return matchCase;
        }

        public void setMatchCase(boolean matchCase) {
            this.matchCase = matchCase;
        }

        public boolean isRegex() {
            return regex;
        }

        public void setRegex(boolean regex) {
            this.regex = regex;
        }

        public boolean validate() {
            boolean validateFlag = true;
            if (query == null || query.length() <= 0) {
                validateFlag = false;
            }
            return validateFlag;
        }
    }

    /*--- Exception definition ---*/
    public static class FormatErrorException extends Exception {

    }
}
