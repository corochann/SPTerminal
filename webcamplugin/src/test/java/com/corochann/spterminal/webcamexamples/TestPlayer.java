package com.corochann.spterminal.webcamexamples;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;

/**
 *
 */
public class TestPlayer {

    CanvasFrame videoCanvas;
    SourceDataLine audioLine;

    public TestPlayer() {
        videoCanvas = new CanvasFrame("VideoCanvas");
        videoCanvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }

    void play() throws Exception {

        //FrameGrabber grabber = new FFmpegFrameGrabber("mmsh://localhost:8080/");
        //grabber.setFormat("asf");
        //FrameGrabber grabber = new FFmpegFrameGrabber("test.mp4");
        FrameGrabber grabber = new FFmpegFrameGrabber("D://workspace/intellij/SPTerminal/spterminal/log/record_20161031_115057/video.mp4");

        grabber.start();

        System.out.println("sample rate = " + grabber.getSampleRate());
        //startAudioLine(grabber.getSampleRate());
        startAudioLine(10);

        try {
            Frame frame = grabber.grabFrame();
            while (frame!=null) {
                if(frame.image!=null){
                    //onFrameVideo();
                    videoCanvas.setCanvasSize(frame.imageWidth, frame.imageHeight);
                    videoCanvas.showImage(frame);
                }
                if(frame.samples!=null){
                    onFrameAudio(frame.samples);
                }
                frame = grabber.grabFrame();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        stopAudioLine();

        grabber.stop();
        grabber.release();
    }

    void onFrameVideo(opencv_core.IplImage iplImage){
        int w = iplImage.cvSize().width();
        int h = iplImage.cvSize().height();
        //videoCanvas.setCanvasSize(w, h);
        //videoCanvas.showImage(iplImage);
    }

    void onFrameAudio(Buffer[] buffer) throws Exception{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        float left = 0, right = 0;
        for (int i = 0; i < buffer[0].limit(); i++) {
            if(buffer.length==1){//mono
                left = ((FloatBuffer)buffer[0]).get();
                right = left;
            }
            if(buffer.length==2){//stereo
                left = ((FloatBuffer)buffer[0]).get();
                right = ((FloatBuffer)buffer[1]).get();
            }
            baos.write(float2shortBytes(left));
            baos.write(float2shortBytes(right));
        }
        audioLine.write(baos.toByteArray(), 0, baos.size());
    }

    void startAudioLine(int sampleRate) throws Exception{
        AudioFormat audioFormat = new AudioFormat(sampleRate, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        audioLine = (SourceDataLine) AudioSystem.getLine(info);
        audioLine.open(audioFormat);
        audioLine.start();
    }

    void stopAudioLine(){
        audioLine.drain();
        audioLine.stop();
        audioLine.close();
    }

    byte[] float2shortBytes(float f){
        int t = (int) (32768.0f * f);
        t = (t<-32768) ? -32768 : t;
        t = (t> 32767) ?  32767 : t;
        short s = (short) t;
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (s & 0xff);
        bytes[1] = (byte) ((s >>> 8) & 0xff);
        return bytes;
    }

    public static void main(String[] args) throws Exception {
        TestPlayer test = new TestPlayer();
        test.play();
    }

}
