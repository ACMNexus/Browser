package com.android.browser.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.android.browser.R;

/**
 * Created by Luooh on 2017/2/14.
 */

public class CustomTabView extends FrameLayout {

    private Context mContext;
    private ViewPager mViewPager;

    public CustomTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mViewPager = (ViewPager) findViewById(R.id.sites);
    }
}
