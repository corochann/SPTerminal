package com.corochann.spterminal.config;

import com.corochann.spterminal.config.style.StyleSelectorConfig;
import com.corochann.spterminal.config.teraterm.TTLMacroConfig;

/**
 * Loads/Saves Project configuration.
 *
 */
public class SPTerminalPreference {

    /**
     * Singleton
     * This instance owns several preferences in attributes, which are stored in XML format.
     */
    private static SPTerminalPreference uniqueInstance = null;

    /*--- Attributes ---*/
    private SerialPortConfig mSerialPortConfig = null;
    private LogConfig mLogConfig = null;
    private StyleSelectorConfig mStyleSelectorConfig = null;
    private TTLMacroConfig mTTLMacroConfig = null;

    private SPTerminalPreference() {

    }

    public static synchronized SPTerminalPreference getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new SPTerminalPreference();
            uniqueInstance.Initialize();
        }
        return uniqueInstance;
    }

    /** Initialization */
    public void Initialize() {
        loadPreference();
    }

    /** Finalization */
    public void Finalize() {
        savePreference();
        uniqueInstance = null;
    }

    public SerialPortConfig getSerialPortConfig() {
        return mSerialPortConfig;
    }

    public LogConfig getLogConfig() {
        return mLogConfig;
    }

    public StyleSelectorConfig getStyleSelectorConfig() {
        return mStyleSelectorConfig;
    }

    public TTLMacroConfig getTTLMacroConfig() {
        return mTTLMacroConfig;
    }

    /**
     * Loads all preferences (specified in Attributes).
     */
    public synchronized void loadPreference() {
        mSerialPortConfig = SerialPortConfig.load();
        mLogConfig = LogConfig.load();
        mStyleSelectorConfig = StyleSelectorConfig.load();
        mTTLMacroConfig = TTLMacroConfig.load();
    }

    /**
     * Saves all preferences.
     */
    public synchronized void savePreference() {
        mSerialPortConfig.save();
        mLogConfig.save();
        mStyleSelectorConfig.save();
        // mSPTAliasListConfig save is done by each SPTAlias independently
    }
}
