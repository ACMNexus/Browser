package com.qirui.browser.bean;

/**
 * Created by Luooh on 2017/3/24.
 */
public class ErrorDialog {

    public final int mTitle;
    public final String mDescription;
    public final int mError;

    public ErrorDialog(int title, String desc, int error) {
        mTitle = title;
        mDescription = desc;
        mError = error;
    }
}
