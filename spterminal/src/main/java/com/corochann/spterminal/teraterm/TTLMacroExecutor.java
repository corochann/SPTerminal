package com.corochann.spterminal.teraterm;

import com.corochann.spterminal.serial.SerialPortTX;
import com.corochann.spterminal.ui.SPTerminal;
import com.corochann.spterminal.util.MyUtils;

import javax.swing.*;
import java.io.*;
import java.util.List;

import static com.corochann.spterminal.teraterm.TTLParser.TTLParam.TYPE_INTEGER;
import static com.corochann.spterminal.teraterm.TTLParser.TTLParam.TYPE_STRING;

/**
 *
 */
public class TTLMacroExecutor extends Thread {

    private SerialPortTX tx;
    private String filePath;

    /**
     *
     * @param filePath Either absolute path of ttl macro or
     *                 only fileName (this case loaded from path specified by {@link TTLMacro})
     * @param tx
     */
    public TTLMacroExecutor(String filePath, SerialPortTX tx) {
        this.filePath = filePath;
        this.tx = tx;
    }

    /**
     * 1. Load Teraterm Macro
     * 2. Execute macro swquentially
     */
    public void run() {

        /*--- 1. LOAD: read ttl file from file ---*/
        String fileStr = "";
        try {
            if (new File(this.filePath).exists()) {
                /* 1.1 load from absolute path */
                System.out.println("loading ttl macro from " + this.filePath);
                fileStr = MyUtils.readFromFile(this.filePath);
            } else {
                /* 1.2 load from fileName */
                String path = TTLMacro.constructPath(this.filePath);
                System.out.println("loading ttl macro from " + path);
                fileStr = MyUtils.readFromFile(path);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }

        /*--- 2. PREPROCESS: Remove comments ---*/
        String commandsStr = TTLParser.removeComment(fileStr);
        String[] commandsArray = commandsStr.split("\n");
        fileStr = null;  // Explicitly make GC
        commandsStr = null;  // Explicitly make GC


        /*--- 3. Execute macro |line| ---*/
        for (int i = 0; i < commandsArray.length; i++) {
            System.out.println("Execute " + commandsArray[i]);
            //TODO: impl, for loop etc.
            executeMacro(commandsArray[i]);
        }
    }

    /**
     * Macro parsing/compile process, execute command.
     * @param macroCommand
     */
    private void executeMacro(String macroCommand) {
        //TODO: impl
        List<TTLParser.TTLParam> args = TTLParser.extractArgsWithType(macroCommand);

        /*--- 2.  ---*/
        if (args.size() == 0) {
            // Do nothing
            return;
        } else {
            String command = args.get(0).param;
            switch (command){
                /* Ref: https://ttssh2.osdn.jp/manual/ja/macro/syntax/identifiers.html */
                /*--- 1. Command ---*/
                /*--- 1.1 Communication command ---*/
                case "bplusrecv":
                case "bplussend":
                case "callmenu":
                case "changedir":
                case "clearscreen":
                case "closett":
                case "connect":
                case "cygconnect":
                case "disconnect":
                case "dispstr":
                case "enablekeyb":
                case "flushrecv":
                case "gethostname":
                case "getmodemstatus":
                case "gettitle":
                case "kmtfinish":
                case "kmtget":
                case "kmtrecv":
                case "kmtsend":
                case "loadkeymap":
                case "logautoclosemode":
                case "logclose":
                case "loginfo":
                case "logopen":
                case "logpause":
                case "logrotate":
                case "logstart":
                case "logwrite":
                case "quickvanrecv":
                case "quickvansend":
                case "recvln":
                case "restoresetup":
                case "scprecv":
                case "scpsend":
                    commandNotSupported(command);
                    break;
                case "send":
                    ttlSend(args);
                    break;
                case "sendbreak":
                case "sendbroadcast":
                case "sendfile":
                case "sendkcode":
                    commandNotSupported(command);
                    break;
                case "sendln":
                    ttlSend(args);
                    tx.transmitNewLine();
                    break;
                case "sendlnbroadcast":
                case "sendmulticast":
                case "setbaud":
                case "setdebug":
                case "setdtr":
                case "setecho":
                case "setmulticastname":
                case "setrts":
                case "setsync":
                case "settitle":
                case "showtt":
                case "testlink":
                case "unlink":
                case "wait":
                case "wait4all":
                case "waitevent":
                case "waitln":
                case "waitn":
                case "waitrecv":
                case "waitregex":
                case "xmodemrecv":
                case "xmodemsend":
                case "ymodemrecv":
                case "ymodemsend":
                case "zmodemrecv":
                case "zmodemsend":
                    commandNotSupported(command);
                    break;
                /*--- 1.2 Control command ---*/
                case "break":
                case "call":
                case "continue":
                case "do":
                case "loop":
                case "end":
                case "execcmnd":
                case "exit":
                case "for":
                case "next":
                case "goto":
                case "if":
                case "then":
                case "elseif":
                case "else":
                case "endif":
                case "include":
                    commandNotSupported(command);
                    break;
                case "mpause":
                    ttlPause(args, 1);    // pause milliseconds
                    break;
                case "pause":
                    ttlPause(args, 1000); // pause seconds
                    break;
                case "return":
                case "until":
                case "enduntil":
                case "while":
                case "endwhile":
                    commandNotSupported(command);
                    break;
                /*--- 1.3 String manipulation command ---*/
                case "code2str":
                case "expandenv":
                case "int2str":
                case "regexoption":
                case "sprintf":
                case "sprintf2":
                case "str2code":
                case "str2int":
                case "strcompare":
                case "strconcat":
                case "strcopy":
                case "strinsert":
                case "strjoin":
                case "strlen":
                case "strmatch":
                case "strremove":
                case "strreplace":
                case "strscan":
                case "strspecial":
                case "strsplit":
                case "strtrim":
                case "tolower":
                case "toupper":
                    commandNotSupported(command);
                    break;
                /*--- 1.4 File manipulation command ---*/
                case "basename":
                case "dirname":
                case "fileclose":
                case "fileconcat":
                case "filecopy":
                case "filecreate":
                case "filedelete":
                case "filelock":
                case "filemarkptr":
                case "fileopen":
                case "filereadln":
                case "fileread":
                case "filerename":
                case "filesearch":
                case "fileseek":
                case "fileseekback":
                case "filestat":
                case "filestrseek":
                case "filestrseek2":
                case "filetruncate":
                case "fileunlock":
                case "filewrite":
                case "filewriteln":
                case "findfirst,findnext,findclose":
                case "foldercreate":
                case "folderdelete":
                case "foldersearch":
                case "getdir":
                case "getfileattr":
                case "makepath":
                case "setdir":
                case "setfileattr":
                    commandNotSupported(command);
                    break;
                /*--- 1.5 Password command ---*/
                case "delpassword":
                case "getpassword":
                case "ispassword":
                case "passwordbox":
                case "setpassword":
                    commandNotSupported(command);
                    break;
                /*--- 1.6 Other command ---*/
                case "beep":
                case "bringupbox":
                case "checksum8":
                case "checksum8file":
                case "checksum16":
                case "checksum16file":
                case "checksum32":
                case "checksum32file":
                case "closesbox":
                case "clipb2var":
                case "crc16":
                case "crc16file":
                case "crc32":
                case "crc32file":
                case "exec":
                case "dirnamebox":
                case "filenamebox":
                case "getdate":
                case "getenv":
                case "getipv4addr":
                case "getipv6addr":
                case "getspecialfolder":
                case "gettime":
                case "getttdir":
                case "getver":
                case "ifdefined":
                case "inputbox":
                case "intdim":
                case "listbox":
                case "messagebox":
                case "random":
                case "rotateleft":
                case "rotateright":
                case "setdate":
                case "setdlgpos":
                case "setenv":
                case "setexitcode":
                case "settime":
                case "show":
                case "statusbox":
                case "strdim":
                case "uptime":
                case "var2clipb":
                case "yesnobox":
                    commandNotSupported(command);
                    break;
                /*--- 2. Operator ---*/
                /*--- 3. System variable ---*/
                default:
                    /*--- 4. User defined variable ---*/
                    break;
            }
        }
    }

    /**
     *
     * @param args
     * @param scale scale specifies the time unit of pause. 1 for MilliSec, 1000 for Sec etc.
     */
    private void ttlPause(List<TTLParser.TTLParam> args, int scale) {
        if (args.size() > 1) {
            try {
                int msec = Integer.parseInt(args.get(1).param);
                sleep(msec * scale);
            } catch (NumberFormatException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void ttlSend(List<TTLParser.TTLParam> args) {
        for (int i = 1; i < args.size(); i++) {
            if (args.get(i).type == TYPE_STRING) {
                tx.transmitString(args.get(i).param);
            } else if (args.get(i).type == TYPE_INTEGER) {
                int ascii = Integer.parseInt(args.get(i).param) & 255;
                tx.transmitAscii(ascii);
            }
        }
    }

    /**
     * To indicate that this Teraterm command is not implemented in SPTerminal yet.
     * @param command
     */
    private void commandNotSupported(String command) {
        System.out.println("[WARNING] " + command + " not supported yet, it will be ignored");
        //TODO: notify user in GUI (user cannot see terminal log)
        final String unsupportedCommand = command;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SPTerminal.getFrame(), unsupportedCommand + " not supported yet, it will be ignored");
            }
        });
    }
}
