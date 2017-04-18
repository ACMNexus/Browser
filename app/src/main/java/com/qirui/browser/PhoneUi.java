/*
 * Copyright (C) 2010 The Android Open Source Project
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
package com.qirui.browser;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import com.qirui.browser.bean.AnimScreen;
import com.qirui.browser.view.UrlInputView;
import com.qirui.browser.view.UrlInputView.StateListener;
import com.qirui.browser.util.DisplayUtils;
import com.qirui.browser.util.ReflectUtils;

/**
 * Ui for regular phone screen sizes
 */
public class PhoneUi extends BaseUi implements UrlInputView.OnSearchUrl {

    private static final String TAG = PhoneUi.class.getSimpleName();
    private static final int MSG_INIT_NAVSCREEN = 100;

    private NavScreen mNavScreen;
    private AnimScreen mAnimScreen;
    private NavigationBarPhone mNavigationBar;
    private boolean mShowNav = false;

    public PhoneUi(Activity browser, UiController controller) {
        super(browser, controller);
        setUseQuickControls(BrowserSettings.getInstance().useQuickControls());
        mNavigationBar = (NavigationBarPhone) mTitleBar.getNavigationBar();
        mHomePagerController.setOnSearchListener(this);
    }

    @Override
    public void onDestroy() {
        hideTitleBar();
    }

    @Override
    public void editUrl(boolean clearInput, boolean forceIME) {
        if (mUseQuickControls) {
            mTitleBar.setShowProgressOnly(false);
        }
        //Do nothing while at Nav show screen.
        if (mShowNav) return;
        super.editUrl(clearInput, forceIME);
    }

    @Override
    public boolean onBackKey() {
        if (showingNavScreen()) {
            Tab currentTab = mUiController.getTabControl().getCurrentTab();
            mNavScreen.close(mUiController.getTabControl().getCurrentPosition());
            if(mUiController.getTabControl().getCurrentTab().isNativePager()) {
                mUiController.getHomeController().switchNativeHome(currentTab);
                mUiController.getTabControl().setCurrentTab(currentTab);
            }
            return true;
        }
        return super.onBackKey();
    }

    private boolean showingNavScreen() {
        return mNavScreen != null && mNavScreen.getVisibility() == View.VISIBLE;
    }

    @Override
    public boolean dispatchKey(int code, KeyEvent event) {
        return false;
    }

    @Override
    public void onProgressChanged(Tab tab) {
        super.onProgressChanged(tab);
        if (mNavScreen == null && getTitleBar().getHeight() > 0) {
            mHandler.sendEmptyMessage(MSG_INIT_NAVSCREEN);
        }
    }

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == MSG_INIT_NAVSCREEN) {
            if (mNavScreen == null) {
                mNavScreen = new NavScreen(mActivity, mUiController, this);
                mCustomViewContainer.addView(mNavScreen, COVER_SCREEN_PARAMS);
                mNavScreen.setVisibility(View.GONE);
            }
            if (mAnimScreen == null) {
                mAnimScreen = new AnimScreen(mActivity);
                // initialize bitmaps
                mAnimScreen.set(getTitleBar(), getWebView());
            }
        }
    }

    @Override
    public void setActiveTab(final Tab tab) {
        mTitleBar.cancelTitleBarAnimation(true);
        mTitleBar.setSkipTitleBarAnimations(true);
        super.setActiveTab(tab);

        //if at Nav screen show, detach tab like what showNavScreen() do.
        if (mShowNav) {
            detachTab(mActiveTab);
        }

        BrowserWebView view = (BrowserWebView) tab.getWebView();
        // TabControl.setCurrentTab has been called before this,
        // so the tab is guaranteed to have a webview
        if (view == null) {
            Log.e(TAG, "active tab with no webview detected");
            return;
        }
        // Request focus on the top window.
        if (mUseQuickControls) {
            view.setTitleBar(null);
            mTitleBar.setShowProgressOnly(true);
        } else {
            view.setTitleBar(mTitleBar);
        }
        // update nav bar state
        mNavigationBar.onStateChanged(StateListener.STATE_NORMAL);
        updateLockIconToLatest(tab);
        mTitleBar.setSkipTitleBarAnimations(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (showingNavScreen()
                && (item.getItemId() != R.id.history_menu_id)
                && (item.getItemId() != R.id.snapshots_menu_id)) {
            hideNavScreen(mUiController.getTabControl().getCurrentPosition(), false);
        }
        return false;
    }

    // action mode callbacks
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public void onActionModeStarted(ActionMode mode) {
        if (!isEditingUrl()) {
            hideTitleBar();
        } else {
            mTitleBar.animate().translationY(DisplayUtils.dip2px(mActivity, 48) /*mActionBarHeight*/);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onActionModeFinished(boolean inLoad) {
        mTitleBar.animate().translationY(0);
        if (inLoad) {
            if (mUseQuickControls) {
                mTitleBar.setShowProgressOnly(true);
            }
            showTitleBar();
        }
    }

    @Override
    public boolean isWebShowing() {
        return super.isWebShowing() /*&& !showingNavScreen()*/;
    }

    @Override
    public void showWeb(boolean animate) {
        super.showWeb(animate);
        hideNavScreen(mUiController.getTabControl().getCurrentPosition(), animate);
    }

    public void switchNativePage(Tab homeTab) {
        int position = mTabControl.getTabPosition(homeTab);
        hideNavScreen(position, true);
        mHomePagerController.switchNativeHome(homeTab);
    }

    public void paneSwitch(int position, boolean animate) {
        Tab tab = mUiController.getCurrentTab();
        hideHomePager();
        hideNavScreen(position, animate);
        attachTab(tab);
        mUiController.getCurrentTab().setNativePager(false);
        if(tab != null) {
            tab.resume();
        }
        mTitleBar.onResume();

    }

    public void showNavScreen() {
        mShowNav = true;
        mUiController.setBlockEvents(true);
        if (mNavScreen == null) {
            mNavScreen = new NavScreen(mActivity, mUiController, this);
            mCustomViewContainer.addView(mNavScreen, COVER_SCREEN_PARAMS);
        } else {
            mNavScreen.setVisibility(View.VISIBLE);
            mNavScreen.setAlpha(1f);
            mNavScreen.refreshAdapter();
        }

        mActiveTab.capture();
        if (mAnimScreen == null) {
            mAnimScreen = new AnimScreen(mActivity);
        } else {
            mAnimScreen.mMain.setAlpha(1f);
            mAnimScreen.mTitle.setAlpha(1f);
            mAnimScreen.setScaleFactor(1f);
        }

        mAnimScreen.set(getTitleBar(), getWebView());
        if (mAnimScreen.mMain.getParent() == null) {
            mCustomViewContainer.addView(mAnimScreen.mMain, COVER_SCREEN_PARAMS);
        }

        mCustomViewContainer.setVisibility(View.VISIBLE);
        mCustomViewContainer.bringToFront();
        mAnimScreen.mMain.layout(0, 0, mContentView.getWidth(), mContentView.getHeight());

        int fromLeft = 0;
        int fromTop = getTitleBar().getHeight();
        int fromRight = mContentView.getWidth();
        int fromBottom = mContentView.getHeight();
        int width = mActivity.getResources().getDimensionPixelSize(R.dimen.nav_tab_width);
        int height = mActivity.getResources().getDimensionPixelSize(R.dimen.nav_tab_height);
        int ntth = mActivity.getResources().getDimensionPixelSize(R.dimen.nav_tab_titleheight);
        int toLeft = (mContentView.getWidth() - width) / 2;
        int toTop = ((fromBottom - (ntth + height)) / 2 + ntth);
        int toRight = toLeft + width;
        int toBottom = toTop + height;
        float scaleFactor = width / (float) mContentView.getWidth();
        detachTab(mActiveTab);

        finishAnimationIn();
        mUiController.setBlockEvents(false);
        if (mAnimScreen.mMain != null) {
            mCustomViewContainer.removeView(mAnimScreen.mMain);
        }

        AnimatorSet set1 = new AnimatorSet();
        AnimatorSet inanim = new AnimatorSet();
        ObjectAnimator tx = ObjectAnimator.ofInt(mAnimScreen.mContent, "left", fromLeft, toLeft);
        ObjectAnimator ty = ObjectAnimator.ofInt(mAnimScreen.mContent, "top", fromTop, toTop);
        ObjectAnimator tr = ObjectAnimator.ofInt(mAnimScreen.mContent, "right", fromRight, toRight);
        ObjectAnimator tb = ObjectAnimator.ofInt(mAnimScreen.mContent, "bottom", fromBottom, toBottom);
        ObjectAnimator title = ObjectAnimator.ofFloat(mAnimScreen.mTitle, "alpha", 1f, 0f);
        ObjectAnimator sx = ObjectAnimator.ofFloat(mAnimScreen, "scaleFactor", 1f, scaleFactor);
        ObjectAnimator blend1 = ObjectAnimator.ofFloat(mAnimScreen.mMain, "alpha", 1f, 0f);
        blend1.setDuration(100);

        inanim.playTogether(tx, ty, tr, tb, sx, title);
        inanim.setDuration(200);
        set1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
                mCustomViewContainer.removeView(mAnimScreen.mMain);
            }
        });
        set1.playSequentially(inanim, blend1);
        set1.start();
    }

    private void finishAnimationIn() {
        if (showingNavScreen()) {
            mNavScreen.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
            mTabControl.setOnThumbnailUpdatedListener(mNavScreen);
        }
    }

    public void hideNavScreen(int position, boolean animate) {
        mShowNav = false;
        if (!showingNavScreen()) {
            return;
        }

        final Tab tab = mUiController.getTabControl().getTab(position);

        mContentParent.setVisibility(View.VISIBLE);
        mHomePagerContainer.setVisibility(View.GONE);
        mCustomViewContainer.setVisibility(View.GONE);

        if ((tab == null) || !animate) {
            if (tab != null && !tab.isNativePager()) {
                setActiveTab(tab);
            } else if (mTabControl.getTabCount() > 0) {
                Tab tempTab = mTabControl.getCurrentTab();
                if(tempTab != null && !tempTab.isNativePager()) {
                    setActiveTab(mTabControl.getCurrentTab());
                }
            }
            finishAnimateOut();
            return;
        }

        NavTabView tabview = mNavScreen.getTabView(position);
        if (tabview == null) {
            Log.i("LOH", "tabview is null....");
            if (mTabControl.getTabCount() > 0) {
                setActiveTab(mTabControl.getCurrentTab());
            }
            finishAnimateOut();
            return;
        }

        mUiController.setBlockEvents(true);
        mUiController.setActiveTab(tab);

        if (mAnimScreen == null) {
            mAnimScreen = new AnimScreen(mActivity);
        }
        mAnimScreen.set(tab.getScreenshot());
        if (mAnimScreen.mMain.getParent() == null) {
            mCustomViewContainer.addView(mAnimScreen.mMain, COVER_SCREEN_PARAMS);
        }
        mAnimScreen.mMain.layout(0, 0, mContentView.getWidth(), mContentView.getHeight());
        mNavScreen.mScroller.finishScroller();
        ImageView target = tabview.mImage;
        int toLeft = 0;
        int toTop = (tab.getWebView() != null) ? ReflectUtils.getVisibleTitleHeight(tab.getWebView()) : 0;
        int toRight = mContentView.getWidth();
        int width = target.getDrawable().getIntrinsicWidth();
        int height = target.getDrawable().getIntrinsicHeight();
        int fromLeft = tabview.getLeft() + target.getLeft() - mNavScreen.mScroller.getScrollX();
        int fromTop = tabview.getTop() + target.getTop() - mNavScreen.mScroller.getScrollY();
        int fromRight = fromLeft + width;
        int fromBottom = fromTop + height;
        float scaleFactor = mContentView.getWidth() / (float) width;
        int toBottom = toTop + (int) (height * scaleFactor);
        mAnimScreen.mContent.setLeft(fromLeft);
        mAnimScreen.mContent.setTop(fromTop);
        mAnimScreen.mContent.setRight(fromRight);
        mAnimScreen.mContent.setBottom(fromBottom);
        mAnimScreen.setScaleFactor(1f);
        AnimatorSet set1 = new AnimatorSet();
        ObjectAnimator fade2 = ObjectAnimator.ofFloat(mAnimScreen.mMain, "alpha", 0f, 1f);
        ObjectAnimator fade1 = ObjectAnimator.ofFloat(mNavScreen, "alpha", 1f, 0f);
        set1.playTogether(fade1, fade2);
        set1.setDuration(100);
        AnimatorSet set2 = new AnimatorSet();
        ObjectAnimator l = ObjectAnimator.ofInt(mAnimScreen.mContent, "left", fromLeft, toLeft);
        ObjectAnimator t = ObjectAnimator.ofInt(mAnimScreen.mContent, "top", fromTop, toTop);
        ObjectAnimator r = ObjectAnimator.ofInt(mAnimScreen.mContent, "right", fromRight, toRight);
        ObjectAnimator b = ObjectAnimator.ofInt(mAnimScreen.mContent, "bottom", fromBottom, toBottom);
        ObjectAnimator scale = ObjectAnimator.ofFloat(mAnimScreen, "scaleFactor", 1f, scaleFactor);
        ObjectAnimator otheralpha = ObjectAnimator.ofFloat(mCustomViewContainer, "alpha", 1f, 0f);
        otheralpha.setDuration(100);
        set2.playTogether(l, t, r, b, scale);
        set2.setDuration(200);
        AnimatorSet combo = new AnimatorSet();
        combo.playSequentially(set1, set2, otheralpha);
        combo.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
                mCustomViewContainer.removeView(mAnimScreen.mMain);
                finishAnimateOut();
                mUiController.setBlockEvents(false);
            }
        });
        combo.start();
    }

    public void hideHomePager() {
        mContentParent.setVisibility(View.VISIBLE);
        mHomePagerContainer.setVisibility(View.GONE);
    }

    private void finishAnimateOut() {
        mTabControl.setOnThumbnailUpdatedListener(null);
        mNavScreen.setVisibility(View.GONE);
        mCustomViewContainer.setAlpha(1f);
        mCustomViewContainer.setVisibility(View.GONE);
    }

    @Override
    public boolean needsRestoreAllTabs() {
        return false;
    }

    public void toggleNavScreen() {
        if (!showingNavScreen()) {
            showNavScreen();
        } else {
            hideNavScreen(mUiController.getTabControl().getCurrentPosition(), false);
        }
    }

    @Override
    public boolean shouldCaptureThumbnails() {
        return true;
    }

    @Override
    public void onSelect(String text, boolean isInputUrl) {
        onSelect(text, isInputUrl, null);
    }

    @Override
    public void onSelect(String text, boolean isInputUrl, String inputWord) {
        Tab tab = mTabControl.getCurrentTab();
        if(tab == null) {
            return;
        }

        tab.setNativePager(false);
        Intent intent = new Intent();
        String action = Intent.ACTION_SEARCH;
        intent.setAction(action);
        intent.putExtra(SearchManager.QUERY, text);
        if (TYPED != null) {
            Bundle appData = new Bundle();
            appData.putString("source", TYPED);
            intent.putExtra(SearchManager.APP_DATA, appData);
        }
        mUiController.handleNewIntent(intent);
    }
}
