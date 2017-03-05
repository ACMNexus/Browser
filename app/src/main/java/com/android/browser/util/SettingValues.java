package com.android.browser.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.WebSettings;
import com.android.browser.BrowserSettings;
import com.android.browser.PreferenceKeys;
import com.android.browser.R;
import com.android.browser.search.SearchEngine;

/**
 * Created by Luooh on 2017/2/24.
 */
public class SettingValues implements PreferenceKeys {

    private Context mContext;
    private SharedPreferences mPrefs;
    private BrowserSettings mBrowserSettings;

    public SettingValues(Context context, SharedPreferences prefs, BrowserSettings browserSettings) {
        this.mPrefs = prefs;
        this.mContext = context;
        this.mBrowserSettings = browserSettings;
    }

    public String getSearchEngineName() {
        return mPrefs.getString(PREF_SEARCH_ENGINE, SearchEngine.BAIDU);
    }

    public void setSearchEngineName(String engineName) {
        mPrefs.edit().putString(PREF_SEARCH_ENGINE, engineName).commit();
    }

    public WebSettings.PluginState getPluginState() {
        String state = mPrefs.getString(PREF_PLUGIN_STATE, "ON");
        return WebSettings.PluginState.valueOf(state);
    }

    public WebSettings.ZoomDensity getDefaultZoom() {
        String zoom = mPrefs.getString(PREF_DEFAULT_ZOOM, "MEDIUM");
        return WebSettings.ZoomDensity.valueOf(zoom);
    }

    public boolean acceptCookies() {
        return mPrefs.getBoolean(PREF_ACCEPT_COOKIES, true);
    }

    public void setAcceptCookiesState(boolean value) {
        mPrefs.edit().putBoolean(PREF_ACCEPT_COOKIES, value).commit();
    }

    public boolean rememberPasswords() {
        return mPrefs.getBoolean(PREF_REMEMBER_PASSWORDS, true);
    }

    public void setRememberPasswordState(boolean value) {
        mPrefs.edit().putBoolean(PREF_REMEMBER_PASSWORDS, value).commit();
    }

    public boolean showSecurityWarnings() {
        return mPrefs.getBoolean(PREF_SHOW_SECURITY_WARNINGS, true);
    }

    public void setSecurityWarnings(boolean state) {
        mPrefs.edit().putBoolean(PREF_SHOW_SECURITY_WARNINGS, state).commit();
    }

    public boolean saveFormdata() {
        return mPrefs.getBoolean(PREF_SAVE_FORMDATA, true);
    }

    public void setFormdata(boolean state) {
        mPrefs.edit().putBoolean(PREF_SAVE_FORMDATA, state).commit();
    }

    public boolean enableGeolocation() {
        return mPrefs.getBoolean(PREF_ENABLE_GEOLOCATION, true);
    }

    public void setGeolocationState(boolean state) {
        mPrefs.edit().putBoolean(PREF_ENABLE_GEOLOCATION, state).commit();
    }

    public boolean enableJavascript() {
        return mPrefs.getBoolean(PREF_ENABLE_JAVASCRIPT, true);
    }

    public void setJavaScriptState(boolean state) {
        mPrefs.edit().putBoolean(PREF_ENABLE_JAVASCRIPT, state).commit();
    }

    public String getDefaultTextEncoding() {
        return mPrefs.getString(PREF_DEFAULT_TEXT_ENCODING, mContext.getString(R.string.pref_default_text_encoding_default));
    }

    public void setDefalutTextCoding(String defalutTextCoding) {
        mPrefs.edit().putString(PREF_DEFAULT_TEXT_ENCODING, defalutTextCoding).commit();
    }

    public int getUserAgent() {
        return Integer.parseInt(mPrefs.getString(PREF_USER_AGENT, "0"));
    }

    public void setUserAgent(int userAgentIndex) {
        mPrefs.edit().putString(PREF_USER_AGENT, userAgentIndex + "").commit();
    }

    public boolean isAutofillEnabled() {
        return mPrefs.getBoolean(PREF_AUTOFILL_ENABLED, true);
    }

    public void setAutofillEnabled(boolean value) {
        mPrefs.edit().putBoolean(PREF_AUTOFILL_ENABLED, value).apply();
    }

    public boolean loadImages() {
        return mPrefs.getBoolean(PREF_LOAD_IMAGES, true);
    }

    public void setLoadImagesState(boolean state) {
        mPrefs.edit().putBoolean(PREF_LOAD_IMAGES, state).apply();
    }
}

