/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.browser;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.browser.UrlInputView.StateListener;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class NavigationBarPhone extends NavigationBarBase implements StateListener, View.OnTouchListener {

    private ImageView mClearButton;
    private ImageView mRefreshButton;
    private TextView mWebViewTitles;
    private TextView mEnterButton;
    private TextView mCancelButton;
    private LinearLayout mEditMode;
    private LinearLayout mRequestMode;

    private Drawable mStopDrawable;
    private Drawable mRefreshDrawable;
    private Drawable mIconSiteDrawable;
    private Drawable mSafetySiteDrawable;

    private String mStopDescription;
    private String mRefreshDescription;

    public NavigationBarPhone(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        init();
        initView();
        setListener();
    }

    private void init() {
        setFocusState(false);
        Resources res = getContext().getResources();
        mStopDrawable = res.getDrawable(R.drawable.common_titlebar_close_selector);
        mRefreshDrawable = res.getDrawable(R.drawable.ic_website_refresh);
        mIconSiteDrawable = res.getDrawable(R.drawable.common_icon_site);
        mSafetySiteDrawable = res.getDrawable(R.drawable.website_safe_icon);
        mStopDescription = res.getString(R.string.accessibility_button_stop);
        mRefreshDescription = res.getString(R.string.accessibility_button_refresh);
    }

    private void initView() {
        mRefreshButton = (ImageView) findViewById(R.id.refresh);
        mWebViewTitles = (TextView) findViewById(R.id.web_titles);
        mEnterButton = (TextView) findViewById(R.id.enter);
        mClearButton = (ImageView) findViewById(R.id.clear);
        mEditMode = (LinearLayout) findViewById(R.id.editMode);
        mRequestMode = (LinearLayout) findViewById(R.id.requestMode);
        mCancelButton = (TextView) findViewById(R.id.cancel);
    }

    private void setListener() {
        mUrlInput.setStateListener(this);
        mLockIcon.setOnClickListener(this);
        mEnterButton.setOnClickListener(this);
        mClearButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mRefreshButton.setOnClickListener(this);
        mWebViewTitles.setOnTouchListener(this);
    }

    @Override
    public void onProgressStarted() {
        super.onProgressStarted();
        if (mLockIcon.getDrawable() != mIconSiteDrawable) {
            mLockIcon.setImageDrawable(mIconSiteDrawable);
        }
        if (mRefreshButton.getDrawable() != mStopDrawable) {
            mRefreshButton.setImageDrawable(mStopDrawable);
            mRefreshButton.setContentDescription(mStopDescription);
            if (mRefreshButton.getVisibility() != View.VISIBLE) {
                mRefreshButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onProgressStopped() {
        super.onProgressStopped();
        mLockIcon.setImageDrawable(mSafetySiteDrawable);
        mRefreshButton.setImageDrawable(mRefreshDrawable);
        mRefreshButton.setContentDescription(mRefreshDescription);
        onStateChanged(StateListener.STATE_NORMAL);
    }

    /**
     * Update the text displayed in the title bar.
     * @param title String to display.  If null, the new tab string will be shown.
     */
    @Override
    void setDisplayTitle(String title) {
        mWebViewTitles.setText(title);
        if (!isEditingUrl()) {
            if (title == null) {
                mUrlInput.setText(R.string.new_tab);
            } else {
                mUrlInput.setText(UrlUtils.stripUrl(title));
            }
            mUrlInput.setSelection(0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear:
                mUrlInput.setText("");
                break;
            case R.id.lock:
                mUiController.showPageInfo();
                break;
            case R.id.cancel:
                onStateChanged(StateListener.STATE_NORMAL);
                break;
            case R.id.refresh:
                reloadPage();
                break;
            case R.id.enter:
                reloadPage();
                break;
        }
    }

    private void reloadPage() {
        if (mTitleBar.isInLoad()) {
            mUiController.stopLoading();
        } else {
            WebView web = mBaseUi.getWebView();
            if (web != null) {
                stopEditingUrl();
                web.reload();
            }
        }
    }

    @Override
    public boolean isMenuShowing() {
        return super.isMenuShowing();
    }

    @Override
    public void onStateChanged(int state) {
        switch (state) {
            case StateListener.STATE_NORMAL:
                mEditMode.setVisibility(View.GONE);
                mRequestMode.setVisibility(View.VISIBLE);
                break;
            case StateListener.STATE_EDITED:
                mEditMode.setVisibility(View.VISIBLE);
                mRequestMode.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.GONE);
                mClearButton.setVisibility(View.VISIBLE);
                mEnterButton.setVisibility(View.VISIBLE);
                mUrlInput.requestFocus();
                mUrlInput.setSelectAllOnFocus(true);
                mUrlInput.showIME();
                break;
            case StateListener.STATE_CLEAR:
                mClearButton.setVisibility(View.GONE);
                mEnterButton.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onTabDataChanged(Tab tab) {
        super.onTabDataChanged(tab);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(view == mWebViewTitles) {
            onStateChanged(StateListener.STATE_EDITED);
            return true;
        }
        return false;
    }
}
