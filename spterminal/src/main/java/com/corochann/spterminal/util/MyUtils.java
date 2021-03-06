package com.corochann.spterminal.util;

import com.corochann.spterminal.config.ProjectConfig;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Util class
 */
public class MyUtils {

    /* String manipulation */
    public static String  unEscapeString(String s){
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<s.length(); i++)
            switch (s.charAt(i)){
                case '\n': sb.append("\\n"); break;  // same with case '\012':
                case '\t': sb.append("\\t"); break;  // same with case '\011':
                case '\b': sb.append("\\b"); break;  // same with case '\010':
                case '\r': sb.append("\\r"); break;  // same with case '\015':
                //case ' ': sb.append("\\s"); break;  // \\s is to show space, this is unique spec
                /* ASCII Code is specified by "Octal" value */
                case '\001': sb.append("\\001"); break;
                case '\002': sb.append("\\002"); break;
                case '\003': sb.append("\\003"); break;
                case '\004': sb.append("\\004"); break;
                case '\005': sb.append("\\005"); break;
                case '\006': sb.append("\\006"); break;
                case '\007': sb.append("\\007"); break;
                // 010 = \b, 011 = \t, 012 = \n
                case '\013': sb.append("\\013"); break;
                case '\014': sb.append("\\014"); break;
                // 015 = \r
                case '\016': sb.append("\\016"); break;
                case '\017': sb.append("\\017"); break;
                case '\020': sb.append("\\020"); break;
                case '\021': sb.append("\\021"); break;
                case '\022': sb.append("\\022"); break;
                case '\023': sb.append("\\023"); break;
                case '\024': sb.append("\\024"); break;
                case '\025': sb.append("\\025"); break;
                case '\026': sb.append("\\026"); break;
                case '\027': sb.append("\\027"); break;
                case '\030': sb.append("\\030"); break;
                case '\031': sb.append("\\031"); break;
                case '\032': sb.append("\\032"); break;
                case '\033': sb.append("\\033"); break;
                case '\034': sb.append("\\034"); break;
                case '\035': sb.append("\\035"); break;
                case '\036': sb.append("\\036"); break;
                case '\037': sb.append("\\037"); break;

                // ... rest of escape characters
                default: sb.append(s.charAt(i));
            }
        return sb.toString();
    }


    /*--- File IO ---*/

    /**
     * If directory specified by directoryPath is not exist, create it.
     * @param directoryPath
     */
    public static void prepareDir(String directoryPath) {
        try {
            if (new File(directoryPath).mkdirs()) {
                System.out.println(directoryPath + " directory created.");
            }
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    /**
     * If file specfied by filePath is not exist, create empty file.
     * @param filePath
     */
    public static void prepareFile(String filePath) {
        File f = new File(filePath);
        if (!f.exists() || f.isDirectory()) {
            try {
                if (f.createNewFile()) {
                    System.out.println(filePath + " created.");
                } else {
                    System.out.println("[ERROR]: " + filePath + " create fail.");
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Saves str to filePath.
     * @param str
     * @param filePath
     */
    public static void writeToFile(String str, String filePath) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
        bw.write(str);
        bw.flush();
        bw.close();
    }

    /**
     * Loads string from filePath
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String readFromFile(String filePath) throws IOException {
        String fileStr = "";
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = "";
        while((line = br.readLine()) != null) {
            fileStr += line + "\n";
        }
        br.close();
        return fileStr;
    }

    /**
     * Extracts extension of file in lower case
     * Ref: http://www.javadrive.jp/tutorial/jfilechooser/index12.html
     */
    public static String getExtension(File f){
        String ext = null;
        String filename = f.getName();
        int dotIndex = filename.lastIndexOf('.');

        if ((dotIndex > 0) && (dotIndex < filename.length() - 1)){
            ext = filename.substring(dotIndex + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * get file name without extension
     * Ex 'abced.txt' -> 'abcde'
     * @param f
     * @return
     */
    public static String getFileNameWithoutExtension(File f){
        String filename = f.getName();
        int dotIndex = filename.lastIndexOf('.');

        if ((dotIndex > 0) && (dotIndex < filename.length() - 1)){
            filename = filename.substring(0, dotIndex).toLowerCase();
        }
        return filename;
    }

    /*
     * XML Encoding/Decoding using java.beans.XMLEncoder, java.beans.XMLDecoder
     * Ref: http://www.rgagnon.com/javadetails/java-0470.html
     */
    /**
     * Saves obj to filepath in XML format.
     * @param obj
     * @param filepath
     * @throws FileNotFoundException
     */
    public static void writeToXML(Object obj, String filepath) throws FileNotFoundException {
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader newLoader = obj.getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(newLoader);

        XMLEncoder encoder = new XMLEncoder(
                new BufferedOutputStream(new FileOutputStream(filepath))
        );
        encoder.writeObject(obj);
        encoder.close();

        Thread.currentThread().setContextClassLoader(oldLoader);
    }

    /**
     * Loads obj from filepath in XML format.
     * The return object should be casted to each obj's proper class.
     * @param filepath
     * @return
     * @throws FileNotFoundException
     */
    public static Object readFromXML(String filepath) throws FileNotFoundException {
        XMLDecoder decoder = new XMLDecoder(
                new BufferedInputStream(new FileInputStream(filepath))
        );
        Object obj = decoder.readObject();
        decoder.close();
        return obj;
    }

    public static Object readFromXML(String filepath, Class className) throws FileNotFoundException {
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader newLoader = className.getClassLoader();
        Thread.currentThread().setContextClassLoader(newLoader);

        XMLDecoder decoder = new XMLDecoder(
                new BufferedInputStream(new FileInputStream(filepath))
        );
        Object obj = decoder.readObject();
        decoder.close();

        Thread.currentThread().setContextClassLoader(oldLoader);
        return obj;
    }

    /*--- Project specific util functions ---*/
    /*--- Plugin ---*/
    /** Load specific plugin
     *
     * Ref: http://stackoverflow.com/questions/16102010/dynamically-loading-plugin-jars-using-serviceloader
     * Example usage.
     * Iterator<SimplePlugin> apit = loadPlugins(SimplePlugin.class);
     * while (apit.hasNext()) {
     *   System.out.println(apit.next().getName());
     * }
     */
    public static <S> Iterator<S> loadPlugins(Class<S> className) {
        System.out.println("pluginTest");
        File loc = new File(ProjectConfig.PLUGIN_FOLDER);

        File[] flist = loc.listFiles(new FileFilter() {
            public boolean accept(File file) {return file.getPath().toLowerCase().endsWith(".jar");}
        });
        URL[] urls = new URL[flist.length];
        for (int i = 0; i < flist.length; i++) {
            try {
                urls[i] = flist[i].toURI().toURL();
                System.out.println(urls[i].toString() + " found");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        URLClassLoader ucl = new URLClassLoader(urls);
        ServiceLoader<S> sl = ServiceLoader.load(className, ucl);
        return sl.iterator();
    }

}
