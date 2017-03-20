package com.qirui.browser.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.WebSettings;
import com.qirui.browser.BrowserSettings;
import com.qirui.browser.PreferenceKeys;
import com.qirui.browser.R;
import com.qirui.browser.search.SearchEngine;

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

    public void setSearchIconResId(int resId) {
        mPrefs.edit().putInt(PREF_SEARCH_ICON, resId).apply();
    }

    public int getSearchIconResId() {
        return mPrefs.getInt(PREF_SEARCH_ICON, R.drawable.ic_browser_engine_baidu);
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

    /**
     * state true 表示自动加载图片，false表示不加载图片
     */
    public boolean getLoadImagesMode() {
        return mPrefs.getBoolean(PREF_LOAD_IMAGES, true);
    }

    /**
     * 设置图片加载的状态， true表示加载图片
     * @param state
     */
    public void setLoadImagesMode(boolean state) {
        mPrefs.edit().putBoolean(PREF_LOAD_IMAGES, state).apply();
    }

    public boolean getFullscreenState() {
        return mPrefs.getBoolean(PREF_FULLSCREEN, false);
    }

    public void setFullScreenState(boolean state) {
        mPrefs.edit().putBoolean(PREF_FULLSCREEN, state).apply();
    }

    /**
     * 设置访问的模式， true表示无痕模式，不保存历史记录
     * @param mode
     */
    public void setPrivateVisite(boolean mode) {
        mPrefs.edit().putBoolean(PREF_VISIT_MODE, mode).apply();
    }

    /**
     * 获取访问的模式，true 表示是私有的访问，无痕模式的；否则是保存历史记录的
     * @return
     */
    public boolean getPrivateMode() {
        return mPrefs.getBoolean(PREF_VISIT_MODE, false);
    }
}

