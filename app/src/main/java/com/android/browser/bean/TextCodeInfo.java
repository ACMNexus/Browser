package com.android.browser.bean;

/**
 * Created by Luooh on 2017/2/24.
 */
public class TextCodeInfo {

    private boolean isChecked;
    private String textCodeName;
    private String textCodeLabel;

    public TextCodeInfo(String textCodeName, String textCodeLabel, boolean isChecked) {
        this.isChecked = isChecked;
        this.textCodeName = textCodeName;
        this.textCodeLabel = textCodeLabel;
    }

    public String getTextCodeName() {
        return textCodeName;
    }

    public void setTextCodeName(String textCodeName) {
        this.textCodeName = textCodeName;
    }

    public String getTextCodeLabel() {
        return textCodeLabel;
    }

    public void setTextCodeLabel(String textCodeLabel) {
        this.textCodeLabel = textCodeLabel;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
