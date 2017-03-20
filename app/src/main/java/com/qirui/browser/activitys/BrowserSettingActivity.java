package com.qirui.browser.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.qirui.browser.BrowserActivity;
import com.qirui.browser.BrowserSettings;
import com.qirui.browser.R;
import com.qirui.browser.util.ActivityUtils;
import com.qirui.browser.util.Constants;
import com.qirui.browser.view.BrowserSettingItem;

/**
 * Created by Luooh on 2017/2/16.
 */
public class BrowserSettingActivity extends BaseActivity {

    public static final int TEXTCODING_REQUESTCODE = 10001;
    public static final int USERAGENT_REQUESTCODE = 20001;
    public static final int SEARCH_REQUESTCODE = 30001;
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

        mSearchEngine.setSettingIcon(mSettingValues.getSearchIconResId());
        mTextCoding.setSettingValue(mSettingValues.getDefaultTextEncoding());
        mUserAgent.setSettingValue(getUserAgentResId(mSettingValues.getUserAgent()));
    }

    private void setListener() {
        findViewById(R.id.setting_security).setOnClickListener(this);
        findViewById(R.id.setting_font_size).setOnClickListener(this);
        findViewById(R.id.setting_clear_data).setOnClickListener(this);
        findViewById(R.id.reset_browser_config).setOnClickListener(this);
        findViewById(R.id.setting_advance).setOnClickListener(this);
        mUserAgent.setOnClickListener(this);
        mTextCoding.setOnClickListener(this);
        mSearchEngine.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.setting_search:
                ActivityUtils.startNextPagerForResult(this, SearchEngineActivity.class, SEARCH_REQUESTCODE);
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
                ActivityUtils.startNextPagerForResult(this, TextCodingActivity.class, TEXTCODING_REQUESTCODE);
                break;
            case R.id.setting_user_agent:
                ActivityUtils.startNextPagerForResult(this, UserAgentActivity.class, USERAGENT_REQUESTCODE);
                break;
            case R.id.reset_browser_config:
                BrowserSettings.getInstance().resetDefaultPreferences();
                startActivity(new Intent(BrowserActivity.ACTION_RESTART, null, this, BrowserActivity.class));
                break;
            case R.id.setting_advance:
                ActivityUtils.startNextPager(this, AdvancedActivity.class);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if(requestCode == SEARCH_REQUESTCODE) {
                int iconId = data.getIntExtra(Constants.SEARCHICON, R.drawable.ic_browser_engine_baidu);
                mSearchEngine.setSettingIcon(iconId);
            } else if (requestCode == TEXTCODING_REQUESTCODE) {
                String coding = data.getStringExtra(Constants.TEXTCODING);
                mTextCoding.setSettingValue(coding);
            } else if (requestCode == USERAGENT_REQUESTCODE) {
                String userAgent = data.getStringExtra(Constants.USERAGENT);
                mUserAgent.setSettingValue(userAgent);
            }
        }
    }

    private int getUserAgentResId(int position) {
        switch (position) {
            case 0:
                return R.string.defalut_user_agent;
            case 1:
                return R.string.iphone_user_agent;
            case 2:
                return R.string.ipad_user_agent;
            case 3:
                return R.string.desktop_user_agent;
        }
        return R.string.defalut_user_agent;
    }
}

