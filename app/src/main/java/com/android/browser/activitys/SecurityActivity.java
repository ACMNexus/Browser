package com.android.browser.activitys;

import android.os.Bundle;
import android.view.View;
import com.android.browser.R;
import com.android.browser.view.BrowserSettingItem;

/**
 * Created by Luooh on 2017/2/22.
 */
public class SecurityActivity extends BaseActivity implements BrowserSettingItem.OnStateChangeListener {

    private BrowserSettingItem mDataItem;
    private BrowserSettingItem mCookieItem;
    private BrowserSettingItem mLocationItem;
    private BrowserSettingItem mSecurityWarnItem;
    private BrowserSettingItem mPasswordItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_browser_security);

        initView();
        setListener();
    }

    private void initView() {
        mCookieItem = (BrowserSettingItem) findViewById(R.id.cookieItem);
        mDataItem = (BrowserSettingItem) findViewById(R.id.dataItem);
        mLocationItem = (BrowserSettingItem) findViewById(R.id.locationItem);
        mPasswordItem = (BrowserSettingItem) findViewById(R.id.passwordItem);
        mSecurityWarnItem = (BrowserSettingItem) findViewById(R.id.securityWarnItem);

        mDataItem.setCheckedImmediately(mSettingValues.saveFormdata());
        mCookieItem.setCheckedImmediately(mSettingValues.acceptCookies());
        mPasswordItem.setCheckedImmediately(mSettingValues.rememberPasswords());
        mLocationItem.setCheckedImmediately(mSettingValues.enableGeolocation());
        mSecurityWarnItem.setCheckedImmediately(mSettingValues.showSecurityWarnings());
    }

    private void setListener() {
        mCookieItem.setOnStateChangeListener(this);
        mDataItem.setOnStateChangeListener(this);
        mLocationItem.setOnStateChangeListener(this);
        mPasswordItem.setOnStateChangeListener(this);
        mSecurityWarnItem.setOnStateChangeListener(this);
    }

    @Override
    public void onStateChange(View view, boolean state) {
        switch (view.getId()) {
            case R.id.cookieItem:
                mCookieItem.setCheckedNoEvent(state);
                mSettingValues.setAcceptCookiesState(mCookieItem.isChecked());
                break;
            case R.id.dataItem:
                mDataItem.setCheckedNoEvent(state);
                mSettingValues.setFormdata(state);
                break;
            case R.id.locationItem:
                mLocationItem.setCheckedNoEvent(state);
                mSettingValues.setGeolocationState(state);
                break;
            case R.id.passwordItem:
                mPasswordItem.setCheckedNoEvent(state);
                mSettingValues.setRememberPasswordState(state);
                break;
            case R.id.securityWarnItem:
                mSecurityWarnItem.setCheckedNoEvent(state);
                mSettingValues.setSecurityWarnings(state);
                break;
        }
    }
}
