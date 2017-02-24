package com.android.browser.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.android.browser.BrowserActivity;
import com.android.browser.BrowserSettings;
import com.android.browser.R;
import com.android.browser.util.ActivityUtils;
import com.android.browser.view.BrowserSettingItem;

/**
 * Created by Luooh on 2017/2/16.
 */
public class BrowserSettingActivity extends BaseActivity {

    private BrowserSettingItem mTextCoding;
    private BrowserSettingItem mSearchEngine;
    private BrowserSettingItem mUserAgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_browser_setting);

        initView();
        setListener();
    }

    private void initView() {
        mSearchEngine = (BrowserSettingItem) findViewById(R.id.setting_search);
        mUserAgent = (BrowserSettingItem) findViewById(R.id.setting_user_agent);
        mTextCoding = (BrowserSettingItem) findViewById(R.id.setting_text_coding);
    }

    private void setListener() {
        findViewById(R.id.setting_security).setOnClickListener(this);
        findViewById(R.id.setting_font_size).setOnClickListener(this);
        findViewById(R.id.setting_clear_data).setOnClickListener(this);
        findViewById(R.id.reset_browser_config).setOnClickListener(this);
        mUserAgent.setOnClickListener(this);
        mTextCoding.setOnClickListener(this);
        mSearchEngine.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.setting_search:
                ActivityUtils.startNextPager(this, SearchEngineActivity.class);
                break;
            case R.id.setting_font_size:
                ActivityUtils.startNextPager(this, FontSizePreviewActivity.class);
                break;
            case R.id.setting_security:
                ActivityUtils.startNextPager(this, SecurityActivity.class);
                break;
            case R.id.setting_clear_data:
                ActivityUtils.startNextPager(this, ClearDataActivity.class);
                break;
            case R.id.setting_text_coding:
                ActivityUtils.startNextPager(this, TextCodingActivity.class);
                break;
            case R.id.setting_user_agent:
                ActivityUtils.startNextPager(this, UserAgentActivity.class);
                break;
            case R.id.reset_browser_config:
                BrowserSettings.getInstance().resetDefaultPreferences();
                startActivity(new Intent(BrowserActivity.ACTION_RESTART, null, this, BrowserActivity.class));
                break;
        }
    }
}

