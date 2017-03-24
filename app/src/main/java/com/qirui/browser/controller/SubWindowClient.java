package com.qirui.browser.controller;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.qirui.browser.WebViewController;

/**
 * Created by Luooh on 2017/3/24.
 * Subclass of WebViewClient used in subwindows to notify the main
 * WebViewClient of certain WebView activities.
 */
public class SubWindowClient extends WebViewClient {

    // The main WebViewClient.
    private final WebViewClient mClient;
    private final WebViewController mController;

    public SubWindowClient(WebViewClient client, WebViewController controller) {
        mClient = client;
        mController = controller;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // Unlike the others, do not call mClient's version, which would
        // change the progress bar.  However, we do want to remove the
        // find or select dialog.
        mController.endActionMode();
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        mClient.doUpdateVisitedHistory(view, url, isReload);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return mClient.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        mClient.onReceivedSslError(view, handler, error);
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        mClient.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        mClient.onFormResubmission(view, dontResend, resend);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        mClient.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, android.view.KeyEvent event) {
        return mClient.shouldOverrideKeyEvent(view, event);
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, android.view.KeyEvent event) {
        mClient.onUnhandledKeyEvent(view, event);
    }
}
