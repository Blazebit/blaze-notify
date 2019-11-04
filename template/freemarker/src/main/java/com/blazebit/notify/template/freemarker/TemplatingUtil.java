package com.blazebit.notify.template.freemarker;

import java.util.ResourceBundle;

/**
 * A Freemarker template utility.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class TemplatingUtil {

    private TemplatingUtil() {
    }

    /**
     * Resolves variables between the given markers <code>${</code> and <code>}</code> contained in the given text against the given resource bundle.
     *
     * @param text           The text
     * @param resourceBundle The resource bundle
     * @return The text with resolved variables
     */
    public static String resolveVariables(String text, ResourceBundle resourceBundle) {
        return resolveVariables(text, resourceBundle, "${", "}");
    }

    /**
     * Resolves variables between the given markers contained in the given text against the given resource bundle.
     *
     * @param text           The text
     * @param resourceBundle The resource bundle
     * @param startMarker    The start marker
     * @param endMarker      The end marker
     * @return The text with resolved variables
     */
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

