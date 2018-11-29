package com.blazebit.notify.template.freemarker;

import java.util.ResourceBundle;

public class TemplatingUtil {

    public static String resolveVariables(String text, ResourceBundle resourceBundle) {
        return resolveVariables(text, resourceBundle, "${", "}");
    }

    public static String resolveVariables(String text, ResourceBundle resourceBundle, String startMarker, String endMarker) {
        int e = 0;
        int s = text.indexOf(startMarker);
        if (s == -1) {
            return text;
        } else {
            StringBuilder sb = new StringBuilder();

            do {
                if (e < s) {
                    sb.append(text, e, s);
                }
                e = text.indexOf(endMarker, s + startMarker.length());
                if (e != -1) {
                    String key = text.substring(s + startMarker.length(), e);
                    sb.append(resourceBundle.getString(key));
                    e += endMarker.length();
                    s = text.indexOf(startMarker, e);
                } else {
                    e = s;
                    break;
                }
            } while (s != -1);

            if (e < text.length()) {
                sb.append(text.substring(e));
            }
            return sb.toString();
        }
    }
}

