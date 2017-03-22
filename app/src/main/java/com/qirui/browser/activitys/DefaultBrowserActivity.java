package com.qirui.browser.activitys;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.qirui.browser.R;
import com.qirui.browser.util.AppUtils;

/**
 * Created by Luooh on 2017/3/22.
 */
public class DefaultBrowserActivity extends BaseActivity {

    private TextView mDefaultName;
    private ImageView mDefaultIcon;
    private View mDefaultSetting;
    private View mDefaultClear;
    private ActivityInfo mDefaultInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_default_browser);

        mDefaultIcon = (ImageView) findViewById(R.id.browserIcon);
        mDefaultName = (TextView) findViewById(R.id.browserName);
        mDefaultSetting = findViewById(R.id.default_setting);
        mDefaultClear = findViewById(R.id.default_clear);

        mDefaultInfo = AppUtils.getInstance().getDefaultBrowserInfo();
        if(mDefaultInfo != null) {
            mDefaultName.setText(mDefaultInfo.loadLabel(getPackageManager()));
            mDefaultIcon.setImageDrawable(mDefaultInfo.loadIcon(getPackageManager()));
        }

        findViewById(R.id.clearDefault).setOnClickListener(this);
        findViewById(R.id.startSetting).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDefaultInfo = AppUtils.getInstance().getDefaultBrowserInfo();
        if (mDefaultInfo != null && !"android".equals(mDefaultInfo.packageName)) {
            mDefaultSetting.setVisibility(View.GONE);
            mDefaultClear.setVisibility(View.VISIBLE);
        } else {
            mDefaultSetting.setVisibility(View.VISIBLE);
            mDefaultClear.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.clearDefault:
                AppUtils.getInstance().jumpSettingAppDetail(mDefaultInfo.packageName);
                break;
            case R.id.startSetting:
                AppUtils.getInstance().setDefaultBrowser();
                break;
        }
    }
}
