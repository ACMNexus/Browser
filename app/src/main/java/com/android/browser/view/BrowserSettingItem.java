package com.android.browser.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.browser.R;
import com.android.browser.util.DisplayUtils;

/**
 * Created by Luooh on 2017/2/16.
 */
public class BrowserSettingItem extends RelativeLayout {

    private Context mContext;
    private TextView mSettingTitle;
    private TextView mSettingDesc;
    private ImageView mSettingIcon;
    private TextView mSettingValue;
    private SwitchButton mSettingToggle;

    private static final int SETTING_NONE_SHOW = -1;
    private static final int SETTING_ONLY_DESC = 1;
    private static final int SETTING_ONLY_ICON = 2;
    private static final int SETTING_ONLY_VALUE = 3;
    private static final int SETTING_ONLY_TOGGLE = 4;
    private static final int SETTING_DESC_AND_TOGGLE = 5;
    private static final int SETTING_DESC_AND_VALUE = 6;

    public BrowserSettingItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BrowserSettingItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        this.mContext = context;
        setBackgroundResource(R.color.white);
        setLayoutParams(new RelativeLayout.LayoutParams(-1, DisplayUtils.dip2px(mContext, 60)));
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.layout_browser_setting_item, this);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SettingItemStyle);

        mSettingTitle = (TextView) contentView.findViewById(R.id.setting_title);
        mSettingDesc = (TextView) contentView.findViewById(R.id.setting_desc);
        mSettingIcon = (ImageView) contentView.findViewById(R.id.setting_icon);
        mSettingValue = (TextView) contentView.findViewById(R.id.setting_value);
        mSettingToggle = (SwitchButton) contentView.findViewById(R.id.setting_toggle);

        int state = Integer.parseInt(array.getString(R.styleable.SettingItemStyle_setting_state));
        String str_title = array.getString(R.styleable.SettingItemStyle_setting_title);
        int valueColorResId = array.getColor(R.styleable.SettingItemStyle_settingValueColor, -1);
        mSettingTitle.setText(str_title);

        if (state == SETTING_ONLY_DESC) {
            String str_desc = array.getString(R.styleable.SettingItemStyle_setting_desc);
            mSettingDesc.setText(str_desc);
        }

        if (state == SETTING_ONLY_ICON) {
            int resId = array.getResourceId(R.styleable.SettingItemStyle_setting_icon, 0);
            mSettingIcon.setImageResource(resId);
        }

        if (state == SETTING_ONLY_VALUE) {
            String str_value = array.getString(R.styleable.SettingItemStyle_setting_value);
            mSettingValue.setText(str_value);
        }

        if (state == SETTING_ONLY_TOGGLE) {
            boolean checked = array.getBoolean(R.styleable.SettingItemStyle_setting_checked, false);
            mSettingToggle.setChecked(checked);
        }

        if (state == SETTING_DESC_AND_TOGGLE) {
            String str_desc = array.getString(R.styleable.SettingItemStyle_setting_desc);
            mSettingDesc.setText(str_desc);

            boolean checked = array.getBoolean(R.styleable.SettingItemStyle_setting_checked, false);
            mSettingToggle.setChecked(checked);
        }

        if (state == SETTING_DESC_AND_VALUE) {
            String str_desc = array.getString(R.styleable.SettingItemStyle_setting_desc);
            mSettingDesc.setText(str_desc);

            String str_value = array.getString(R.styleable.SettingItemStyle_setting_value);
            mSettingValue.setText(str_value);
        }

        if(valueColorResId != -1) {
            mSettingValue.setTextColor(valueColorResId);
        }

        setItemState(state);

        array.recycle();
    }

    public void setSettingTitle(String title) {
        mSettingTitle.setText(title);
    }

    private void setItemState(int state) {
        switch (state) {
            case SETTING_NONE_SHOW:
                break;
            case SETTING_ONLY_DESC:
                mSettingDesc.setVisibility(View.VISIBLE);
                break;
            case SETTING_ONLY_ICON:
                mSettingIcon.setVisibility(View.VISIBLE);
                break;
            case SETTING_ONLY_VALUE:
                mSettingValue.setVisibility(View.VISIBLE);
                break;
            case SETTING_ONLY_TOGGLE:
                mSettingToggle.setVisibility(View.VISIBLE);
                break;
            case SETTING_DESC_AND_TOGGLE:
                mSettingDesc.setVisibility(View.VISIBLE);
                mSettingToggle.setVisibility(View.VISIBLE);
                break;
            case SETTING_DESC_AND_VALUE:
                mSettingDesc.setVisibility(View.VISIBLE);
                mSettingValue.setVisibility(View.VISIBLE);
                break;
        }
    }
}
