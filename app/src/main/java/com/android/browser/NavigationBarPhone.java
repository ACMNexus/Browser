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
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import com.android.browser.UrlInputView.StateListener;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class NavigationBarPhone extends NavigationBarBase implements
        StateListener, OnMenuItemClickListener, OnDismissListener {

    private ImageView mClearButton;
    private TextView mWebViewTitles;
    private ImageView mRefreshButton;
    private Drawable mStopDrawable;
    private Drawable mRefreshDrawable;
    private Drawable mIconSiteDrawable;
    private Drawable mSafetySiteDrawable;
    private String mStopDescription;
    private String mRefreshDescription;
    private PopupMenu mPopupMenu;
    private boolean mOverflowMenuShowing;

    public NavigationBarPhone(Context context) {
        super(context);
    }

    public NavigationBarPhone(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigationBarPhone(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mClearButton = (ImageView) findViewById(R.id.clear);
        mRefreshButton = (ImageView) findViewById(R.id.refresh);
        mWebViewTitles = (TextView) findViewById(R.id.web_titles);
        mClearButton.setOnClickListener(this);
        mRefreshButton.setOnClickListener(this);
        findViewById(R.id.iconcombo).setOnClickListener(this);
        setFocusState(false);
        Resources res = getContext().getResources();
        mStopDrawable = res.getDrawable(R.drawable.common_titlebar_close_selector);
        mRefreshDrawable = res.getDrawable(R.drawable.ic_website_refresh);
        mIconSiteDrawable = res.getDrawable(R.drawable.common_icon_site);
        mSafetySiteDrawable = res.getDrawable(R.drawable.website_safe_icon);
        mStopDescription = res.getString(R.string.accessibility_button_stop);
        mRefreshDescription = res.getString(R.string.accessibility_button_refresh);
        mUrlInput.setContainer(this);
        mUrlInput.setStateListener(this);
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
        onStateChanged(mUrlInput.getState());
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
                mUrlInput.setText(UrlUtils.stripUrl(title), false);
            }
            mUrlInput.setSelection(0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stop:
                break;
            case R.id.clear:
                mUrlInput.setText("");
                break;
            case R.id.iconcombo:
                mUiController.showPageInfo();
                break;
            case R.id.refresh:
                if (mTitleBar.isInLoad()) {
                    mUiController.stopLoading();
                } else {
                    WebView web = mBaseUi.getWebView();
                    if (web != null) {
                        stopEditingUrl();
                        web.reload();
                    }
                }
                break;
        }
    }

    @Override
    public boolean isMenuShowing() {
        return super.isMenuShowing() || mOverflowMenuShowing;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void showMenu(View anchor) {
        Activity activity = mUiController.getActivity();
        if (mPopupMenu == null) {
            mPopupMenu = new PopupMenu(getContext(), anchor);
            mPopupMenu.setOnMenuItemClickListener(this);
            mPopupMenu.setOnDismissListener(this);
            if (!activity.onCreateOptionsMenu(mPopupMenu.getMenu())) {
                mPopupMenu = null;
                return;
            }
        }
        Menu menu = mPopupMenu.getMenu();
        if (activity.onPrepareOptionsMenu(menu)) {
            mOverflowMenuShowing = true;
            mPopupMenu.show();
        }
    }

    @Override
    public void onDismiss(PopupMenu menu) {
        if (menu == mPopupMenu) {
            onMenuHidden();
        }
    }

    private void onMenuHidden() {
        mOverflowMenuShowing = false;
        mBaseUi.showTitleBarForDuration();
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        super.onFocusChange(view, hasFocus);
    }

    @Override
    public void onStateChanged(int state) {
        switch (state) {
            case StateListener.STATE_NORMAL:
                mClearButton.setVisibility(View.GONE);
                mLockIcon.setImageDrawable(mSafetySiteDrawable);
                break;
            case StateListener.STATE_EDITED:
                mClearButton.setVisibility(View.VISIBLE);
                mLockIcon.setImageDrawable(mIconSiteDrawable);
                break;
        }
    }

    @Override
    public void onTabDataChanged(Tab tab) {
        super.onTabDataChanged(tab);
//        mIncognitoIcon.setVisibility(tab.isPrivateBrowsingEnabled() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return mUiController.onOptionsItemSelected(item);
    }
}
