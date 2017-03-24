package com.qirui.browser.controller;

import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.qirui.browser.Tab;
import com.qirui.browser.WebViewController;

/**
 * Created by Luooh on 2017/3/24.
 * WebChromeClient implementation for the sub window
 */
public class SubWindowChromeClient extends WebChromeClient {

    private Tab mTab;
    private WebView mSubView;
    // The main WebChromeClient.
    private final WebChromeClient mClient;
    private WebViewController mWebViewController;

    public SubWindowChromeClient(Tab tab, WebChromeClient client) {
        mTab = tab;
        mClient = client;
    }

    public void setWebViewController(WebViewController webViewController){
        mWebViewController = webViewController;
    }

    public void setSubView(WebView subView) {
        this.mSubView = subView;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        mClient.onProgressChanged(view, newProgress);
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, android.os.Message resultMsg) {
        return mClient.onCreateWindow(view, dialog, userGesture, resultMsg);
    }

    @Override
    public void onCloseWindow(WebView window) {
        if (window != mSubView) {
            Log.e("Tab", "Can't close the window");
        }
        mWebViewController.dismissSubWindow(mTab);
    }
}
