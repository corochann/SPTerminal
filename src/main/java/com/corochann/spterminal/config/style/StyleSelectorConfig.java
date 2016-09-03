package com.corochann.spterminal.config.style;

import com.corochann.spterminal.config.ProjectConfig;
import com.corochann.spterminal.util.MyUtils;

import java.io.FileNotFoundException;

/**
 * Style config selector, this is preference class,
 * It decides which {@link StyleConfig} to be used.
 */
public class StyleSelectorConfig {
    /*--- Attributes ---*/
    private String styleName = StyleConfig.STYLE_DEFAULT;
    private StyleConfig styleConfig = StyleConfig.load(styleName);

    /*--- Save & Load ---*/
    /**
     * save this preference
     */
    public synchronized void save() {
        String path = ProjectConfig.STYLE_SELECTOR_CONFIG_XML_PATH;
        System.out.println("Saving SerialPortConfig to " + path);
        try {
            MyUtils.writeToXML(this, path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * load this preference
     */
    public static synchronized StyleSelectorConfig load() {
        String path = ProjectConfig.STYLE_SELECTOR_CONFIG_XML_PATH;
        StyleSelectorConfig styleSelectorConfig;
        try {
            styleSelectorConfig = (StyleSelectorConfig) MyUtils.readFromXML(path);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println(path + " not found, default value will be used.");
            styleSelectorConfig = new StyleSelectorConfig();
        }
        return styleSelectorConfig;
    }

    /*--- Getter & Setter ---*/
    public String getStyleName() {
        return styleName;
    }

    /**
     * styleConfig is automatically updated together when styleName is updated.
     * @param styleName
     */
    public void setStyleName(String styleName) {
        this.styleName = styleName;
        setStyleConfig(StyleConfig.load(styleName));
    }

    public StyleConfig getStyleConfig() {
        return styleConfig;
    }

    /**
     * It is not expected to be called from outside package,
     * since {@link #setStyleName} will update styleConfig as well.
     * But it must be public so that java.beans.XMLEncoder/Decoder library work correctly.
     * @param styleConfig
     */
    public void setStyleConfig(StyleConfig styleConfig) {
        this.styleConfig = styleConfig;
    }
}
