package com.android.browser.activitys;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.browser.R;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luooh on 2017/2/15.
 */

public class BaseActivity extends Activity implements View.OnClickListener, AbsListView.OnItemClickListener {

    protected Activity mActivity;
    protected ActionBar mActionBar;
    protected TextView mLeftTitleView;
    protected TextView mRigthTitleView;
    protected ImageView mRightiIcon;
    protected ImageView mLeftIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowFeature();
        this.mActivity = this;
        initActionBar();
    }

    protected void setWindowFeature() {
        this.requestWindowFeature(Window.FEATURE_ACTION_BAR);
    }

    protected void initActionBar() {
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            View view = View.inflate(this, R.layout.layout_title, null);
            mActionBar.setCustomView(view);

            mLeftIcon = (ImageView) view.findViewById(R.id.iv_left_icon);
            mLeftTitleView = (TextView) view.findViewById(R.id.tv_title_left);
            mRightiIcon = (ImageView) view.findViewById(R.id.iv_right_icon);
            mRigthTitleView = (TextView) view.findViewById(R.id.tv_title_right);

            mLeftIcon.setOnClickListener(this);
            mRightiIcon.setOnClickListener(this);
            mRigthTitleView.setOnClickListener(this);
            mLeftTitleView.setText(getTitle());
        }
    }

    @Override
    public void onClick(View view) {
        if(view == mLeftIcon) {
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    }

    protected void startNextPager(Class<?> clazz, Map<String, ? extends Serializable> map) {
        Intent intent = new Intent(this, clazz);
        if (map != null && map.size() > 0) {
            Set<String> keySets = map.keySet();
            for (String key : keySets) {
                intent.putExtra(key, map.get(key));
            }
        }
        startActivity(intent);
    }
}
