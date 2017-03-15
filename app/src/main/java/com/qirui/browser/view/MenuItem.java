package com.qirui.browser.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.browser.R;

/**
 * Created by Luooh on 2017/2/14.
 */
public class MenuItem extends LinearLayout {

    private Context mContext;
    private AttributeSet mAttrs;
    private ImageView mMenuIcon;
    private TextView mMenuName;

    public MenuItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MenuItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mAttrs = attrs;
        this.mContext = context;
        setGravity(Gravity.CENTER);
        setOrientation(LinearLayout.VERTICAL);
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.layout_menu_item, this);

        mMenuIcon = (ImageView) contentView.findViewById(R.id.menu_icon);
        mMenuName = (TextView) contentView.findViewById(R.id.menu_name);
        TypedArray typedArray = mContext.obtainStyledAttributes(mAttrs, R.styleable.MenuItemAttrs);
        String menuName = typedArray.getString(R.styleable.MenuItemAttrs_menu_name);
        int iconId = typedArray.getResourceId(R.styleable.MenuItemAttrs_menu_icon, R.drawable.menu_full_screen_normal);
        mMenuName.setText(menuName);
        mMenuIcon.setImageResource(iconId);
        typedArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setMenuIcon(int resId) {
        mMenuIcon.setImageResource(resId);
    }

    public void setMenuName(int textResId) {
        mMenuName.setText(textResId);
    }
}
