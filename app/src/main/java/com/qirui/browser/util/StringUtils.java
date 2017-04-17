package com.qirui.browser.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Luooh on 2017/4/17.
 */
public class StringUtils {

    public static final String bookmarkOrHistoryColumn(String column) {
        return "CASE WHEN bookmarks." + column + " IS NOT NULL THEN " +
                "bookmarks." + column + " ELSE history." + column + " END AS " + column;
    }

    public static final String bookmarkOrHistoryLiteral(String column, String bookmarkValue,
                                                        String historyValue) {
        return "CASE WHEN bookmarks." + column + " IS NOT NULL THEN \"" + bookmarkValue +
                "\" ELSE \"" + historyValue + "\" END";
    }

    public static final String qualifyColumn(String table, String column) {
        return table + "." + column + " AS " + column;
    }

    // Regular expression which matches http://, followed by some stuff, followed by
    // optionally a trailing slash, all matched as separate groups.
    private static final Pattern STRIP_URL_PATTERN = Pattern.compile("^(http://)(.*?)(/$)?");

    /**
     * Strips the provided url of preceding "http://" and any trailing "/". Does not
     * strip "https://". If the provided string cannot be stripped, the original string
     * is returned.
     * <p>
     * TODO: Put this in TextUtils to be used by other packages doing something similar.
     *
     * @param url a url to strip, like "http://www.google.com/"
     * @return a stripped url like "www.google.com", or the original string if it could
     * not be stripped
     */
    public static String stripUrl(String url) {
        if (url == null) return null;
        Matcher m = STRIP_URL_PATTERN.matcher(url);
        if (m.matches() && m.groupCount() == 3) {
            return m.group(2);
        } else {
            return url;
        }
    }
}
