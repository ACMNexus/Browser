package com.android.browser.activitys;

import android.os.Bundle;
import android.view.View;
import com.android.browser.R;
import com.android.browser.view.BrowserSettingItem;

/**
 * Created by Luooh on 2017/3/5.
 */

public class AdvancedActivity extends BaseActivity implements BrowserSettingItem.OnStateChangeListener {

    private BrowserSettingItem mEnableJavaScript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_advanced);

        initView();
        setListener();
    }

    private void initView() {
        mEnableJavaScript = (BrowserSettingItem) findViewById(R.id.enableJavaScript);

        mEnableJavaScript.setCheckedImmediately(mSettingValues.enableJavascript());
    }

    private void setListener() {
        mEnableJavaScript.setOnStateChangeListener(this);
    }

    @Override
    public void onStateChange(View view, boolean state) {
        switch (view.getId()) {
            case R.id.enableJavaScript:
                mEnableJavaScript.setCheckedNoEvent(state);
                mSettingValues.setJavaScriptState(state);
                break;
        }
    }
}
