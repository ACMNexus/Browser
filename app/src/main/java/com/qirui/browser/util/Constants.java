package com.qirui.browser.util;

/**
 * Created by Luooh on 2017/1/8.
 */

public class Constants {

    public static final String EXTRA_SHARE_FAVICON = "share_favicon";

    public static final String EXTRA_SHARE_SCREENSHOT = "share_screenshot";

    public static final String NATIVE_PAGE_URL = "qirui-native://homepage";

    // TODO: Do something with this UserAgent stuff
    public static final String DESKTOP_USERAGENT = "Mozilla/5.0 (X11; " +
            "Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) " +
            "Chrome/11.0.696.34 Safari/534.24";

    public static final String IPHONE_USERAGENT = "Mozilla/5.0 (iPhone; U; " +
            "CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 " +
            "(KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7";

    public static final String IPAD_USERAGENT = "Mozilla/5.0 (iPad; U; " +
            "CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 " +
            "(KHTML, like Gecko) Version/4.0.4 Mobile/7B367 Safari/531.21.10";

    public static final String USER_AGENTS[] = {null,
            IPHONE_USERAGENT,
            IPAD_USERAGENT,
            DESKTOP_USERAGENT
    };

    public static final String SEARCHICON = "searchIcon";
    public static final String USERAGENT = "UserAgent";
    public static final String TEXTCODING = "TextCoding";
}
