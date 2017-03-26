package com.qirui.browser.controller;

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
public class ContextMenuManager implements ContextMenuDialog.OnContextMenuClickListener {

    private Context mContext;
    private Controller mController;
    private WebView.HitTestResult mResult;
    private ContextMenuDialog mMenuDialog;
    private static ContextMenuManager sInstance;

    private ContextMenuManager() {
    }

    public void initial(Context context, Controller controller) {
        mContext = context;
        mController = controller;
    }

    public static ContextMenuManager getInstance() {
        if (sInstance == null) {
            sInstance = new ContextMenuManager();
        }
        return sInstance;
    }

    public void show(WebView.HitTestResult result, int... loc) {
        this.mResult = result;
        mMenuDialog = new ContextMenuDialog(mContext);
        mMenuDialog.setContextMenuClickListener(this);
        mMenuDialog.show(result, loc);
    }

    @Override
    public void onMenuClick(View v) {
        String extra = mResult.getExtra();
        if (TextUtils.isEmpty(extra)) {
            mMenuDialog.dismiss();
            return;
        }
        switch (v.getId()) {
            case R.id.dial_context_menu_id:
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_TEL + extra)));
                break;
            case R.id.add_contact_context_menu_id:
                addContact(extra);
                break;
            case R.id.copy_geo_context_menu_id:
            case R.id.copy_mail_context_menu_id:
            case R.id.copy_phone_context_menu_id:
            case R.id.copy_link_context_menu_id:
                copy(extra);
                break;
            case R.id.email_context_menu_id:
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_MAILTO + extra)));
                break;
            case R.id.map_context_menu_id:
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_GEO + URLEncoder.encode(extra))));
                break;
            case R.id.share_link_context_menu_id:
                //分享链接地址
                Utils.sharePage(mContext, null, mResult.getExtra(), null, null);
                break;
            case R.id.view_image_context_menu_id:
                Tab tab = mController.getTabControl().getCurrentTab();
                mController.openTab(mResult.getExtra(), tab, true, true);
                //预览图片
                break;
            case R.id.set_wallpaper_context_menu_id:
                //设置壁纸
                new WallpaperHandler(mContext, mResult.getExtra());
                break;
            case R.id.open_newtab_context_menu_id:
                mController.onContextItemSelected(R.id.open_newtab_context_menu_id);
                break;
            case R.id.open_context_menu_id:
                mController.onContextItemSelected(R.id.open_context_menu_id);
                break;
            case R.id.save_link_context_menu_id:
                mController.onContextItemSelected(R.id.save_link_context_menu_id);
                break;
            case R.id.download_context_menu_id:
                /*if (isImageViewableUri(Uri.parse(extra))) {
                    if (mController.getCurrentTab() != null && mController.getCurrentTab().getOriginalUrl() != null
                            && mController.getCurrentTab().getOriginalUrl().equals
                            (extra) && mController.getParentTab() != null && mController.getParentTab()
                            .getOriginalUrl() != null) {
                        String userAgent = null;
                        String referer = null;
                        try {
                            userAgent = mController.getCurrentWebView().getSettings().getUserAgentString();
                            referer = mController.getParentTab().getOriginalUrl();
                        } catch (Exception e) {
                        }

                        DownloadHandler.onDownloadStart(mController, (Activity) mContext, extra, userAgent, null, null, referer, false);
                    } else {
                        if (mController.getCurrentTab() != null) {
                            DownloadHandler.onDownloadStart(mController, (Activity) mContext, extra, null, null, null, mController.getCurrentTab().getOriginalUrl(), false);
                        }
                    }
                } else if (extra.startsWith("data:image/jpeg;base64")) {
                    saveBase64ImgToFile(extra);
                }*/
                break;
        }
        mMenuDialog.dismiss();
    }

    private void saveBase64ImgToFile(String imgStr) {
        /*imgStr = imgStr.substring(imgStr.indexOf(","));
        //TODO can't set download dir
        final Uri apkpath = Uri.withAppendedPath(Uri.fromFile(new File(BrowserSettings.getInstance().getDownloadPath())), "download_image" + System.currentTimeMillis() + ".jpg");
        if (FileUtils.GenerateImage(imgStr, apkpath.getPath())) {
            ToastUtils.show(mContext, apkpath.getPath() + "     " + mContext.getResources().getString(R.string.downloaded));
        } else {
            ToastUtils.show(mContext, "下载失败！！！");
        }*/
    }

    private void onpenLinkByNewTab() {
        boolean showNewTab = mController.getTabControl().canCreateNewTab();
    }

    public void openLink() {
    }

    private void addContact(String extra) {
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, Uri.decode(extra));
        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        mContext.startActivity(intent);
    }

    private void copy(CharSequence text) {
        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(text);
    }
}

