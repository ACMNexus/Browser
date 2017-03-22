package com.qirui.browser.activitys;

import android.os.Bundle;
import android.view.View;
import com.qirui.browser.R;
import com.qirui.browser.util.ToastUtils;
import com.qirui.browser.view.BrowserSettingItem;

/**
 * Created by Luooh on 2017/2/22.
 */
public class ClearDataActivity extends BaseActivity implements BrowserSettingItem.OnStateChangeListener {

    private BrowserSettingItem mClearCache;
    private BrowserSettingItem mClearCookie;
    private BrowserSettingItem mClearFormData;
    private BrowserSettingItem mClearHistory;
    private BrowserSettingItem mClearPassword;
    private BrowserSettingItem mClearPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_clear_data);

        initView();
        initData();
        setListener();
    }

    private void initView() {
        mClearCache = (BrowserSettingItem) findViewById(R.id.clear_cache);
        mClearCookie = (BrowserSettingItem) findViewById(R.id.clear_cookie);
        mClearFormData = (BrowserSettingItem) findViewById(R.id.clear_form);
        mClearHistory = (BrowserSettingItem) findViewById(R.id.clear_history);
        mClearPassword = (BrowserSettingItem) findViewById(R.id.clear_password);
        mClearPosition = (BrowserSettingItem) findViewById(R.id.clear_position);
    }

    private void initData() {
    }

    private void setListener() {
        mClearCache.setOnStateChangeListener(this);
        mClearCookie.setOnStateChangeListener(this);
        mClearFormData.setOnStateChangeListener(this);
        mClearHistory.setOnStateChangeListener(this);
        mClearPassword.setOnStateChangeListener(this);
        mClearPosition.setOnStateChangeListener(this);
        findViewById(R.id.clearAll).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.clearAll:
                clearAll();
                break;
        }
    }

    private void clearAll() {
        if(mClearCache.isChecked()) {
            mBrowserSetting.clearCache();
            mBrowserSetting.clearDatabases();
        }
        if(mClearCookie.isChecked()) {
            mBrowserSetting.clearCookies();
        }
        if(mClearFormData.isChecked()) {
            mBrowserSetting.clearFormData();
        }
        if(mClearHistory.isChecked()) {
            mBrowserSetting.clearHistory();
        }
        if(mClearPassword.isChecked()) {
            mBrowserSetting.clearPasswords();
        }
        if(mClearPosition.isChecked()) {
            mBrowserSetting.clearLocationAccess();
        }
        ToastUtils.show(this, R.string.clear_data_result_tip);
    }

    @Override
    public void onStateChange(View view, boolean state) {
        if(view instanceof BrowserSettingItem) {
            BrowserSettingItem item = (BrowserSettingItem) view;
            item.setCheckedState(state);
        }
    }
}
