package com.qirui.browser.controller;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import com.qirui.browser.BaseUi;
import com.qirui.browser.Controller;
import com.qirui.browser.R;
import com.qirui.browser.Tab;
import com.qirui.browser.TabControl;
import com.qirui.browser.adapter.HomePagerAdapter;
import com.qirui.browser.bean.WebSiteInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luooh on 2017/3/24.
 */
public class HomePagerController implements AbsListView.OnItemClickListener {

    private Tab mCurrentTab;
    private Context mContext;
    private EditText mEditUrl;
    private View mHomePagerView;
    private BaseUi mBaseUi;
    private TabControl mTabControl;
    private FrameLayout mHomeParent;
    private Controller mController;
    private HomePagerAdapter mAdapter;

    public HomePagerController(Context context) {
        this.mContext = context;

        initHomeView();
        initData();
    }

    public void initial(Controller controller, FrameLayout homeParent) {
        this.mController = controller;
        this.mHomeParent = homeParent;
        this.mBaseUi = (BaseUi) controller.getUi();
        this.mTabControl = controller.getTabControl();
    }

    private void initHomeView() {
        this.mHomePagerView = View.inflate(mContext, R.layout.layout_native_home_pager, null);
        GridView mWebSite = (GridView) mHomePagerView.findViewById(R.id.grid_website);
        mEditUrl = (EditText) mHomePagerView.findViewById(R.id.et_website);
        mAdapter = new HomePagerAdapter(mContext);
        mWebSite.setAdapter(mAdapter);
        mWebSite.setOnItemClickListener(this);
    }

    private void initData() {
        List<WebSiteInfo> list = new ArrayList();
        WebSiteInfo webSiteInfo;
        for(int i = 0; i < 16; i++) {
            webSiteInfo = new WebSiteInfo();
            webSiteInfo.setIconResId(R.drawable.filesystem_icon_apk);
            webSiteInfo.setName(mContext.getString(R.string.stop));
            webSiteInfo.setUrl("http://www.baidu.com");
            list.add(webSiteInfo);
        }
        mAdapter.setItems(list);
    }

    public void switchNativeHome(Tab tab) {
        this.mCurrentTab = tab;
        if (mCurrentTab != null && mCurrentTab.getWebView() != null) {
            mCurrentTab.getWebView().stopLoading();
            mCurrentTab.getWebView().clearFocus();
        }
        //显示主页信息
        if(mHomeParent.getChildCount() == 0) {
            mHomeParent.addView(getHomePageView());
        }
        mHomeParent.setVisibility(View.VISIBLE);
        mHomeParent.bringToFront();
        mTabControl.setCurrentTab(mCurrentTab);
    }

    public View getHomePageView() {
        return mHomePagerView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        WebSiteInfo info = mAdapter.getItem(position);
        mController.openTab(info.getUrl(), mCurrentTab, true, false);
    }
}
