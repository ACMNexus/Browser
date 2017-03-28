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
package com.qirui.browser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.luooh.stackoverview.views.Overview;
import com.qirui.browser.adapter.TabScrollerAdapter;
import java.util.HashMap;

public class NavScreen extends RelativeLayout implements OnClickListener, TabControl.OnThumbnailUpdatedListener {

    private UiController mUiController;
    private PhoneUi mUi;
    private Activity mActivity;

    private ImageButton mBack;
    private ImageButton mNewTab;
    private Overview mTabScroller;
    private TabScrollerAdapter mTabAdapter;

    public NavTabScroller mScroller;
    private TabAdapter mAdapter;
    private int mOrientation;
    private HashMap<Tab, View> mTabViews;

    public NavScreen(Activity activity, UiController ctl, PhoneUi ui) {
        super(activity);
        mUi = ui;
        mActivity = activity;
        mUiController = ctl;
        mTabAdapter = new TabScrollerAdapter(mActivity);
        mOrientation = activity.getResources().getConfiguration().orientation;
        init();
    }

    private void showWebView() {
        Tab currentTab = mUi.getActiveTab();
        int pos = mUiController.getTabControl().getTabPosition(currentTab);
        close(pos);
    }

    protected float getToolbarHeight() {
        return mActivity.getResources().getDimension(R.dimen.toolbar_height);
    }

    @Override
    protected void onConfigurationChanged(Configuration newconfig) {
        if (newconfig.orientation != mOrientation) {
            int sv = mScroller.getScrollValue();
            removeAllViews();
            mOrientation = newconfig.orientation;
            init();
            mScroller.setScrollValue(sv);
        }
    }

    public void refreshAdapter() {
        mScroller.handleDataChanged(mUiController.getTabControl().getTabPosition(mUi.getActiveTab()));
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void init() {
        LayoutInflater.from(mActivity).inflate(R.layout.nav_screen, this);
        mBack = (ImageButton) findViewById(R.id.back);
        mNewTab = (ImageButton) findViewById(R.id.newtab);
        mTabScroller = (Overview) findViewById(R.id.tab_scroller);
        mScroller = (NavTabScroller) findViewById(R.id.scroller);

        mBack.setOnClickListener(this);
        mNewTab.setOnClickListener(this);
        TabControl tc = mUiController.getTabControl();
        mTabViews = new HashMap(tc.getTabCount());

        mAdapter = new TabAdapter(getContext(), tc);
        mScroller.setOrientation(mOrientation == Configuration.ORIENTATION_LANDSCAPE ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        mScroller.setAdapter(mAdapter, mUiController.getTabControl().getTabPosition(mUi.getActiveTab()));
        mScroller.setOnRemoveListener(new NavTabScroller.OnRemoveListener() {
            public void onRemovePosition(int pos) {
                Tab tab = mAdapter.getItem(pos);
                onCloseTab(tab);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newtab:
                openNewTab();
                break;
            case R.id.back:
                showWebView();
                break;
        }
    }

    private void onCloseTab(Tab tab) {
        if (tab != null) {
            if (tab == mUiController.getCurrentTab()) {
                mUiController.closeCurrentTab();
            } else {
                mUiController.closeTab(tab);
            }
        }
    }

    private void openNewTab() {
        final Tab tab = mUiController.openTab(BrowserSettings.getInstance().getHomePage(), false, false, false);
        if (tab != null) {
            mUiController.setBlockEvents(true);
            final int tix = mUi.mTabControl.getTabPosition(tab);
            mScroller.setOnLayoutListener(new NavTabScroller.OnLayoutListener() {
                @Override
                public void onLayout(int l, int t, int r, int b) {
                    mUi.hideNavScreen(tix, true);
                    switchToTab(tab);
                }
            });
            mScroller.handleDataChanged(tix);
            mUi.hideNavScreen(tix, true);
            switchToTab(tab);
            mTabAdapter.notifyDataSetInserted(tab, 0);
            mUiController.setBlockEvents(false);
        }
    }

    private void switchToTab(Tab tab) {
        if (tab != mUi.getActiveTab()) {
            mUiController.setActiveTab(tab);
        }
    }

    protected void close(int position) {
        close(position, true);
    }

    protected void close(int position, boolean animate) {
        mUi.hideNavScreen(position, animate);
    }

    protected NavTabView getTabView(int pos) {
        return mScroller.getTabView(pos);
    }

    class TabAdapter extends BaseAdapter {

        Context context;
        TabControl tabControl;

        public TabAdapter(Context ctx, TabControl tc) {
            context = ctx;
            tabControl = tc;
        }

        @Override
        public int getCount() {
            return tabControl.getTabCount();
        }

        @Override
        public Tab getItem(int position) {
            return tabControl.getTab(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final NavTabView tabview = new NavTabView(mActivity);
            final Tab tab = getItem(position);
            tabview.setWebView(tab);
            mTabViews.put(tab, tabview.mImage);
            tabview.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tabview.isClose(v)) {
                        mScroller.animateOut(tabview);
                    } else if (tabview.isTitle(v)) {
                        switchToTab(tab);
                        mUi.getTitleBar().setSkipTitleBarAnimations(true);
                        close(position, false);
                        mUi.editUrl(false, true);
                        mUi.getTitleBar().setSkipTitleBarAnimations(false);
                    } else if (tabview.isWebView(v)) {
                        close(position);
                    }
                }
            });
            return tabview;
        }
    }

    @Override
    public void onThumbnailUpdated(Tab t) {
        View v = mTabViews.get(t);
        if (v != null) {
            v.invalidate();
        }
    }
}
