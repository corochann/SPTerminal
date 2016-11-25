package com.corochann.spterminal.webcamexamples;

import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.style.StyleSelectorConfig;
import com.corochann.spterminal.ui.component.CustomJButton;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;

/**
 *
 */
public class PlayVideo implements Runnable {

    public static final String VIDEO_PATH = "D://workspace/intellij/SPTerminal/spterminal/log/record_20161031_115057/video.mp4";

    final static int INTERVAL = 40;///you may use interval
    //IplImage image;
    static CanvasFrame canvas = new CanvasFrame("JavaCV player", 1);
    private static double frameRate;
    private static Boolean playing;

    public PlayVideo() {
        System.out.println("PlayVideo constructor");
        StyleSelectorConfig styleSelectorConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig();

        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

        CustomJButton playButton = new CustomJButton("play", styleSelectorConfig.getStyleConfig());

        //canvas.setLayout(new MigLayout("insets 0, gap 10", "grow", "grow"));
        //canvas.getContentPane().add(playButton);
        //canvas.pack();
    }


    public static void convert(File file) {
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(file.getAbsolutePath());

        Frame captured_frame = null;

        FrameRecorder recorder = null;
        //recorder = new FFmpegFrameRecorder("/mnt/sdcard/external_sd/videosteste/primeiroteste.mp4", 300, 300);
        //recorder = new FFmpegFrameRecorder("D://temp.mp4", 300, 300);
        //recorder.setVideoCodec(13);
        //recorder.setFrameRate(30);
        //recorder.setFormat("mp4");

        try {
            //recorder.start();
            frameGrabber.start();

            playing = true;
            long frame = 0;
            frameRate = 10;
            long startTime = System.currentTimeMillis();
            while (playing) {
                try {
                    captured_frame = frameGrabber.grab();

                    if (captured_frame == null) {
                        System.out.println("cvQueryFrame Failed (Maybe end of video)");
                        break;
                    }
                    //recorder.record(captured_frame);
                    canvas.showImage(captured_frame);
                } catch (Exception e) {
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

    private static long getWaitMillis(long startTime, long frame) {
        return 1000 * frame / (long)frameRate - (System.currentTimeMillis() - startTime);
    }

    @Override
    public void run() {
        convert(new File(VIDEO_PATH));
    }

    public static void main(String[] args) {
        PlayVideo gs = new PlayVideo();
        Thread th = new Thread(gs);
        th.start();
    }
}
