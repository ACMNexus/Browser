package com.qirui.browser.activitys;

import android.os.Bundle;
import android.view.View;
import com.qirui.browser.R;
import com.qirui.browser.view.BrowserSettingItem;

/**
 * Created by Luooh on 2017/3/5.
 */
public class AdvancedActivity extends BaseActivity implements BrowserSettingItem.OnStateChangeListener {

    private BrowserSettingItem mFormAutoFill;
    private BrowserSettingItem mForceScale;
    private BrowserSettingItem mAutoFit;
    private BrowserSettingItem mLoadImage;
    private BrowserSettingItem mAdBlock;
    private BrowserSettingItem mPluginState;
    private BrowserSettingItem mBlockPopup;
    private BrowserSettingItem mEnableJavaScript;
    private BrowserSettingItem mExitConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_advanced);

        initView();
        setListener();
    }

    private void initView() {
        mFormAutoFill = (BrowserSettingItem) findViewById(R.id.auto_fill);
        mForceScale = (BrowserSettingItem) findViewById(R.id.setting_force_scale);
        mAutoFit = (BrowserSettingItem) findViewById(R.id.auto_fit);
        mLoadImage = (BrowserSettingItem) findViewById(R.id.load_image);
        mAdBlock = (BrowserSettingItem) findViewById(R.id.setting_filter_ad);
        mPluginState = (BrowserSettingItem) findViewById(R.id.plugin_state);
        mBlockPopup = (BrowserSettingItem) findViewById(R.id.block_popups);
        mEnableJavaScript = (BrowserSettingItem) findViewById(R.id.enableJavaScript);
        mExitConfirm = (BrowserSettingItem) findViewById(R.id.setting_exit_confirm);


        mAutoFit.setCheckedImmediately(mSettingValues.autofitPages());
        mAdBlock.setCheckedImmediately(mSettingValues.getBlockAdMode());
        mPluginState.setCheckedImmediately(mSettingValues.getPluginMode());
        mLoadImage.setCheckedImmediately(mSettingValues.getLoadImagesMode());
        mBlockPopup.setCheckedImmediately(mSettingValues.isBlockPopupWindows());
        mExitConfirm.setCheckedImmediately(mSettingValues.getConfirmExitMode());
        mEnableJavaScript.setCheckedImmediately(mSettingValues.enableJavascript());
        mForceScale.setCheckedImmediately(mSettingValues.forceEnableUserScalable());
    }

    private void setListener() {
        mFormAutoFill.setOnStateChangeListener(this);
        mForceScale.setOnStateChangeListener(this);
        mAutoFit.setOnStateChangeListener(this);
        mLoadImage.setOnStateChangeListener(this);
        mAdBlock.setOnStateChangeListener(this);
        mPluginState.setOnStateChangeListener(this);
        mBlockPopup.setOnStateChangeListener(this);
        mEnableJavaScript.setOnStateChangeListener(this);
        mExitConfirm.setOnStateChangeListener(this);
    }

    @Override
    public void onStateChange(View view, boolean state) {
        switch (view.getId()) {
            case R.id.enableJavaScript:
                mSettingValues.setJavaScriptState(state);
                break;
            case R.id.block_popups:
                mSettingValues.setBlockPopupWindows(state);
                break;
            case R.id.load_image:
                mSettingValues.setLoadImagesMode(state);
                break;
            case R.id.auto_fit:
                mSettingValues.setAutoFitPages(state);
                break;
            case R.id.setting_force_scale:
                mSettingValues.setForceScaleable(state);
                break;
            case R.id.plugin_state:
                mSettingValues.setPluginMode(state);
                break;
            case R.id.setting_exit_confirm:
                mSettingValues.setConfirmExitMode(state);
                break;
            case R.id.setting_filter_ad:
                mSettingValues.setBlockAdMode(state);
                break;
        }

        if(view instanceof BrowserSettingItem) {
            BrowserSettingItem item = (BrowserSettingItem) view;
            item.setCheckedNoEvent(state);
        }
    }
}
