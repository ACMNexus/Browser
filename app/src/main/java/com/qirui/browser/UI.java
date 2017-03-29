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

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebView;
import android.widget.FrameLayout;
import java.util.List;

/**
 * UI interface definitions
 */
public interface UI {

    FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
            new FrameLayout.LayoutParams(-1, -1);

    int MSG_HIDE_TITLEBAR = 1;
    int HIDE_TITLEBAR_DELAY = 1500; // in ms

    enum ComboViews {
        History,
        Bookmarks,
        Snapshots,
    }

    void onPause();

    void onResume();

    void onDestroy();

    void onConfigurationChanged(Configuration config);

    boolean onBackKey();

    boolean onMenuKey();

    boolean needsRestoreAllTabs();

    void addTab(Tab tab);

    void removeTab(Tab tab);

    void setActiveTab(Tab tab);

    void updateTabs(List<Tab> tabs);

    void detachTab(Tab tab);

    void attachTab(Tab tab);

    void onSetWebView(Tab tab, WebView view);

    void createSubWindow(Tab tab, WebView subWebView);

    void attachSubWindow(View subContainer);

    void removeSubWindow(View subContainer);

    void onTabDataChanged(Tab tab);

    void onPageStopped(Tab tab);

    void onProgressChanged(Tab tab);

    void showComboView(ComboViews startingView, Bundle extra);

    void showCustomView(View view, int requestedOrientation,
            CustomViewCallback callback);

    void onHideCustomView();

    boolean isCustomViewShowing();

    boolean onOptionsItemSelected(MenuItem item);

    void onContextMenuCreated(Menu menu);

    void onActionModeStarted(ActionMode mode);

    void onActionModeFinished(boolean inLoad);

    void setShouldShowErrorConsole(Tab tab, boolean show);

    // returns if the web page is clear of any overlays (not including sub windows)
    boolean isWebShowing();

    void showWeb(boolean animate);

    Bitmap getDefaultVideoPoster();

    View getVideoLoadingProgressView();

    void bookmarkedStatusHasChanged(Tab tab);

    void showMaxTabsWarning();

    void editUrl(boolean clearInput, boolean forceIME);

    boolean isEditingUrl();

    boolean dispatchKey(int code, KeyEvent event);

    void showAutoLogin(Tab tab);

    void hideAutoLogin(Tab tab);

    void setFullscreen(boolean enabled);

    void setUseQuickControls(boolean enabled);

    public boolean shouldCaptureThumbnails();

    boolean blockFocusAnimations();

    void onVoiceResult(String result);
}
