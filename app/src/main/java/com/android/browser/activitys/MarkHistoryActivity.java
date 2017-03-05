package com.android.browser.activitys;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;

import com.android.browser.CombinedBookmarksCallbacks;
import com.android.browser.R;
import com.android.browser.adapter.MarkHistoryAdapter;
import com.android.browser.view.PagerSlidingTabStrip;

/**
 * Created by Luooh on 2017/2/28.
 */
public class MarkHistoryActivity extends BaseActivity implements CombinedBookmarksCallbacks {

    public static final String EXTRA_CURRENT_URL = "url";
    public static final String EXTRA_COMBO_ARGS = "combo_args";
    public static final String EXTRA_INITIAL_VIEW = "initial_view";

    private ViewPager mViewPager;
    private PagerSlidingTabStrip mTabStrip;
    private MarkHistoryAdapter mHistoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_bookmark_page);

        initView();
        setListener();
    }

    private void initView() {
        mTitleBarDivider.setBackgroundResource(R.color.transparent);
        mViewPager = (ViewPager) findViewById(R.id.viewpage);
        mTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mHistoryAdapter = new MarkHistoryAdapter(mActivity, getSupportFragmentManager());
        mViewPager.setAdapter(mHistoryAdapter);
        mTabStrip.setViewPager(mViewPager);
    }

    private void setListener() {
        mTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mLeftTitleView.setText(R.string.tab_bookmarks);
                } else {
                    mLeftTitleView.setText(R.string.tab_history);
                }
            }

            @Override
            public void onPageScrollStateChanged(int position) {
            }
        });
    }

    @Override
    public void openUrl(String url) {
    }

    @Override
    public void openInNewTab(String... urls) {
    }

    @Override
    public void openSnapshot(long id) {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
