package com.corochann.spterminal.webcamplugin.component;

import java.awt.event.AdjustmentEvent;

/**
 * Listener class for {@link VideoPlayerPanel}
 */
public interface VideoPlayerListener {
    void onPlayStateChanged(int playState);
    void adjustValueChanged(AdjustmentEvent e);
    void onPreUpdateUI();
    void onPostUpdateUI();
}
