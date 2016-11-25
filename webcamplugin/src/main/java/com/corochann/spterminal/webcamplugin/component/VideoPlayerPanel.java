package com.corochann.spterminal.webcamplugin.component;

import com.corochann.spterminal.config.style.StyleConfig;
import com.corochann.spterminal.ui.component.CustomJButton;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.util.Vector;

/**
 * Video player panel using JavaCV.
 * styleConfig can be set to null if style setup unnecessary.
 */
public class VideoPlayerPanel extends JPanel implements ActionListener {

    /*--- Constants ---*/
    // Action definition
    private static final String ACTION_PLAY = "play";
    private static final String ACTION_PAUSE = "pause";

    // State definition
    /** Initilization is necessary before play */
    public static final int STATE_NOT_INITIALIZED = 0;
    /** play */
    public static final int STATE_PLAYING  = 1;
    /** pause / it may be after init but not play yet */
    public static final int STATE_PAUSE = 2;

    /*--- Attribute ---*/
    private Vector<VideoPlayerListener> videoPlayerListenerVector = new Vector<>();

    private CanvasPanel videoPanel;

    private int playState = STATE_NOT_INITIALIZED;
    private String videoPath;
    private static double frameRate = 10;
    private CustomJButton playPauseButton;
    private FFmpegFrameGrabber frameGrabber;

    private PlayVideoFrameThread playThread = null;
    private JLabel currentTimeLabel;
    private JLabel totalTimeLabel;
    private JScrollBar playScrollBar;

    /*--- Constructor ---*/
    public VideoPlayerPanel(StyleConfig styleConfig) {
        super();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        /* 1st row: video player */

        System.out.println("videoplayerpanel gc = " + this.getGraphicsConfiguration());

        videoPanel = new CanvasPanel();
        GraphicsConfiguration gc =  videoPanel.getGraphicsConfiguration();
        System.out.println("videopanel gc = " + gc);
        videoPanel.setPreferredSize(new Dimension(640, 480));//VGA

        /* 2nd row: video controller */
        JPanel videoControllerPanel = new JPanel();
        videoControllerPanel.setLayout(new BoxLayout(videoControllerPanel, BoxLayout.X_AXIS));

        playPauseButton = new CustomJButton(styleConfig);
        playPauseButton.addActionListener(this);

        playScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        playScrollBar.setValues(0, 0, 0, 100);  // 2nd arg: extent is related to knob size.

        playScrollBar.setUI(new BasicScrollBarUI() {
            // Ref: http://stackoverflow.com/questions/7633354/how-to-hide-the-arrow-buttons-in-a-jscrollbar
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0, 0));
                jbutton.setMinimumSize(new Dimension(0, 0));
                jbutton.setMaximumSize(new Dimension(0, 0));
                return jbutton;
            }

            @Override
            protected Dimension getMinimumThumbSize() {
                // Default value is (8, 8)
                Dimension dim = super.getMinimumThumbSize();
                //System.out.println("minimum thumb size = " + dim.getWidth() + ", h = " + dim.getHeight());
                return new Dimension(12, 12);
            }

            @Override
            protected Dimension getMaximumThumbSize() {
                // Default value is (4096, 4096)
                //Dimension dim = super.getMaximumThumbSize();
                //System.out.println("maximum thumb size = " + dim.getWidth() + ", h = " + dim.getHeight());
                return new Dimension(16, 16);
            }
        });
        playScrollBar.addAdjustmentListener(new AdjustmentListener() {
            boolean previousValueIsAdjusting = false;
            boolean wasPlaying = false;
            @Override
            public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
                int value = e.getValue();
                System.out.println("value = " + value + ", type = "+ e.getAdjustmentType()
                        + ", id = " + e.getID()+ ", source = " + e.getSource() + "e.getValueIsAdjusting() = " + e.getValueIsAdjusting());
                if (!previousValueIsAdjusting && e.getValueIsAdjusting()) { // when drag & drop knob starts
                    wasPlaying = (playState == STATE_PLAYING);
                    if (wasPlaying) pause();
                } else if (previousValueIsAdjusting && !e.getValueIsAdjusting()) { // when drag & drop knob ends
                    System.out.println("frame# = "+ frameGrabber.getFrameNumber());
                    try {
                        frameGrabber.setFrameNumber(value);
                        Frame frame = frameGrabber.grab();
                        if (frame!= null) updateUI(frame);
                        frameGrabber.setFrameNumber(value);
                        System.out.println("after set " + "value = " + value + "frame# = "+ frameGrabber.getFrameNumber());
                        if (wasPlaying) start();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                previousValueIsAdjusting = e.getValueIsAdjusting();
                for (VideoPlayerListener listener : videoPlayerListenerVector) {
                    listener.adjustValueChanged(e);
                }
            }
        });


        currentTimeLabel = new JLabel();
        JLabel separatorLabel = new JLabel("/");
        totalTimeLabel = new JLabel();

        videoControllerPanel.add(playPauseButton);
        videoControllerPanel.add(playScrollBar);
        videoControllerPanel.add(currentTimeLabel);
        videoControllerPanel.add(separatorLabel);
        videoControllerPanel.add(totalTimeLabel);

        add(videoPanel);
        add(videoControllerPanel);

        updatePlayState(STATE_NOT_INITIALIZED);
    }

    public VideoPlayerPanel(String videoFilePath, StyleConfig styleConfig) {
        this(styleConfig);
        setVideoPath(videoFilePath);
    }

    /** start video */
    public void start() {
        if (playThread == null) {
            playThread = new PlayVideoFrameThread();
            playThread.start();
            updatePlayState(STATE_PLAYING);
        } else {
            System.out.println("playThread start is called but it is not null, maybe already running");
        }
    }

    /** start video */
    public void pause() {
        if (playThread == null) {
            System.out.println("playThread already paused");
            updatePlayState(STATE_PAUSE);
        } else {
            playThread.pause();
            updatePlayState(STATE_PAUSE);
            playThread = null;
        }
    }

    public void updatePlayState(int state) {
        System.out.println("updatePlayState playstate = " + state);
        switch (state) {
            case STATE_NOT_INITIALIZED:
                playPauseButton.setText("play");
                playPauseButton.setActionCommand(ACTION_PLAY);
                playPauseButton.setEnabled(false);
                playState = state;
                break;
            case STATE_PLAYING:
                playPauseButton.setText("pause");
                playPauseButton.setActionCommand(ACTION_PAUSE);
                playPauseButton.setEnabled(true);
                playState = state;
                break;
            case STATE_PAUSE:
                playPauseButton.setText("play");
                playPauseButton.setActionCommand(ACTION_PLAY);
                playPauseButton.setEnabled(true);
                playState = state;
                break;
            default:
                System.out.println("[Error] unhandled playstate = " + state);
        }
        // call listener
        for (VideoPlayerListener listener : videoPlayerListenerVector) {
            listener.onPlayStateChanged(playState);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        switch (action) {
            case ACTION_PLAY:
                start();
                break;
            case ACTION_PAUSE:
                pause();
                break;
            default:
                break;
        }
    }


    /*--- Getter & Setter ---*/
    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public static double getFrameRate() {
        return frameRate;
    }

    public static void setFrameRate(double frameRate) {
        VideoPlayerPanel.frameRate = frameRate;
    }

    /**
     *
     * @return true if init success, false when some parameter error happens.
     */
    public boolean Initialize() {
        System.out.println("VideoPlayerPanel Initialize...");
        if (initialSetup()) {
            updatePlayState(STATE_PAUSE);
            return true;
        } else {
            updatePlayState(STATE_NOT_INITIALIZED);
            return false;
        }
    }

    public static String formatTime(long millisec) {
        String text = "";
        long totalSec = millisec / 1000;
        long sec = totalSec % 60;
        long totalMin = totalSec / 60;
        long min = totalMin % 60;
        long hour = min / 60;
        if (hour > 0) {
            text = String.format("%d:%02d:%02d", hour, min, sec);
        } else {
            text = String.format("%d:%02d", min, sec);
        }
        return text;
    }

    private boolean initialSetup() {
        if (videoPath != null) {
            File file = new File(videoPath);
            if (file.exists()) {
                frameGrabber = new FFmpegFrameGrabber(file.getAbsolutePath());
                try {
                    Frame captured_frame = null;
                    frameGrabber.start();
                    // Initial size setting
                    captured_frame = frameGrabber.grab();
                    if (captured_frame != null) {
                        videoPanel.setPreferredSize(new Dimension(captured_frame.imageWidth, captured_frame.imageHeight));
                        frameGrabber.setFrameNumber(0);
                        totalTimeLabel.setText(formatTime(frameGrabber.getLengthInTime()/1000));
                        currentTimeLabel.setText(formatTime(0));
                        return true;
                    } else {
                        System.out.println("[Error] captured_frame is null");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("[Error] file" + videoPath + " not found!");
            }
        } else {
            System.out.println("[Error] video path not set!");
        }
        return false;
    }

    private static long getWaitMillis(long startTime, long frame) {
        return 1000 * frame / (long)frameRate - (System.currentTimeMillis() - startTime);
    }

    /*--- INNER CLASS ---*/
    public class PlayVideoFrameThread extends Thread  {
        private boolean playing = true;

        @Override
        public void run() {
            play();
            synchronized (this) {
                notify();
            }
        }

        public synchronized void pause() {
            playing = false;
            try {
                wait(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void play() {
            playScrollBar.setValues(frameGrabber.getFrameNumber(), 0, 0, frameGrabber.getLengthInFrames());
            Frame captured_frame = null;
            try {
                long frame = 0;
                long startTime = System.currentTimeMillis();
                while (playing) {
                    try {
                        captured_frame = frameGrabber.grab();

                        //System.out.println("len in frame = " + frameGrabber.getLengthInFrames() +
                        //        ", len in time = " + frameGrabber.getLengthInTime() +
                        //        ", delayed time = " + frameGrabber.getDelayedTime()
                        //);
                        if (captured_frame == null) {
                            System.out.println("cvQueryFrame Failed (Maybe end of video)");
                            // TODO: review. is this behavior ok?
                            frameGrabber.setFrameNumber(0);
                            VideoPlayerPanel.this.pause();
                            updateUI(frameGrabber.grab());
                            frameGrabber.setFrameNumber(0);
                            //updatePlayState(STATE_PAUSE);
                            return; //TODO: review. ok to return?
                        }
                        updateUI(captured_frame);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    frame++;
                    long waitMillis = getWaitMillis(startTime, frame);
                    while (playing && waitMillis <= 0) {
                        // If this error appeared, better to consider lower FRAME_RATE.
                        System.out.println("[ERROR] frameGrabber.grab() or canvas.showImage() is too slow to encode, skip grab image at frame = " + frame + ", waitMillis = " + waitMillis);
                        captured_frame = frameGrabber.grab();
                        frame++;
                        waitMillis = getWaitMillis(startTime, frame);
                    }
                    System.out.println("frame " + frame + ", System.currentTimeMillis() = " + System.currentTimeMillis() + ", waitMillis = " + waitMillis);
                    if (waitMillis > 0) Thread.sleep(waitMillis);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUI(Frame captured_frame) {
        for (VideoPlayerListener listener : videoPlayerListenerVector) {
            listener.onPreUpdateUI();
        }
        videoPanel.showImage(captured_frame);
        currentTimeLabel.setText(formatTime(
                getCurrentTimeInMillisec()
        ));// current time in millisec.
        //playScrollBar.setValueIsAdjusting(true);
        playScrollBar.setValue(frameGrabber.getFrameNumber());
        //playScrollBar.setValueIsAdjusting(false);
        for (VideoPlayerListener listener : videoPlayerListenerVector) {
            listener.onPostUpdateUI();
        }
    }

    public long getCurrentTimeInMillisec() {
        return frameGrabber.getLengthInTime() * frameGrabber.getFrameNumber() / frameGrabber.getLengthInFrames() / 1000;
    }

    public synchronized int addVideoPlayerListener(VideoPlayerListener listener) {
        int index = videoPlayerListenerVector.size();
        videoPlayerListenerVector.add(listener);
        return index;
    }

    public synchronized void removeVideoPlayerListener(int index) {
        videoPlayerListenerVector.remove(index);
    }

    public synchronized void removeVideoPlayerListener() {
        videoPlayerListenerVector.removeAllElements();
    }
}
