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

package com.qirui.browser.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.qirui.browser.BrowserSettings;
import com.qirui.browser.UiController;
import com.qirui.browser.UrlSelectionActionMode;
import com.qirui.browser.util.UrlUtils;
import com.qirui.browser.adapter.SuggestionsAdapter;
import com.qirui.browser.search.SearchEngine;
import com.qirui.browser.search.SearchEngineInfo;
import com.qirui.browser.search.SearchEngines;

/**
 * url/search input view
 * handling suggestions
 */
public class UrlInputView extends EditText implements OnEditorActionListener, OnItemClickListener, TextWatcher {

    public static final String TYPED = "browser-type";
    public static final String SUGGESTED = "browser-suggest";

    public interface StateListener {
        int STATE_NORMAL = 0;
        int STATE_CANCEL = 1;
        int STATE_EDITED = 2;
        int STATE_SEARCH = 3;

        void onStateChanged(int state);
    }

    private UrlInputListener mListener;
    private InputMethodManager mInputManager;
    private SuggestionsAdapter mAdapter;
    private boolean mLandscape;
    private boolean mIncognitoMode;
    private boolean mNeedsUpdate;

    private int mState;
    private StateListener mStateListener;

    public UrlInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context ctx) {
        mInputManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        setOnEditorActionListener(this);
//        mAdapter = new SuggestionsAdapter(ctx, this);
        onConfigurationChanged(ctx.getResources().getConfiguration());
        mNeedsUpdate = false;
        addTextChangedListener(this);
        mState = StateListener.STATE_NORMAL;
    }

    /**
     * check if focus change requires a title bar update
     */
    public boolean needsUpdate() {
        return mNeedsUpdate;
    }

    /**
     * clear the focus change needs title bar update flag
     */
    public void clearNeedsUpdate() {
        mNeedsUpdate = false;
    }

    public void setController(UiController controller) {
        UrlSelectionActionMode urlSelectionMode = new UrlSelectionActionMode(controller);
        setCustomSelectionActionModeCallback(urlSelectionMode);
    }

    public void setUrlInputListener(UrlInputListener listener) {
        mListener = listener;
    }

    public void setStateListener(StateListener listener) {
        mStateListener = listener;
        changeState(mState);
    }

    public void changeState(int newState) {
        mState = newState;
        if (mStateListener != null) {
            mStateListener.onStateChanged(mState);
        }
    }

    public void setUrlInputState(int newState) {
        mState = newState;
    }

    public int getState() {
        return mState;
    }

    @Override
    protected void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        mLandscape = (config.orientation & Configuration.ORIENTATION_LANDSCAPE) != 0;
//        mAdapter.setLandscapeMode(mLandscape);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        finishInput(getText().toString(), null, TYPED);
        return true;
    }

    public void hideIME() {
        if(mInputManager.isActive()) {
            mInputManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    public void showIME() {
        mInputManager.showSoftInput(this, 0);
    }

    public void finishInput(String url, String extra, String source) {
        mNeedsUpdate = true;
        mInputManager.hideSoftInputFromWindow(getWindowToken(), 0);
        if (TextUtils.isEmpty(url)) {
            mListener.onDismiss();
        } else {
            if (mIncognitoMode && isSearch(url)) {
                // To prevent logging, intercept this request
                // TODO: This is a quick hack, refactor this
                SearchEngine searchEngine = BrowserSettings.getInstance().getSearchEngine();
                if (searchEngine == null) {
                    return;
                }
                SearchEngineInfo engineInfo = SearchEngines.getSearchEngineInfo(getContext(), searchEngine.getName());
                if (engineInfo == null) return;
                url = engineInfo.getSearchUriForQuery(url);
                // mLister.onAction can take it from here without logging
            }
            mListener.onAction(url, extra, source);
        }
    }

    public boolean isSearch(String inUrl) {
        String url = UrlUtils.fixUrl(inUrl).trim();
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (Patterns.WEB_URL.matcher(url).matches() || UrlUtils.ACCEPTED_URI_SCHEMA.matcher(url).matches()) {
            return false;
        }
        return true;
    }

    public void onSearch(String search) {
        mListener.onCopySuggestion(search);
    }

    public void onSelect(String url, int type, String extra) {
        finishInput(url, extra, SUGGESTED);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SuggestionsAdapter.SuggestItem item = mAdapter.getItem(position);
//        onSelect(SuggestionsAdapter.getSuggestionUrl(item), item.type, item.extra);
    }

    public void setIncognitoMode(boolean incognito) {
        mIncognitoMode = incognito;
//        mAdapter.setIncognitoMode(mIncognitoMode);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent evt) {
        if (keyCode == KeyEvent.KEYCODE_ESCAPE && !isInTouchMode()) {
            finishInput(null, null, null);
            return true;
        }
        return super.onKeyDown(keyCode, evt);
    }

    public SuggestionsAdapter getAdapter() {
        return mAdapter;
    }

    /*
     * no-op to prevent scrolling of webview when embedded titlebar
     * gets edited
    */
    @Override
    public boolean requestRectangleOnScreen(Rect rect, boolean immediate) {
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(TextUtils.isEmpty(getText().toString())) {
            changeState(StateListener.STATE_CANCEL);
        }else {
            if(mState != StateListener.STATE_SEARCH) {
                changeState(StateListener.STATE_SEARCH);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    public interface OnSearchUrl {
        /**
         * 打开指定url链接，入口可能是推荐，或者输入，以及历史记录／收藏中的长按打开
         * @param url
         * @param isInputUrl 　区分是否是手动输入
         */
        void onSelect(String url, boolean isInputUrl);

        void onSelect(String url, boolean isInputUrl, String inputWord);
    }

    public interface UrlInputListener {

        void onDismiss();

        void onAction(String text, String extra, String source);

        void onCopySuggestion(String text);
    }
}
