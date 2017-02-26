package com.android.browser.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.browser.BaseUi;
import com.android.browser.R;
import com.android.browser.UiController;
import com.android.browser.BrowserPreferencesPage;
import com.android.browser.activitys.BrowserSettingActivity;
import com.android.browser.activitys.DownloadFileActivity;
import com.android.browser.util.ActivityUtils;
import com.android.browser.util.DisplayUtils;

/**
 * Created by Luooh on 2017/2/15.
 */
public class MenuToolBar extends RelativeLayout implements View.OnClickListener {

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

    private View mMenuShadow;
    private LinearLayout mMenuParent;

    private AnimatorSet showAnimatorSet;
    private AnimatorSet dismissAimatorSet;

    public MenuToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        setListener();
    }

    public MenuToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        setListener();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initAnimator();
    }

    public void init(BaseUi baseUi, UiController uiController) {
        this.mBaseUI = baseUi;
        this.mUiController = uiController;
    }

    private void initView(Context context) {
        this.mContext = context;
        setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
        params.setMargins(0, DisplayUtils.dip2px(mContext, 48.5f), 0, 0);
        setLayoutParams(params);
        View contentView = inflate(mContext, R.layout.layout_menu_setting, this);
        mMenuNightMode = (ImageView) contentView.findViewById(R.id.menu_night_type);
        mMenuNoPicture = (ImageView) contentView.findViewById(R.id.menu_pic_type);
        mMenuFullScreen = (ImageView) contentView.findViewById(R.id.menu_fullscreen_type);
        mMenuTraceMode = (ImageView) contentView.findViewById(R.id.menu_trace_type);

        mMenuShadow = contentView.findViewById(R.id.common_menu_shadow);
        mMenuParent = (LinearLayout) contentView.findViewById(R.id.commen_menu_parent);

        mMenuAddBookMark = (MenuItem) contentView.findViewById(R.id.menu_addbookmark);
        mMenuHistory = (MenuItem) contentView.findViewById(R.id.menu_history);
        mMenuDownload = (MenuItem) contentView.findViewById(R.id.menu_download);
        mMenuRefresh = (MenuItem) contentView.findViewById(R.id.menu_refresh);
        mMenuSetting = (MenuItem) contentView.findViewById(R.id.menu_setting);
        mMenuShare = (MenuItem) contentView.findViewById(R.id.menu_share);
        mMenuTools = (MenuItem) contentView.findViewById(R.id.menu_tools);
        mMenuExit = (MenuItem) contentView.findViewById(R.id.menu_exit);

        initAnimator();
    }

    private void initAnimator() {
        ObjectAnimator menuShadowAnimator = ObjectAnimator.ofFloat(mMenuShadow, "alpha", 0.0f, 1.0f);
        ObjectAnimator menuToolBarAnimator = ObjectAnimator.ofFloat(this, "alpha", 0.0f, 1.0f);
        ObjectAnimator menuTranslatXAnimator = ObjectAnimator.ofFloat(mMenuParent, "translationX", 0.0f, 0.0f);

        showAnimatorSet = new AnimatorSet();
        showAnimatorSet.playTogether(menuShadowAnimator, menuToolBarAnimator, menuTranslatXAnimator);
        showAnimatorSet.setDuration(100);

        ObjectAnimator menuShadowDismissAnimator = ObjectAnimator.ofFloat(mMenuShadow, "alpha", 1.0f, 0.0f);
        ObjectAnimator menuToolBarDismissAnimator = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.0f);
        dismissAimatorSet = new AnimatorSet();
        dismissAimatorSet.playTogether(menuShadowDismissAnimator, menuToolBarDismissAnimator);
        dismissAimatorSet.setDuration(100);
        dismissAimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
    }

    private void setListener() {
        mMenuNightMode.setOnClickListener(this);
        mMenuNoPicture.setOnClickListener(this);
        mMenuFullScreen.setOnClickListener(this);
        mMenuTraceMode.setOnClickListener(this);

        mMenuAddBookMark.setOnClickListener(this);
        mMenuHistory.setOnClickListener(this);
        mMenuDownload.setOnClickListener(this);
        mMenuRefresh.setOnClickListener(this);
        mMenuSetting.setOnClickListener(this);
        mMenuShare.setOnClickListener(this);
        mMenuTools.setOnClickListener(this);
        mMenuExit.setOnClickListener(this);

        mMenuShadow.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        setVisibility(View.GONE);
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
                mBaseUI.getWebView().reload();
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
            case R.id.common_menu_shadow:
                showPopMenu(View.GONE);
                break;
        }
    }

    public void showPopMenu(int state) {
        if(state == View.VISIBLE) {
            setVisibility(state);
            showAnimatorSet.start();
        }else {
            dismissAimatorSet.start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
