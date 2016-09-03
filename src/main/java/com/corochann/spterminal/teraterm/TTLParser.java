package com.corochann.spterminal.teraterm;

import java.util.*;

import static com.corochann.spterminal.teraterm.TTLParser.TTLParam.TYPE_INTEGER;
import static com.corochann.spterminal.teraterm.TTLParser.TTLParam.TYPE_STRING;

/**
 * Teraterm macro (.ttl) parse logic
 * Util class
 */
public class TTLParser {

    public static final String TTL_COMMENT_SYMBOL1 = ";";

    /**
     * //TODO: Currently C Style comment is not supported
     * Find comment in below format
     * 1. One line comment: ; comment
     * 2. C Style comment:  /* comment * /
     * and remove comment
     *
     * It will also format to use "\n" as line separator.
     * @param inputStr
     * @return
     */
    public static String removeComment(String inputStr) {
        //TODO: Currently C Style comment is not supported
        String outStr = "";
        Scanner scanner = new Scanner(inputStr);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String parsedLine = removeOneLineComment(line);
            outStr += parsedLine.trim() + "\n";
        }
        scanner.close();
        return outStr;
    }

    /**
     * Find comment String ";" and remove following sentence.
     * @param command expected one line command (Must not include "\n")
     * @return
     */
    public static String removeOneLineComment(String command) {
        String parsedCommand = command;
        int index = command.indexOf(TTL_COMMENT_SYMBOL1);
        if (index != -1) {
            parsedCommand = command.substring(0, index);
        } // if -1, no comment exists in this line. do nothing.
        return parsedCommand;
    }

    /**
     * Deprecated. Use {@link #extractArgsWithType(String)}
     * This method just split args by " ".
     * Ex1. sendln "hello world!" --> args[0] = "sendln", args[1] = '"hello', args[2] = 'world!'
     * Ex2. mpause   3000         --> args[0] = mpause,   args[1] = "3000"
     * @param command
     * @return
     */
    public static String[] extractArgs(String command) {
        String[] args = new String[0];
        if (command == null || command.length() == 0) {
            return args;
        } else {
            args = command.split(" ");
            // remove empty string from args array.
            List<String> argsList = new ArrayList<>(Arrays.asList(args));
            argsList.removeAll(Arrays.asList(""));
            args = argsList.toArray(new String[argsList.size()]);
            return args;
        }
    }

    /**
     * This method just split args by " ".
     * Ex1. sendln "hello world!" --> args[0] = "sendln", args[1] = '"hello', args[2] = 'world!'
     * Ex2. mpause   3000         --> args[0] = mpause,   args[1] = "3000"
     * @param command
     * @return
     */
    public static List<TTLParam> extractArgsWithType(String command) {
        List<TTLParam> argsList = new ArrayList<>();
        if (command == null || command.length() == 0) {
            return argsList;
        } else {
            argsList.add(new TTLParam());
            int argIndex = 0; // 0: command, 1~ : params
            int startIndex = 0;
            int endIndex = 0;
            for (int i = 0; i < command.length(); i++) {
                char c = command.charAt(i);
                if (c == ' ') {
                    // Create current TTLParam
                    endIndex = i;
                    //argsList.get(argIndex).type = TYPE_INTEGER;
                    System.out.println("debug1 start = " + startIndex + ", end " + endIndex);
                    argsList.get(argIndex).param += command.substring(startIndex, endIndex);

                    // Go until next none space
                    while (command.charAt(i) == ' ') {
                        i++;
                        if (i >= command.length()) break;
                    }
                    // update startIndex before break;
                    startIndex = i;
                    if (i >= command.length()) break;
                    i--;

                    // Add new TTLParam
                    argsList.add(new TTLParam());
                    argIndex++;
                } else if (c == '"' || c == '\'') {
                    // Go until next '"' or '\''
                    i++;
                    startIndex = i;
                    while (command.charAt(i) != c) {
                        i++;
                        if (i >= command.length()) break;
                    }
                    if (i >= command.length()) {
                        // update startIndex
                        startIndex = i;
                        break;
                    }
                    endIndex = i;
                    argsList.get(argIndex).type = TYPE_STRING;
                    System.out.println("debug2 start = " + startIndex + ", end " + endIndex);
                    argsList.get(argIndex).param += command.substring(startIndex, endIndex);
                    // update startIndex
                    startIndex = i + 1;
                } else if (c == '#') {
                    // Convert to ASCII
                    // Go until next numeric expression ends.
                    i++;
                    if (i >= command.length()) {
                        System.out.println("[Unexpected command] only '#', number expected.");
                        break;
                    }
                    char nextChar = command.charAt(i);
                    boolean hexFlag = false;
                    if ('$' == nextChar) {
                        hexFlag = true;
                        i++;
                        if (i >= command.length()) {
                            System.out.println("[Unexpected command] only '#$', number expected.");
                            break;
                        }
                    }
                    startIndex = i;
                    while (true) {
                        nextChar = command.charAt(i);
                        if (('0' <= nextChar && nextChar <= '9') ||
                                ('a' <= nextChar && nextChar <= 'f') ||
                                ('A' <= nextChar && nextChar <= 'F')) {
                            i++;
                            if (i >= command.length()) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    endIndex = i;
                    i--;
                    String value = command.substring(startIndex, endIndex);
                    int decimal = Integer.parseInt(value, hexFlag ? 16 : 10);
                    argsList.get(argIndex).type = TYPE_STRING;
                    System.out.println("debug# start = " + startIndex + ", end " + endIndex + ", decimal " + decimal);
                    argsList.get(argIndex).param += Character.toString((char)decimal);
                    // update startIndex
                    startIndex = i + 1;
                } else if (c == '$') {
                    // TODO: test. implementation done but not tested.
                    // Convert to decimal Integer from Hex
                    // Go until next numeric expression ends.
                    i++;
                    startIndex = i;
                    if (i >= command.length()) {
                        System.out.println("[Unexpected command] only '$', number expected.");
                        break;
                    }
                    while (true) {
                        char nextChar = command.charAt(i);
                        if (('0' <= nextChar && nextChar <= '9') ||
                                ('a' <= nextChar && nextChar <= 'f') ||
                                ('A' <= nextChar && nextChar <= 'F')) {
                            i++;
                            if (i >= command.length()) { break; }
                        } else {
                            break;
                        }
                    }
                    endIndex = i;
                    i--;
                    String hexValue = command.substring(startIndex, endIndex);
                    Integer decimal = Integer.parseInt(hexValue, 16);

                    argsList.get(argIndex).type = TYPE_INTEGER;
                    System.out.println("debug$ start = " + startIndex + ", end " + endIndex);
                    argsList.get(argIndex).param += decimal.toString();
                    // update startIndex
                    startIndex = i + 1;
                } else {

                }

            }
            endIndex = command.length();
            System.out.println("debugfinal start = " + startIndex + ", end " + endIndex);
            argsList.get(argIndex).param += command.substring(startIndex, endIndex);
            return argsList;
        }
    }

    public static class TTLParam {
        /*--- CONSTANT ---*/
        public static final int TYPE_UNDEFINED = 0;
        // param = 123, -11, $3a $10F etc.
        public static final int TYPE_INTEGER = 1;
        // param = "Hello, world", "I can't do it", 'this is "pen"'
        // Also support ASCII -> param = #65, #$41, #13 etc.
        public static final int TYPE_STRING = 2;

        /*--- Attribute ---*/
        public int type;
        public String param;

        TTLParam() {
            this.type = TYPE_UNDEFINED;
            this.param = "";
        }
    }

    /**
     * Get sequence of text and convert it to TTL
     * @param commandText
     * @return
     */
    public static String convertToSendlnCommand(String commandText) {
        String outStr = "";
        Scanner scanner = new Scanner(commandText);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            line = line.replace("\"", "\"#$22\"");
            outStr += "sendln \"" + line + "\"\n";
        }
        scanner.close();
        return outStr;
    }
}
