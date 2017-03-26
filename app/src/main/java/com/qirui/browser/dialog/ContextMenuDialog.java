package com.qirui.browser.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.qirui.browser.R;
import com.qirui.browser.BrowserSettings;
import com.qirui.browser.util.DisplayUtils;

/**
 * Created by Luooh on 2017/3/24.
 */
public class ContextMenuDialog extends Dialog {

    private Context mContext;
    private View mContentView;
    private Resources mResource;
    private int mMenuItemMaxWidth = 0;
    private WebView.HitTestResult mResult;
    private WindowManager.LayoutParams mLayoutParams;
    private OnContextMenuClickListener mListener;
    // Only view images using these schemes
    private static final String[] IMAGE_VIEWABLE_SCHEMES = {
            "http",
            "https",
            "file"
    };

    public ContextMenuDialog(Context context) {
        super(context, R.style.dialogStyle);
        this.mContext = context;
        Window window = getWindow();
        mLayoutParams = window.getAttributes();
        mResource = mContext.getResources();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
    }

    private void initView(int type) {

        mContentView = View.inflate(mContext, R.layout.dialog_context_menu, null);
        LinearLayout menuParent = (LinearLayout) mContentView.findViewById(R.id.menu_parent);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.gravity = Gravity.CENTER_VERTICAL;
        int dividerHeight = mResource.getDimensionPixelOffset(R.dimen.context_menu_dialog_divider_height);
        int left_margin = mResource.getDimensionPixelOffset(R.dimen.context_menu_dialog_margin_left_right);
        int right_margin = mResource.getDimensionPixelOffset(R.dimen.context_menu_dialog_margin_left_right);
        int top_margin = mResource.getDimensionPixelOffset(R.dimen.context_menu_dialog_margin_top_btm);
        int bottom_margin = mResource.getDimensionPixelOffset(R.dimen.context_menu_dialog_margin_top_btm);
        TextView menuItem;
        int menuInfos[][] = new MenuItemInfo().getMenuItemInfo(type);
        for (int index = 0; index < menuInfos[0].length; index++) {
            menuItem = new TextView(mContext);
            menuItem.setId(menuInfos[0][index]);
            menuItem.setText(menuInfos[1][index]);
            menuItem.setTextColor(Color.WHITE);
            menuItem.setTextSize(14);
            menuItem.setGravity(Gravity.START);
            menuItem.setPadding(left_margin, top_margin, right_margin, bottom_margin);
            menuItem.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mListener.onMenuClick(v);
                }
            });
            menuParent.addView(menuItem, params);
            TextPaint paint = menuItem.getPaint();
            float len = paint.measureText(menuItem.getText().toString());
            if (mMenuItemMaxWidth < len) {
                mMenuItemMaxWidth = (int) len;
            }

            if (index != menuInfos[0].length - 1) {
                View divider = new View(mContext);
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(-1, dividerHeight);
                divider.setBackgroundColor(mResource.getColor(R.color.longclick_menu_divider_bg));
                menuParent.addView(divider, dividerParams);
            }
        }

        setContentView(mContentView);
    }

    public void setContextMenuClickListener(OnContextMenuClickListener listener) {
        this.mListener = listener;
    }

    public void show(WebView.HitTestResult result, int... loc) {
        this.mResult = result;

        initView(result.getType());

        mLayoutParams.x = loc[0];
        mLayoutParams.y = loc[1];
        int max = mResource.getDimensionPixelOffset(R.dimen.context_menu_dialog_maxwidth);
        int realWidth = mMenuItemMaxWidth + mResource.getDimensionPixelOffset(R.dimen.context_menu_dialog_margin_left_right) * 2;
        mLayoutParams.width = realWidth > max ? max : realWidth;

        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        int viewHeight = DisplayUtils.getHeightOfView(mContentView);
        int screenHeight = DisplayUtils.getScreenHeight(mContext);
        if (loc[1] + viewHeight > screenHeight - mResource.getDimension(R.dimen.toolbar_height) - DisplayUtils.getNavBarHeight(mContext)) {
            if (BrowserSettings.getInstance().getSettingValues().getFullscreenState()) {
                mLayoutParams.y = (int) (screenHeight - viewHeight - mResource.getDimension(R.dimen.toolbar_height));
            }
        }

        mLayoutParams.y = mLayoutParams.y < 0 ? 0 : mLayoutParams.y;
        int screenWidth = DisplayUtils.getScreenWidth(mContext);
        if (mLayoutParams.x > screenWidth) {
            mLayoutParams.x -= screenWidth;
        }
        if (mLayoutParams.x + mLayoutParams.width > screenWidth) {
            mLayoutParams.x = mLayoutParams.x - mLayoutParams.width;
            mLayoutParams.x = mLayoutParams.x < 0 ? 0 : mLayoutParams.x;
        }

        getWindow().setAttributes(mLayoutParams);
        show();
    }

    public static boolean isImageViewableUri(Uri uri) {
        String scheme = uri.getScheme();
        for (String allowed : IMAGE_VIEWABLE_SCHEMES) {
            if (allowed.equals(scheme)) {
                return true;
            }
        }
        return false;
    }

    public interface OnContextMenuClickListener{
        void onMenuClick(View view);
    }

    private class MenuItemInfo {

        private final int[][] PHONE_MENU = {
                { R.id.dial_context_menu_id, R.id.add_contact_context_menu_id, R.id.copy_phone_context_menu_id },
                { R.string.contextmenu_dial_dot, R.string.contextmenu_add_contact, R.string.contextmenu_copy }
        };

        private final int[][] EMAIL_MENU = {
                { R.id.email_context_menu_id, R.id.copy_mail_context_menu_id },
                { R.string.contextmenu_send_mail, R.string.contextmenu_copy }
        };

        private final int[][] GEO_MENU = {
                { R.id.map_context_menu_id, R.id.copy_geo_context_menu_id },
                { R.string.contextmenu_map, R.string.contextmenu_copy }
        };


        private final int[][] ANCHOR_MENU = {
                { R.id.open_context_menu_id, R.id.open_newtab_context_menu_id, R.id.save_link_context_menu_id, R.id.copy_link_context_menu_id },
                { R.string.contextmenu_openlink, R.string.contextmenu_openlink_newwindow, R.string.contextmenu_savelink, R.string.contextmenu_copylink }
        };

        private final int[][] IMAGE_MENU = {
                { R.id.download_context_menu_id, R.id.view_image_context_menu_id,
                  R.id.set_wallpaper_context_menu_id, R.id.share_link_context_menu_id, R.id.copy_link_context_menu_id },
                { R.string.contextmenu_download_image, R.string.contextmenu_view_image,
                  R.string.contextmenu_set_wallpaper, R.string.contextmenu_sharelink, R.string.contextmenu_copylink }
        };

        private final int[][] SELECT_IMAGE_ANCHOR_MENU = {
                { R.id.open_newtab_context_menu_id,
                  R.id.save_link_context_menu_id, R.id.copy_link_context_menu_id,
                  R.id.download_context_menu_id, R.id.view_image_context_menu_id,
                  R.id.set_wallpaper_context_menu_id, R.id.share_link_context_menu_id },
                { R.string.contextmenu_openlink_newwindow,
                  R.string.contextmenu_savelink, R.string.contextmenu_copylink,
                  R.string.contextmenu_download_image, R.string.contextmenu_view_image,
                  R.string.contextmenu_set_wallpaper, R.string.contextmenu_sharelink }
        };

        private final int[][] SELECT_TEXT_MENU = {
                { R.id.select_text_menu_id },
                { R.string.select_dot }
        };

        public int[][] getMenuItemInfo(int type) {
            switch (type) {
                case WebView.HitTestResult.PHONE_TYPE:
                    return PHONE_MENU;
                case WebView.HitTestResult.EMAIL_TYPE:
                    return EMAIL_MENU;
                case WebView.HitTestResult.GEO_TYPE:
                    return GEO_MENU;
                case WebView.HitTestResult.IMAGE_TYPE:
                    return IMAGE_MENU;
                case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!isImageViewableUri(Uri.parse(mResult.getExtra()))) {
                            return ANCHOR_MENU;
                        }
                        return SELECT_IMAGE_ANCHOR_MENU;
                    } else {
                        return IMAGE_MENU;
                    }
                case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                    return ANCHOR_MENU;
            }
            return null;
        }
    }
}