package com.corochann.spterminal.webcamplugin;

import com.corochann.spterminal.config.ProjectConfig;
import com.corochann.spterminal.plugin.MenuItemPlugin;
import com.corochann.spterminal.ui.SPTerminal;
import com.corochann.spterminal.webcamplugin.data.model.VideoLogRecord;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Webcam plugin to support play & record feature
 */
public class WebcamMenuItemPlugin implements MenuItemPlugin, ActionListener {
    /*--- CONSTANTS ---*/
    public static final String ACTION_WEBCAM_PLAY = "play";
    public static final String ACTION_WEBCAM_RECORD = "record";

    /** Returns title of this plugin. */
    @Override
    public String getText() {
        return "Webcam";
    }

    /** Returns this plugin's instance.
     * It can be either {@link JMenu} or {@link JMenuItem}
     */
    @Override
    public JMenuItem createJMenuItemInstance() {
        JMenu webcamMenuItem = new JMenu(getText());
        JMenuItem recordMenuItem = new JMenuItem("Record");
        recordMenuItem.setActionCommand(ACTION_WEBCAM_RECORD);
        recordMenuItem.addActionListener(this);

        JMenuItem playMenuItem = new JMenuItem("Play");
        playMenuItem.setActionCommand(ACTION_WEBCAM_PLAY);
        playMenuItem.addActionListener(this);

        webcamMenuItem.add(recordMenuItem);
        webcamMenuItem.add(playMenuItem);
        return webcamMenuItem;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        System.out.println("Webcam plugin called! action = " + action);
        switch (action) {
            case ACTION_WEBCAM_PLAY:
                // Choose file from JFileChooser
                setupPlayerFrame();
                break;
            case ACTION_WEBCAM_RECORD:
                new WebcamRecoderFrame().showFrame();
                break;
            default:
                break;
        }
    }

    private void setupPlayerFrame() {
        JFileChooser fileChooser = new JFileChooser(ProjectConfig.LOG_FOLDER);
        FileFilter xmlFileFilter = new FileNameExtensionFilter("record_config file", "xml");
        fileChooser.addChoosableFileFilter(xmlFileFilter);
        fileChooser.setFileFilter(xmlFileFilter);
        int selected = fileChooser.showOpenDialog(SPTerminal.getFrame());

        if (selected == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (checkFile(file)) {
                String fileName = file.getName();
                //String filePath = file.getAbsolutePath();
                System.out.println(fileName + " selected, path = " + file.getAbsolutePath());
                VideoLogRecord videoLogRecord = VideoLogRecord.load(file.getAbsolutePath());
                if (videoLogRecord != null) {
                    //PlayerFrame playerFrame = new PlayerFrame(PlayerFrame.VIDEO_PATH, PlayerFrame.LOG_PATH);
                    PlayerFrame playerFrame = new PlayerFrame(videoLogRecord);
                    playerFrame.showFrame();
                }
            } else {
                System.out.println("file access error");
            }
        } else {
            System.out.println("play cancel");
        }
    }

    private boolean checkFile(File file) {
        if (file.exists()) {
            if (file.isFile() && file.canRead()) {
                return true;
            }
        }
        return false;
    }

}
