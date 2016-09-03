package com.corochann.spterminal.teraterm;

import com.corochann.spterminal.util.MyUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static com.corochann.spterminal.teraterm.TTLParser.TTLParam.TYPE_INTEGER;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;

/**
 * Test for TTL Macro parser
 */
public class TTLParserTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testRemoveComment() {
        //assertEquals("abc", TTLParser.removeComment("abc/*;*/"));
        //assertEquals("", TTLParser.removeComment("abc/*;\n */"));
        //assertEquals("c", TTLParser.removeComment("abc/*;\n */c"));
        //assertEquals("abc", TTLParser.removeComment("abc/*;*/c"));

        //assertEquals("c", TTLParser.removeComment("abc/*;\n */c"));
        // assertEquals("c", TTLParser.removeComment("abc/*;\n */c"));
    }


    @Test
    public void testRemoveOneLineComment() {
        assertEquals("", TTLParser.removeOneLineComment(";"));
        assertEquals("", TTLParser.removeOneLineComment(";a b e"));
        assertEquals(" a test ", TTLParser.removeOneLineComment(" a test ;"));
        assertEquals(" btest", TTLParser.removeOneLineComment(" btest; bbb"));
        assertEquals("c test ", TTLParser.removeOneLineComment("c test ;df c"));
        assertEquals(" d test ", TTLParser.removeOneLineComment(" d test ;  d ;fd   ; ; dfe "));
        assertEquals(" a test de", TTLParser.removeOneLineComment(" a test de"));
        assertEquals("a test", TTLParser.removeOneLineComment("a test"));
    }

    /* Learning test */
    @Test
    public void testSplitString() {
        /*
         * The behavior is easy to understand if you image delimiter as "," instead of " "
         * Ex. ",,abc,,def,ghi,,,"
         */
        String string = "  abc  def ghi   ";
        String[] splitStr = string.split(" ");
        System.out.println("0: " + splitStr[0]
                + ", 1: " + splitStr[1]
                + ", 2: " + splitStr[2]
                + ", 3: " + splitStr[3]
                + ", 4: " + splitStr[4]
                + ", 5: " + splitStr[5]
        );
        assertEquals("", TTLParser.removeOneLineComment(splitStr[0]));
        assertEquals("", TTLParser.removeOneLineComment(splitStr[1]));
        assertEquals("abc", TTLParser.removeOneLineComment(splitStr[2]));
        assertEquals("", TTLParser.removeOneLineComment(splitStr[3]));
        assertEquals("def", TTLParser.removeOneLineComment(splitStr[4]));
        assertEquals("ghi", TTLParser.removeOneLineComment(splitStr[5]));
        expectedException.expect(ArrayIndexOutOfBoundsException.class);
        System.out.println("6: " + splitStr[6]);

    }

    @Test
    public void testExtractArgs() {
        String[] args = {"sendln", "\"hello", "world!\""};
        assertArrayEquals(args, TTLParser.extractArgs("sendln  \"hello   world!\"   "));

        args = new String[]{"mpause", "3000"};
        assertArrayEquals(args, TTLParser.extractArgs("mpause   3000  "));
    }

    @Test
    public void testExtractArgsWithType() {
        /* TEST 1: understand string \" \" literal */
        List<TTLParser.TTLParam> argsList;
        argsList = TTLParser.extractArgsWithType("sendln  \"hello world!\"   ");
        // DEBUG
        //for (TTLParser.TTLParam ttlParam : argsList) {
        //    System.out.println("type = " + ttlParam.type + ", param " + ttlParam.param);
        //}
        assertEquals("sendln", argsList.get(0).param);
        assertEquals("hello world!", argsList.get(1).param);

        /* TEST 2: understand string \' \' literal */
        argsList = TTLParser.extractArgsWithType("sendln  'test \"  aa'");
        assertEquals("sendln", argsList.get(0).param);
        assertEquals("test \"  aa", argsList.get(1).param);

        /* TEST 3: Check space handling at the end of line */
        argsList = TTLParser.extractArgsWithType("mpause   3000  ");
        assertEquals("mpause", argsList.get(0).param);
        assertEquals("3000", argsList.get(1).param);

        /* TEST 4 */
        argsList = TTLParser.extractArgsWithType("mpause 1000  ");
        assertEquals("mpause", argsList.get(0).param);
        assertEquals("1000", argsList.get(1).param);

        /* TEST 5: Check no space at the end of line */
        argsList = TTLParser.extractArgsWithType("mpause 1000");
        assertEquals("mpause", argsList.get(0).param);
        assertEquals("1000", argsList.get(1).param);

        /* TEST 6: multiple string */
        argsList = TTLParser.extractArgsWithType("sendln  'test \"  aa'  \"second' string 123\"");
        assertEquals("sendln", argsList.get(0).param);
        assertEquals("test \"  aa", argsList.get(1).param);
        assertEquals("second' string 123", argsList.get(2).param);

        /* TEST 7: consective string  */
        argsList = TTLParser.extractArgsWithType("sendln  'test \"  aa'\"second' string 123\"");
        assertEquals("sendln", argsList.get(0).param);
        assertEquals("test \"  aasecond' string 123", argsList.get(1).param);

        /* TEST 8: empty string  */
        argsList = TTLParser.extractArgsWithType("sendln  ''");
        assertEquals("sendln", argsList.get(0).param);
        assertEquals("", argsList.get(1).param);
    }

    @Test
    public void testAsciiConversion() {
        // Ref https://ttssh2.osdn.jp/manual/ja/macro/syntax/formats.html
        // ASCII spec -> 2.b
        char c = (char)1;
        assertEquals("\001", ""+c);

        /* TEST 1: check #number */
        System.out.println("ASCII Test 1");
        List<TTLParser.TTLParam> argsList;
        argsList = TTLParser.extractArgsWithType("sendln  #3 #05");
        assertEquals("sendln", argsList.get(0).param);
        assertEquals("\003", argsList.get(1).param);
        assertEquals("\005", argsList.get(2).param);

        /* TEST 2: check #$number */
        System.out.println("ASCII Test 2");
        argsList = TTLParser.extractArgsWithType("sendln  #3#13 #$a #$A ");
        for (TTLParser.TTLParam ttlParam : argsList) {
            System.out.println("type = " + ttlParam.type + ", param " +
                    MyUtils.unEscapeString(ttlParam.param));
        }
        assertEquals("sendln", argsList.get(0).param);
        assertEquals("\003\015", argsList.get(1).param);
        assertEquals("\012", argsList.get(2).param);
        assertEquals("\012", argsList.get(3).param);

        /* TEST 3: combination */
        System.out.println("ASCII Test 3");
        argsList = TTLParser.extractArgsWithType("send 'cat readme.txt'#13#10");
        for (TTLParser.TTLParam ttlParam : argsList) {
            System.out.println("type = " + ttlParam.type + ", param " +
                    MyUtils.unEscapeString(ttlParam.param));
        }
        assertEquals("send", argsList.get(0).param);
        assertEquals("cat readme.txt\015\012", argsList.get(1).param);

        /* TEST 4: combination */
        System.out.println("ASCII Test 4");
        argsList = TTLParser.extractArgsWithType("'abc'#$0d#$0a'def'#$0d#$0a'ghi' ");
        assertEquals("abc\015\012def\015\012ghi", argsList.get(0).param);
    }

    @Test
    public void testHexConversion() {
        // Ref https://ttssh2.osdn.jp/manual/ja/macro/syntax/formats.html
        // Integer type spec -> 1)

        /* TEST 1: check #number */
        List<TTLParser.TTLParam> argsList;
        argsList = TTLParser.extractArgsWithType("pause $A");
        assertEquals("pause", argsList.get(0).param);
        assertEquals(TYPE_INTEGER, argsList.get(1).type);
        assertEquals("10", argsList.get(1).param);

        argsList = TTLParser.extractArgsWithType("$A302d3C $A");
        assertEquals(TYPE_INTEGER, argsList.get(0).type);
        assertEquals("170929468", argsList.get(0).param);
        assertEquals(TYPE_INTEGER, argsList.get(1).type);
        assertEquals("10", argsList.get(1).param);
    }

    @Test
    public void testConvertToSendlnCommand() {
        /* Test 1: one line text */
        String inputStr = "cat abc.txt";
        assertEquals("sendln \"cat abc.txt\"\n", TTLParser.convertToSendlnCommand(inputStr));
        /* Test 2: multi line text */
        inputStr = " cd / \ncat abc.txt ";
        assertEquals("sendln \" cd / \"\nsendln \"cat abc.txt \"\n", TTLParser.convertToSendlnCommand(inputStr));
        /* Test 2: multi line text with " */
        inputStr = "cd /\necho \"abc.txt\"";
        assertEquals("sendln \"cd /\"\nsendln \"echo \"#$22\"abc.txt\"#$22\"\"\n", TTLParser.convertToSendlnCommand(inputStr));
    }
}
