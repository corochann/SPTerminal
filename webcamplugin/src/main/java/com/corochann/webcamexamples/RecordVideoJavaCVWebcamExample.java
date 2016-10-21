package com.corochann.webcamexamples;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder.Exception;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 *
 */
public class RecordVideoJavaCVWebcamExample {


    private static final int FRAME_RATE = 30;
    public static int FPS_INTERVAL_MILLIS = 1000 / FRAME_RATE;
    public static int RECORD_SEC = 5;

    private static final int WEBCAM_DEVICE_INDEX = 0;
    //private static final int CAPTUREWIDTH = 600;
    //private static final int CAPTUREHRIGHT = 600;
    private static final int CAPTUREWIDTH = 640;
    private static final int CAPTUREHRIGHT = 480;
    private static final int GOP_LENGTH_IN_FRAMES = 60;

    private static FFmpegFrameRecorder recorder = null;
    //private static OpenCVFrameGrabber grabber = null;
    private volatile boolean runnable = true;
    private static final long serialVersionUID = 1L;
    private Catcher cat;
    private Thread catcher;
    private final Webcam webcam;

    public RecordVideoJavaCVWebcamExample() {
        // Webcam setup
        Dimension size = WebcamResolution.VGA.getSize();

        webcam = Webcam.getDefault();
        webcam.setViewSize(size);
        webcam.open(true);

        // JavaCV recorder setup
        //grabber = new OpenCVFrameGrabber(WEBCAM_DEVICE_INDEX);
        cat = new Catcher();
        catcher = new Thread(cat);
        catcher.start();
    }

    class Catcher implements Runnable {
        @Override
        public void run() {
                // while (runnable) {
                try {
                    //grabber.setImageWidth(CAPTUREWIDTH);
                    //grabber.setImageHeight(CAPTUREHRIGHT);
                    //grabber.start();
                    recorder = new FFmpegFrameRecorder(
                            "webcam/webcam-javacv.mp4",
                            CAPTUREWIDTH, CAPTUREHRIGHT, 2);
                    recorder.setInterleaved(true);
                    // video options //
                    recorder.setVideoOption("tune", "zerolatency");
                    recorder.setVideoOption("preset", "ultrafast");
                    recorder.setVideoOption("crf", "28");
                    recorder.setVideoBitrate(2000000);
                    recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                    recorder.setFormat("mp4");
                    recorder.setFrameRate(FRAME_RATE);
                    recorder.setGopSize(GOP_LENGTH_IN_FRAMES);
                    System.out.println("current format = "+ recorder.getPixelFormat());
                    //recorder.setPixelFormat(avutil.AV_PIX_FMT_ARGB);
                    // audio options //
                    recorder.setAudioOption("crf", "0");
                    recorder.setAudioQuality(0);
                    recorder.setAudioBitrate(192000);
                    recorder.setSampleRate(44100);
                    recorder.setAudioChannels(2);
                    recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);

                    recorder.start();

                    Frame capturedFrame = null;
                    Java2DFrameConverter paintConverter = new Java2DFrameConverter();

                    /* Performance test */
                    final long beginTime = System.currentTimeMillis();
                    for (int i = 0; i < 100; i++) {
                        long time1 = System.currentTimeMillis();
                        BufferedImage image = webcam.getImage(); // Obtain an image to encode
                        long time2 = System.currentTimeMillis();
                        File file = new File(String.format("webcam/webcam-%d.jpg", i));
                        ImageIO.write(image, "JPG", file);
                        long time3 = System.currentTimeMillis();
                        System.out.println("time12 = " + (time2 - time1) + ", time23 = " + (time3 - time2));
                    }
                    final long endTime = System.currentTimeMillis();
                    System.out.println("100 picture save took " + (endTime - beginTime) + " ms");
                    /* Performance test end */


                    final long startTime = System.currentTimeMillis();
                    final int frameNum = RECORD_SEC * 1000 / FPS_INTERVAL_MILLIS;
                    long frame = 0;
                    //while ((capturedFrame = grabber.grab()) != null && runnable && frame < frameNum) {
                    while (runnable && frame < frameNum) {
                        //BufferedImage buff = paintConverter.getBufferedImage(capturedFrame, 1);
                        //recorder.record(getFrame(paintConverter, frame), avutil.AV_PIX_FMT_RGB8);
                        recorder.record(getFrame(paintConverter, frame), avutil.AV_PIX_FMT_RGB24);
                        //Frame f = getFrame(paintConverter, frame);
                        //System.out.println("w = " + f.imageWidth + ", h = " +  f.imageHeight
                        //                + ", d = " + f.imageDepth + ", ch = " + f.imageChannels
                        //                + ", st = " + f.imageStride
                        //);
                        frame++;
                        long waitMillis = 1000 * frame / FRAME_RATE - (System.currentTimeMillis() - startTime);
                        while (waitMillis <= 0 && frame < frameNum) {
                            // If this error appeared, better to consider lower FRAME_RATE.
                            System.out.println("[ERROR] getFrame operation is too slow to encode, skip grab image at frame = " + frame + ", waitMillis = " + waitMillis);
                            recorder.record(getFrame(paintConverter, frame));  // use same capturedFrame for fast processing.
                            frame++;
                            waitMillis = 1000 * frame / FRAME_RATE - (System.currentTimeMillis() - startTime);
                        }
                        System.out.println("frame " + frame + ", System.currentTimeMillis() = " + System.currentTimeMillis() + ", waitMillis = " + waitMillis);
                        Thread.sleep(waitMillis);
                    }
                    recorder.stop();
                    //grabber.stop();
                    runnable = false;
                } catch (InterruptedException | IOException | Exception ex) {
                    ex.printStackTrace();
                }

                //}//end of while
            System.out.println("Catcher exit");
        }
    }

    private Frame getFrame(Java2DFrameConverter paintConverter, long frame) throws IOException {
        Frame capturedFrame;
        BufferedImage image = webcam.getImage(); // Obtain an image to encode
        //File file = new File(String.format("webcam/test-%d.jpg", frame));
        //ImageIO.write(image, "JPG", file);
        capturedFrame = paintConverter.getFrame(image);
        return capturedFrame;
    }

    public static void main(String[] args) {
        new RecordVideoJavaCVWebcamExample();
        System.out.println("main exit");
    }

}
