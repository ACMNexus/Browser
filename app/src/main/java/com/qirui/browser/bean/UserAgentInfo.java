package com.qirui.browser.bean;

/**
 * Created by Luooh on 2017/2/24.
 */
public class UserAgentInfo {

    private String userAgentName;
    private String userAgentValue;
    private boolean isChecked;

    public UserAgentInfo(String userAgentName, String userAgentValue, boolean isChecked) {
        this.userAgentName = userAgentName;
        this.userAgentValue = userAgentValue;
        this.isChecked = isChecked;
    }

    public String getUserAgentValue() {
        return userAgentValue;
    }

    public void setUserAgentValue(String userAgentValue) {
        this.userAgentValue = userAgentValue;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getUserAgentName() {
        return userAgentName;
    }

    public void setUserAgentName(String userAgentName) {
        this.userAgentName = userAgentName;
    }
}
