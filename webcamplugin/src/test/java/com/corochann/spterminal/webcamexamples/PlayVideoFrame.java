package com.corochann.spterminal.webcamexamples;

import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.style.StyleSelectorConfig;
import com.corochann.spterminal.ui.component.CustomJButton;
import com.corochann.spterminal.ui.component.HighlightableJTextPane;
import com.corochann.spterminal.webcamplugin.component.CanvasPanel;
import com.corochann.spterminal.webcamplugin.component.VideoPlayerPanel;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 *
 */
public class PlayVideoFrame extends JFrame implements ActionListener {

    public static final String VIDEO_PATH = "D://workspace/intellij/SPTerminal/spterminal/log/record_20161031_115057/video.mp4";
    final static int INTERVAL=40;///you may use interval
    //IplImage image;
    //static CanvasFrame canvas = new CanvasFrame("JavaCV player", 1);


    public PlayVideoFrame() {
        StyleSelectorConfig styleSelectorConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig();

        //canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

        //canvas.setLayout(new MigLayout("insets 0, gap 10", "grow", "grow"));
        //canvas.getContentPane().add(playButton);
        //canvas.pack();

        /*--- Create components ---*/
        JPanel mainPanel = new JPanel();
        //mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        mainPanel.setLayout(new BorderLayout());

        /*--- main > video panel ---*/
        VideoPlayerPanel videoPlayerPanel = new VideoPlayerPanel(styleSelectorConfig.getStyleConfig());
        videoPlayerPanel.setFrameRate(10);
        videoPlayerPanel.setVideoPath(VIDEO_PATH);
        videoPlayerPanel.Initialize();

        /*--- main > log panel ---*/
        JPanel logPanel = new JPanel();
        HighlightableJTextPane logTextPane = new HighlightableJTextPane();
        logTextPane.setText("test string\naaa");
        logPanel.add(logTextPane);

        mainPanel.add(videoPlayerPanel, BorderLayout.CENTER);
        //mainPanel.add(videoPanel, BorderLayout.CENTER);
        //mainPanel.add(logPanel, BorderLayout.EAST);

        this.getContentPane().add(mainPanel);
        this.pack();
    }


    public void showFrame() {
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new PlayVideoFrame().showFrame();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
