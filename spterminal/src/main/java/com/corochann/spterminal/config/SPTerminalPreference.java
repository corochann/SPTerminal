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
    private LayoutConfig mLayoutConfig = null;
    private StyleSelectorConfig mStyleSelectorConfig = null;
    private TTLMacroConfig mTTLMacroConfig = null;
    private FilterConfig mFilterConfig = null;

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
        savePreference(); // Should we save anytime? or save should be done separately??
        uniqueInstance = null;
    }

    public SerialPortConfig getSerialPortConfig() {
        return mSerialPortConfig;
    }

    public LogConfig getLogConfig() {
        return mLogConfig;
    }

    public LayoutConfig getLayoutConfig() {
        return mLayoutConfig;
    }

    public StyleSelectorConfig getStyleSelectorConfig() {
        return mStyleSelectorConfig;
    }

    public TTLMacroConfig getTTLMacroConfig() {
        return mTTLMacroConfig;
    }

    public FilterConfig getFilterConfig() {
        return mFilterConfig;
    }

    /**
     * Loads all preferences (specified in Attributes).
     */
    public synchronized void loadPreference() {
        mSerialPortConfig = SerialPortConfig.load();
        mLogConfig = LogConfig.load();
        mLayoutConfig = LayoutConfig.load();
        mStyleSelectorConfig = StyleSelectorConfig.load();
        mTTLMacroConfig = TTLMacroConfig.load();
        mFilterConfig = FilterConfig.load();
    }

    /**
     * Saves all preferences.
     */
    public synchronized void savePreference() {
        mSerialPortConfig.save();
        mLogConfig.save();
        mLayoutConfig.save();
        mStyleSelectorConfig.save();
        // mTTLMacroConfig save is done by each TTLMacro independently
        // mFilterConfig save is done by each FilterRule independently
    }
}
