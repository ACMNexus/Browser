package com.android.browser.activitys;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.android.browser.R;
import com.android.browser.view.BrowserSettingItem;

import java.util.List;

/**
 * Created by Luooh on 2017/2/21.
 */
public class TextCodingActivity extends BaseActivity {

    private BrowserSettingItem mLatinCode;
    private BrowserSettingItem mUTF8Code;
    private BrowserSettingItem mGBKCode;
    private BrowserSettingItem mBig5Code;
    private BrowserSettingItem mISOJPCode;
    private BrowserSettingItem mJISJPCode;
    private BrowserSettingItem mEUCJPCode;
    private BrowserSettingItem mEUCKRCode;

    private LinearLayout mCodeNames;
    private List<BrowserSettingItem> mCodeViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_textcoding);

        String codingtitles[] = getResources().getStringArray(R.array.pref_default_text_encoding_choices);
        mCodeNames = (LinearLayout) findViewById(R.id.code_names);
        for(int i = 0; i < mCodeNames.getChildCount(); i++) {
            View view = mCodeNames.getChildAt(0);
            if(view instanceof BrowserSettingItem) {
                mCodeViews.add((BrowserSettingItem) view);
                mCodeViews.get(i).setSettingTitle(codingtitles[i]);
                mCodeViews.get(i).setOnClickListener(this);
            }
        }

        /*mLatinCode.setSettingTitle(codingtitles[0]);
        mUTF8Code.setSettingTitle(codingtitles[1]);
        mGBKCode.setSettingTitle(codingtitles[2]);
        mBig5Code.setSettingTitle(codingtitles[3]);
        mISOJPCode.setSettingTitle(codingtitles[4]);
        mJISJPCode.setSettingTitle(codingtitles[5]);
        mEUCJPCode.setSettingTitle(codingtitles[6]);
        mEUCKRCode.setSettingTitle(codingtitles[7]);*/
    }
}
