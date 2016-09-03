package com.corochann.spterminal.teraterm;

import org.junit.Test;

/**
 * Test for TTLMacro class
 */
public class TTLMacroTest {

    @Test(expected = TTLMacro.FormatErrorException.class)
    public void testSaveNullFileName() throws TTLMacro.FormatErrorException {
        TTLMacro ttlMacro = new TTLMacro();
        ttlMacro.setFileName(null);
        ttlMacro.save();
    }

    @Test(expected = TTLMacro.FormatErrorException.class)
    public void testSaveEmptyFileName() throws TTLMacro.FormatErrorException {
        TTLMacro ttlMacro = new TTLMacro();
        ttlMacro.setFileName("");
        ttlMacro.save();
    }

    @Test(expected = TTLMacro.FormatErrorException.class)
    public void testSaveFileNameWithSpace() throws TTLMacro.FormatErrorException {
        TTLMacro ttlMacro = new TTLMacro();
        ttlMacro.setFileName("teraterm macro test");
        ttlMacro.save();
    }

    @Test(expected = TTLMacro.FormatErrorException.class)
    public void testSaveEmptyCommand() throws TTLMacro.FormatErrorException {
        TTLMacro ttlMacro = new TTLMacro();
        ttlMacro.setFileName("teraterm_macro_test");
        ttlMacro.setCommand("");
        ttlMacro.save();
    }

}
