package com.android.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.browser.BaseUi;
import com.android.browser.BrowserPreferencesPage;
import com.android.browser.BrowserSettings;
import com.android.browser.R;
import com.android.browser.UiController;
import com.android.browser.activitys.BrowserSettingActivity;
import com.android.browser.activitys.DownloadFileActivity;
import com.android.browser.util.ActivityUtils;
import com.android.browser.util.DisplayUtils;

/**
 * Created by Luooh on 2017/2/15.
 */

public class MenuToolBar extends LinearLayout implements View.OnClickListener {

    private Context mContext;
    private BaseUi mBaseUI;
    private UiController mUiController;

    private ImageView mMenuNightMode;
    private ImageView mMenuNoPicture;
    private ImageView mMenuFullScreen;
    private ImageView mMenuTraceMode;

    private MenuItem mMenuAddBookMark;
    private MenuItem mMenuHistory;
    private MenuItem mMenuDownload;
    private MenuItem mMenuRefresh;
    private MenuItem mMenuSetting;
    private MenuItem mMenuShare;
    private MenuItem mMenuTools;
    private MenuItem mMenuExit;

    public MenuToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        setListener();
    }

    public MenuToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void init(BaseUi baseUi, UiController uiController) {
        this.mBaseUI = baseUi;
        this.mUiController = uiController;
    }

    private void initView(Context context) {
        this.mContext = context;
        setGravity(Gravity.BOTTOM);
        setOrientation(LinearLayout.VERTICAL);
        setBackgroundResource(R.color.white);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, DisplayUtils.dip2px(mContext, 48));
        setLayoutParams(params);
        View contentView = inflate(mContext, R.layout.layout_menu_setting, this);
        mMenuNightMode = (ImageView) contentView.findViewById(R.id.menu_night_type);
        mMenuNoPicture = (ImageView) contentView.findViewById(R.id.menu_pic_type);
        mMenuFullScreen = (ImageView) contentView.findViewById(R.id.menu_fullscreen_type);
        mMenuTraceMode = (ImageView) contentView.findViewById(R.id.menu_trace_type);

        mMenuAddBookMark = (MenuItem) contentView.findViewById(R.id.menu_addbookmark);
        mMenuHistory = (MenuItem) contentView.findViewById(R.id.menu_history);
        mMenuDownload = (MenuItem) contentView.findViewById(R.id.menu_download);
        mMenuRefresh = (MenuItem) contentView.findViewById(R.id.menu_refresh);
        mMenuSetting = (MenuItem) contentView.findViewById(R.id.menu_setting);
        mMenuShare = (MenuItem) contentView.findViewById(R.id.menu_share);
        mMenuTools = (MenuItem) contentView.findViewById(R.id.menu_tools);
        mMenuExit = (MenuItem) contentView.findViewById(R.id.menu_exit);
    }

    private void setListener() {
        mMenuNightMode.setOnClickListener(this);
        mMenuNoPicture.setOnClickListener(this);
        mMenuFullScreen.setOnClickListener(this);
        mMenuTraceMode.setOnClickListener(this);

        mMenuDownload.setOnClickListener(this);
        mMenuTools.setOnClickListener(this);
        mMenuSetting.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_night_type:
                break;
            case R.id.menu_pic_type:
                ActivityUtils.startNextPager(mContext, BrowserPreferencesPage.class);
                break;
            case R.id.menu_fullscreen_type:
                break;
            case R.id.menu_trace_type:
                break;
            case R.id.menu_addbookmark:
                break;
            case R.id.menu_history:
                break;
            case R.id.menu_download:
                ActivityUtils.startNextPager(mContext, DownloadFileActivity.class);
                break;
            case R.id.menu_refresh:
                break;
            case R.id.menu_setting:
                ActivityUtils.startNextPager(mContext, BrowserSettingActivity.class);
                break;
            case R.id.menu_share:
                break;
            case R.id.menu_tools:
                break;
            case R.id.menu_exit:
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
