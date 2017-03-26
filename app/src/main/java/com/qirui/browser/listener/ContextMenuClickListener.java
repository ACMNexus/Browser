package com.qirui.browser.listener;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import com.qirui.browser.Controller;
import com.qirui.browser.R;
import com.qirui.browser.Tab;
import com.qirui.browser.WallpaperHandler;
import com.qirui.browser.dialog.ContextMenuDialog;
import com.qirui.browser.util.Utils;
import java.net.URLEncoder;

/**
 * Created by Luooh on 2017/3/24.
 */
public class ContextMenuClickListener implements View.OnClickListener {

    private Context mContext;
    private Controller mController;
    private ContextMenuDialog mDialog;
    private WebView.HitTestResult mResult;

    public ContextMenuClickListener(Context context, Controller controller, WebView.HitTestResult result, ContextMenuDialog dialog) {
        this.mResult = result;
        this.mDialog = dialog;
        this.mContext = context;
        this.mController = controller;
    }

    @Override
    public void onClick(View v) {
    }

}
