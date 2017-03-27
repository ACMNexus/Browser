package com.qirui.browser.controller;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import com.qirui.browser.Controller;
import com.qirui.browser.R;
import com.qirui.browser.Tab;

/**
 * Created by Luooh on 2017/3/24.
 */
public class HomePagerController implements AbsListView.OnItemClickListener {

    private Tab mCurrentTab;
    private Context mContext;
    private EditText mEditUrl;
    private View mHomePagerView;
    private FrameLayout mHomeParent;
    private Controller mController;

    public HomePagerController(Context context, Controller controller) {
        this.mContext = context;
        this.mController = controller;

        initHomeView();
    }

    private void initHomeView() {
        this.mHomePagerView = View.inflate(mContext, R.layout.layout_native_home_pager, null);
        GridView mWebSite = (GridView) mHomePagerView.findViewById(R.id.grid_website);
        mEditUrl = (EditText) mHomePagerView.findViewById(R.id.et_website);
        mWebSite.setOnItemClickListener(this);
    }

    public void switchTab(Tab tab) {
        this.mCurrentTab = tab;
        if(mCurrentTab == null) {
            return;
        }
    }

    public void showHomePager() {
        if(mHomeParent != null) {
            if(mHomePagerView.getParent() == null) {
                mHomeParent.addView(mHomePagerView);
            }
        }
        if(mHomeParent.getVisibility() == View.GONE) {
            mHomeParent.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }
}
