package com.corochann.webcamexamples;

import com.github.sarxos.webcam.Webcam;

/**
 * Testing webcam-capture library.
 */
public class DetectWebcam {
    public static void main(String[] args) {
        Webcam webcam = Webcam.getDefault();
        if (webcam != null) {
            System.out.println("Webcam: " + webcam.getName());
        } else {
            System.out.println("No webcam detected");
        }
    }
}
