package com.android.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Luooh on 2017/2/14.
 */

public class CustomTabView extends FrameLayout {

    private Context mContext;

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
}
