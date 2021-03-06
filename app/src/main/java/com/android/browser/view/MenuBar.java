package com.android.browser.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.browser.BaseUi;
import com.android.browser.PhoneUi;
import com.android.browser.R;
import com.android.browser.UiController;
import com.android.browser.util.DisplayUtils;

/**
 * Created by Luooh on 2017/2/7.
 */
public class MenuBar extends LinearLayout implements View.OnClickListener {

    private BaseUi mBaseUi;
    private Context mContext;
    private UiController mUiController;
    private ImageView mMenuTools;
    private ImageView mMenuBack;
    private ImageView mMenuForwards;
    private ImageView mMenuHome;
    private TextView mTabCounts;

    public MenuBar(Context context, UiController controller, BaseUi baseUi, ViewGroup contentView) {
        super(context, null);
        this.mContext = context;
        this.mBaseUi = baseUi;
        this.mUiController = controller;

        initLayout();
        setFixBottomMenuTools();
    }

    private void initLayout() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(R.layout.navigationbar, this);
        setOrientation(LinearLayout.HORIZONTAL);
        setBackgroundResource(R.color.white);
        setLayoutParams(new LinearLayout.LayoutParams(-1, DisplayUtils.dip2px(mContext, 48)));
        mMenuBack = (ImageView) findViewById(R.id.menu_back);
        mMenuHome = (ImageView) findViewById(R.id.menu_home);
        mMenuTools = (ImageView) findViewById(R.id.menu_tools);
        mTabCounts = (TextView) findViewById(R.id.tabCount);
        mMenuForwards = (ImageView) findViewById(R.id.menu_forward);

        mMenuBack.setOnClickListener(this);
        mMenuHome.setOnClickListener(this);
        mMenuTools.setOnClickListener(this);
        mMenuForwards.setOnClickListener(this);
        findViewById(R.id.switch_tab).setOnClickListener(this);
    }

    private void setFixBottomMenuTools() {
        if(getParent() == null) {
            mBaseUi.addBottomMenuTools(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_back:
                backPager();
                break;
            case R.id.menu_forward:
                forwardPager();
                break;
            case R.id.menu_tools:
                showToolMenu();
                break;
            case R.id.menu_home:
                break;
            case R.id.switch_tab:
                switchTab();
                break;
        }
    }

    private void backPager() {
        WebView webView = mBaseUi.getActivieWebView();
        if(webView != null && webView.canGoBack()) {
            webView.goBack();
        }
    }

    private void forwardPager() {
        WebView webView = mBaseUi.getActivieWebView();
        if(webView != null && webView.canGoForward()) {
            webView.goForward();
        }
    }

    private void switchTab() {
        ((PhoneUi) mBaseUi).toggleNavScreen();
    }

    private void showToolMenu() {
        mBaseUi.showPopMenuTool();
    }
}
