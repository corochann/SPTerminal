package com.corochann.spterminal.webcamplugin;

import com.corochann.spterminal.config.ProjectConfig;
import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.style.StyleSelectorConfig;
import com.corochann.spterminal.ui.SPTerminal;
import com.corochann.spterminal.ui.component.HighlightableJTextPane;
import com.corochann.spterminal.util.MyUtils;
import com.corochann.spterminal.webcamplugin.component.VideoPlayerListener;
import com.corochann.spterminal.webcamplugin.component.VideoPlayerPanel;
import com.corochann.spterminal.webcamplugin.data.model.VideoLogRecord;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Scanner;
import java.util.Vector;

/**
 * Play log and video recorded by {@link WebcamRecoderFrame}.
 */
public class PlayerFrame extends JFrame implements ActionListener {

    /*--- CONSTANTS ---*/
    // TODO: remove, for debug

    public static final String XML_PATH = "D://workspace/intellij/SPTerminal/spterminal/log/record_20161122_182514/record_config.xml";
    public static final String VIDEO_PATH = "D://workspace/intellij/SPTerminal/spterminal/log/record_20161117_184601/video.mp4";
    public static final String LOG_PATH = "D://workspace/intellij/SPTerminal/spterminal/log/record_20161117_184601/record_timestamp.log";
    private VideoPlayerPanel videoPlayerPanel;
    private HighlightableJTextPane logTextPane;


    /*--- Action definitions ---*/

    /*--- Relations ---*/

    /*--- Attribute ---*/
    private boolean autoScroll = true; // TODO: Allow user to change. Currently it is always true.
    private long videoStartTime = 0L;
    private JScrollPane logSP;

/*
    public PlayerFrame(String videoPath, String logPath) {
        this(videoPath, logPath, null);
    }
*/

    public PlayerFrame(VideoLogRecord videoLogRecord) {
        this(videoLogRecord, null);
    }

    //public PlayerFrame(String videoPath, String logPath, GraphicsConfiguration gc) {
    public PlayerFrame(VideoLogRecord videoLogRecord, GraphicsConfiguration gc) {
        super(gc);
        videoStartTime = videoLogRecord.getVideoStartTime();
        this.setTitle("Video & log player");
        StyleSelectorConfig styleSelectorConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig();

        /*--- Create components ---*/
        JPanel mainPanel = new JPanel();
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        mainSplitPane.setDividerSize(5);
        //mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        mainPanel.setLayout(new BorderLayout());

        /*--- main > video panel ---*/
        videoPlayerPanel = new VideoPlayerPanel(styleSelectorConfig.getStyleConfig());
        videoPlayerPanel.setFrameRate(10);
        videoPlayerPanel.setVideoPath(videoLogRecord.getVideoFilePath());
        videoPlayerPanel.Initialize();

        /*--- main > log panel ---*/
        logTextPane = new HighlightableJTextPane();
        logTextPane.setWrapText(false);
        setupLogTextPane(videoLogRecord.getLogFilePath());
        JPanel noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.add(logTextPane);
        //JScrollPane logSP = new JScrollPane(logTextPane);//, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        logSP = new JScrollPane(noWrapPanel);
        logSP.setPreferredSize(new Dimension(400, 480));
        mainSplitPane.setLeftComponent(videoPlayerPanel);
        mainSplitPane.setRightComponent(logSP);
        mainPanel.add(mainSplitPane);

        this.getContentPane().add(mainPanel);
        this.pack();

        this.addWindowListener(new PlayerFrameClosedListener());
        videoPlayerPanel.addVideoPlayerListener(new LogPanelUpdateListener());
        System.out.println("logTextPane width = " + logTextPane.getWidth());
        System.out.println("logTextPane pf width = " + logTextPane.getPreferredSize().getWidth());
    }

    public static final int TIMESTAMP_LOG_PREFIX_SIZE = 16;
    public Vector<Long> timeStampLineVec; // index: line, value: timestamp
    private void setupLogTextPane(String logFilePath) {
        //logTextPane.setText("test string\n");
        try {
            String inputStr = MyUtils.readFromFile(logFilePath);
            Scanner scanner = new Scanner(inputStr);
            String outStr = "";
            timeStampLineVec = new Vector<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.length() >= TIMESTAMP_LOG_PREFIX_SIZE) {
                    //String parsedLine = removeOneLineComment(line);
                    String timeStr = line.substring(1, 14);
                    long timestamp = Long.parseLong(timeStr);
                    timeStampLineVec.add(timestamp);
                    String logStr = line.substring(TIMESTAMP_LOG_PREFIX_SIZE);
                    outStr += logStr + "\n";
                    //System.out.println(line + " processed! -> " + "time = " + timestamp + ", log = " + logStr);
                } else {
                    System.out.println("[ERROR] Unexpected line size: size is too small, line = " + line);
                }
            }
            scanner.close();
            logTextPane.setText(outStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class LogPanelUpdateListener implements VideoPlayerListener {
        private int prevline = 0;

        @Override
        public void onPlayStateChanged(int playState) {

        }

        @Override
        public void adjustValueChanged(AdjustmentEvent e) {

        }

        @Override
        public void onPreUpdateUI() {
            long ms = videoPlayerPanel.getCurrentTimeInMillisec() + videoStartTime;

            int line = lower_bound(timeStampLineVec, ms);
            for (int i = prevline; i <= line; i++) {
                logTextPane.addHightlightLine(i);
            }
            if (prevline < line + 1) prevline = line+1;
            if (autoScroll) { // Scroll to line
                try {
                    BoundedRangeModel brm = logSP.getVerticalScrollBar().getModel();
                    Rectangle rect = logTextPane.modelToView(logTextPane.getLineStartOffset(line));
                    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, logTextPane);
                    brm.setValue(rect.y - c.getHeight() / 2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            logTextPane.repaint();
            System.out.println("onPreUpdateUI: " + "ms = " + ms + ", line = " + line);
        }

        @Override
        public void onPostUpdateUI() {

        }
    }

    public static <T> int lower_bound(Vector<T> vec, T key) {
        int len = vec.size();
        int lb = 0;
        int ub = len-1;
        //int mid = (lo + hi)/2;
        int mid;
        if (vec == null) System.out.println("vector is null");
        while (ub-lb > 1) {
            mid = (lb + ub)/2;
            if (vec.get(mid) == null) System.out.println("vector(mid) is null, mid = " + mid + ", len = " + len);
            if ((Long)vec.get(mid) < (Long)key) { // TODO: now T must be Long or Integer...
                lb = mid;
            } else {
                ub = mid;
            }
        }
        return lb;
    }


    public void showFrame() {
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void Finalize() {
        if (videoPlayerPanel != null) videoPlayerPanel.pause();
    }

    /*--- INNER CLASS ---*/
    private class PlayerFrameClosedListener extends WindowAdapter {
        /** For finalize, called when user press X button on top-right */
        @Override
        public void windowClosing(WindowEvent e) {
            System.out.println("player window closing");
            Finalize();
        }

        /** For finalize, called when dispose() method is called */
        @Override
        public void windowClosed(WindowEvent e) {
            System.out.println("player window closed");
            Finalize();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        switch (action) {
            default:
                System.out.println("action " + action + " not handled");
                break;
        }
    }

    public static void main(String args[]) {
        final VideoLogRecord videoLogRecord = VideoLogRecord.load(XML_PATH);
        if (videoLogRecord == null) {
            System.out.println("videoLogRecord is null with path = " + XML_PATH);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        new PlayerFrame(videoLogRecord).showFrame();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

/*
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    setupPlayerFrame();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
*/
    }


    private static void setupPlayerFrame() {
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


    private static boolean checkFile(File file) {
        if (file.exists()) {
            if (file.isFile() && file.canRead()) {
                return true;
            }
        }
        return false;
    }
}
