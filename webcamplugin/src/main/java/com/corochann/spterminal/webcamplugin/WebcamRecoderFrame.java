package com.corochann.spterminal.webcamplugin;

import com.corochann.spterminal.config.ProjectConfig;
import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.style.StyleSelectorConfig;
import com.corochann.spterminal.webcamplugin.data.model.VideoLogRecord;
import com.corochann.spterminal.log.MyAnsiLogger;
import com.corochann.spterminal.serial.SerialPortRX;
import com.corochann.spterminal.ui.SPTerminal;
import com.corochann.spterminal.ui.component.CustomJButton;
import com.corochann.spterminal.util.MyUtils;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Webcam recoder/take screenshot dialog.
 * It is invoked from {@link SPTerminal}.
 */
public class WebcamRecoderFrame extends JFrame implements ActionListener {

    /*--- CONSTANTS ---*/
    public static final String SNAPSHOT_FOLDER_NAME = "snapshots";
    public static final String RECORD_LOG_FILENAME = "record_timestamp.log";
    public static final String VIDEO_FILENAME = "video.mp4";

    /*--- Action definitions ---*/
    private static final String ACTION_RECORD_START = "recordstart";
    private static final String ACTION_RECORD_STOP = "recordstop";
    private static final String ACTION_SNAPSHOT = "snapshot";
    private static final String ACTION_CANCEL = "cancel";

    /*--- Relations ---*/
    private Webcam webcam = null;

    /*--- Attribute ---*/
    private CustomJButton recordButton;
    private CustomJButton snapshotButton;
    private CustomJButton cancelButton;
    private WebcamVideoRecorderThread recorderThread;
    private MyAnsiLogger timestampLogger;
    private String recordDirPath;
    private WebcamPanel webcamPanel;

    public WebcamRecoderFrame() {
        super();
        this.setTitle("Webcam Recoder");
        StyleSelectorConfig styleSelectorConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig();

        /* webcam setup */
        webcam = Webcam.getDefault();
        if (webcam == null) {
            System.out.println("webcam not detected, skip init");
            return;
        }
        Dimension size = WebcamResolution.VGA.getSize();
        webcam.setViewSize(size);

        /*--- Create components ---*/
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));


        /* 1st row: webcamPanel */
        // false for showing frame faster
        webcamPanel = new WebcamPanel(webcam, size, false);
        webcamPanel.setFPSLimited(true);
        webcamPanel.setFPSLimit(5);
        webcamPanel.setFPSDisplayed(true);
        webcamPanel.setDisplayDebugInfo(true);
        webcamPanel.setImageSizeDisplayed(true);
        webcamPanel.setMirrored(false);

        /* 2nd row: control panel */
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        recordButton = new CustomJButton("Record", styleSelectorConfig.getStyleConfig());
        recordButton.setActionCommand(ACTION_RECORD_START);
        recordButton.setToolTipText("Start video recording");
        recordButton.addActionListener(this);

        snapshotButton = new CustomJButton("Snapshot", styleSelectorConfig.getStyleConfig());
        snapshotButton.setActionCommand(ACTION_SNAPSHOT);
        snapshotButton.addActionListener(this);
        snapshotButton.setToolTipText("Take snapshot.");

        cancelButton = new CustomJButton("Cancel", styleSelectorConfig.getStyleConfig());
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        cancelButton.setToolTipText("Cancel and exit.");

        JLabel notificationLabel = new JLabel("  * Supported only Windows-64 bit version");

        controlPanel.add(recordButton);
        controlPanel.add(snapshotButton);
        controlPanel.add(cancelButton);
        controlPanel.add(notificationLabel);

        mainPanel.add(webcamPanel);
        mainPanel.add(controlPanel);

        this.getContentPane().add(mainPanel);
        this.pack();

        //this.setFocusable(false);
        //this.setFocusableWindowState(false);
        this.addWindowListener(new WebcamWindowClosedListener());
    }


    private class WebcamWindowClosedListener extends WindowAdapter {
        /** For finalize, called when user press X button on top-right */
        @Override
        public void windowClosing(WindowEvent e) {
            System.out.println("webcam window closing");
            stopRecord();
            finalizeWebcam();
        }

        /** For finalize, called when dispose() method is called */
        @Override
        public void windowClosed(WindowEvent e) {
            System.out.println("webcam window closed");
            stopRecord();
            finalizeWebcam();
        }

        private void finalizeWebcam() {
            if (webcam != null) {
                System.out.println("webcam.close()...");
                webcam.close();
                webcam = null;
            }
        }
    }

    public void showFrame() {
        if (webcam == null) {  // show error dialog
            JOptionPane.showMessageDialog(SPTerminal.getFrame(), "Webcam not found!");
        } else {  // show frame
            setLocationRelativeTo(null);
            setVisible(true);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    webcamPanel.start();
                }
            });
        }
    }

    public synchronized void refreshRecoderThread() {
        if (recorderThread != null) {
            recorderThread.stopRecord();
            try { /* videoLogRecord must be saved before releasing recorderThread */
                VideoLogRecord videoLogRecord = new VideoLogRecord();
                //videoLogRecord.setVideoFilename(recorderThread.getVideoPath());
                videoLogRecord.setVideoFilename(VIDEO_FILENAME);
                videoLogRecord.setVideoStartTime(recorderThread.getStartTime());
                videoLogRecord.setLogFilename(RECORD_LOG_FILENAME);
                videoLogRecord.save(recordDirPath);
            } catch (VideoLogRecord.FormatErrorException e) {
                e.printStackTrace();
            }
            recorderThread = null;
        }
    }

    /** construct record directory according to the current time & create directory */
    public static String constructRecordDir() {
        SimpleDateFormat sdf = new SimpleDateFormat("'record'_yyyyMMdd_HHmmss");
        Calendar c = Calendar.getInstance();
        String recordDir = sdf.format(c.getTime());
        String recordDirPath = ProjectConfig.LOG_FOLDER + recordDir + "/";
        MyUtils.prepareDir(recordDirPath);
        return recordDirPath;
    }

    public synchronized void takeSnapshot() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Calendar c = Calendar.getInstance();
        String timeStr = sdf.format(c.getTime());
        String snapshotDirPath = ProjectConfig.LOG_FOLDER + SNAPSHOT_FOLDER_NAME + "/";
        MyUtils.prepareDir(snapshotDirPath);
        try {
            File file = new File(snapshotDirPath+"snapshot_"+timeStr+".jpg");
            ImageIO.write(webcam.getImage(), "JPG", file);
            System.out.format("Image for %s saved in %s \n", webcam.getName(), file);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public synchronized void startRecord() {
        refreshRecoderThread();

        recordDirPath = constructRecordDir();
        // take log
        //String portName = SPTerminal.getSerialPortManager().getCurrentPortName();

        SerialPortRX portRX = SPTerminal.getSerialPortManager().getPortRX();
        if (portRX != null) {
            String filePath = recordDirPath + RECORD_LOG_FILENAME;
            timestampLogger = new MyAnsiLogger(filePath, true);
            portRX.addBufferLogger(timestampLogger);
        } else {
            /* port is not connected, cannot take log.
             * Only take video record */
            //TODO: How to deal with this case?? defer now.
            System.out.println("[WARNING] port is not connected! Do not record log.");
        }
        // record video
        recorderThread = new WebcamVideoRecorderThread(webcam, recordDirPath);
        recorderThread.start();

        recordButton.setText("Stop");
        recordButton.setActionCommand(ACTION_RECORD_STOP);
        recordButton.setToolTipText("Stop video recording");
    }

    public synchronized void stopRecord() {
        refreshRecoderThread();
        if (timestampLogger != null) {
            timestampLogger.Finalize();
            timestampLogger = null;
        }

        recordButton.setText("Record");
        recordButton.setActionCommand(ACTION_RECORD_START);
        recordButton.setToolTipText("Start video recording");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        switch (action) {
            case ACTION_RECORD_START:
                System.out.println("Record pressed");
                startRecord();
                break;
            case ACTION_RECORD_STOP:
                System.out.println("Stop pressed");
                stopRecord();
                break;
            case ACTION_SNAPSHOT:
                System.out.println("Snapshot pressed");
                takeSnapshot();
                break;
            case ACTION_CANCEL:
                System.out.println("Cancel pressed");
                refreshRecoderThread();
                this.dispose();
                break;
        }
    }

    /**
     * Webcam video recoding thread
     * [usage]
     * - start recording
     * WebcamVideoRecorderThread thread = new WebcamVideoRecorderThread(webcam);
     * thread.start();
     *
     * - stop recording
     * thread.stopRecord();
     */
    public static class WebcamVideoRecorderThread extends Thread {
        private static final int GOP_LENGTH_IN_FRAMES = 60;
        private final double frameRate = 10;

        /*--- Relation ---*/
        private Webcam webcam = null;
        private FFmpegFrameRecorder recorder;

        /*--- Attributes ---*/
        private boolean runnable = true;

        private final String videoPath;
        private long startTime;

        WebcamVideoRecorderThread(Webcam webcam, String dirPath) {
            if (webcam == null) {
                //TODO: Error handling
                System.out.println("[ERROR] webcam is null");
            }
            this.webcam = webcam;

            double width = webcam.getViewSize().getWidth();
            double height = webcam.getViewSize().getHeight();
            videoPath = dirPath + VIDEO_FILENAME;
            // recorder setup
            recorder = new FFmpegFrameRecorder(videoPath, (int)width, (int)height, 2);
            recorder.setInterleaved(true);
            // video options //
            recorder.setVideoOption("tune", "zerolatency");
            recorder.setVideoOption("preset", "ultrafast");
            recorder.setVideoOption("crf", "28");
            recorder.setVideoBitrate(2000000);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            recorder.setFrameRate(frameRate);
            recorder.setGopSize(GOP_LENGTH_IN_FRAMES);
            //recorder.setPixelFormat(avutil.AV_PIX_FMT_ARGB);
            // audio options //
            recorder.setAudioOption("crf", "0");
            recorder.setAudioQuality(0);
            recorder.setAudioBitrate(192000);
            recorder.setSampleRate(44100);
            recorder.setAudioChannels(2);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        }

        @Override
        public void run() {
            try {
                recorder.start();
                Java2DFrameConverter paintConverter = new Java2DFrameConverter();

                startTime = System.currentTimeMillis();
                long frame = 0;
                while (runnable) {
                    org.bytedeco.javacv.Frame capturedFrame = paintConverter.getFrame(webcam.getImage());
                    recorder.record(capturedFrame, avutil.AV_PIX_FMT_RGB24);
                    frame++;
                    long waitMillis = getWaitMillis(startTime, frame);
                    while (runnable && waitMillis <= 0) {
                        // If this error appeared, better to consider lower FRAME_RATE.
                        System.out.println("[ERROR] getFrame operation is too slow to encode, skip grab image at frame = " + frame + ", waitMillis = " + waitMillis);
                        recorder.record(capturedFrame, avutil.AV_PIX_FMT_RGB24);  // use same image for fast processing.
                        frame++;
                        waitMillis = getWaitMillis(startTime, frame);
                    }
                    System.out.println("frame " + frame + ", System.currentTimeMillis() = " + System.currentTimeMillis() + ", waitMillis = " + waitMillis);
                    if (waitMillis > 0) Thread.sleep(waitMillis);
                }

                recorder.stop();
                System.out.println("Video successfully saved to " + videoPath);
                recorder.release();
            } catch (InterruptedException | FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }

        private long getWaitMillis(long startTime, long frame) {
            return 1000 * frame / (long)frameRate - (System.currentTimeMillis() - startTime);
        }

        public void stopRecord() {
            runnable = false;
        }

        public String getVideoPath() {
            return videoPath;
        }

        public long getStartTime() {
            return startTime;
        }
    }
}
