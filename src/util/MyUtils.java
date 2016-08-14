package util;

/**
 * Util class
 */
public class MyUtils {

    public static String unEscapeString(String s){
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<s.length(); i++)
            switch (s.charAt(i)){
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                //case ' ': sb.append("\\s"); break;  // \\s is to show space, this is unique spec
                case '\007': sb.append("\\007"); break;
                // ... rest of escape characters
                default: sb.append(s.charAt(i));
            }
        return sb.toString();
    }
}
