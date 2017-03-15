/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qirui.browser.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.browser.R;
import com.qirui.browser.util.DisplayUtils;
import java.util.Locale;

public class PagerSlidingTabStrip extends HorizontalScrollView implements View.OnTouchListener {

    public interface IconTabProvider {
        int getPageIconResId(int position);
    }

    public interface DrawableTabProvider {
        int getTextId(int position);
    }

    public interface OnTabItemClickListener {
        void getClickItem(View view, int position);
    }

    // @formatter:off
    private static final int[] ATTRS = new int[]{
            android.R.attr.textSize,
            android.R.attr.textColor
    };
    // @formatter:on

    private LinearLayout.LayoutParams mDefaultTabLayoutParams;
    private LinearLayout.LayoutParams mExpandedTabLayoutParams;

    private final PageListener mPageListener = new PageListener();
    public OnPageChangeListener mDelegatePageListener;
    public OnTabItemClickListener mTabItemClickListener;

    private LinearLayout mTabsContainer;
    private ViewPager mPagerView;

    private int mTabCount;

    private int mCurrentPosition = 0;
    private float mCurrentPositionOffset = 0f;

    private Paint mRectPaint;
    private Paint mDividerPaint;

    private int mIndicatorColor = 0xFF666666;
    private int mUnderlineColor = 0x1A000000;
    private int mDividerColor = 0x1A000000;

    private boolean mIsShouldExpand = false;
    private boolean mIsTextAllCaps = true;

    private int mScrollOffset = 0;
    private int mIndicatorHeight = 8;
    private int mUnderlineHeight = 2;
    private int mDividerPadding = 12;
    private int mTabPadding = 0;
    private int mTabPaddingTop = 3;
    private int mTabPaddingBottom = 3;
    private int mTabItemHeight = 0;
    private int mTabDivider = 0;
    private int mDividerWidth = 1;
    private int mTabItemWidth = DisplayUtils.getScreenWidth(getContext()) / 2;
    private int mCurrentColor = 0;

    private int[] mTabColors;
    private int mDefaultColor;

    private int mTabTextSize = 14;
    private int mTabTextColor = 0xFF666666;
    public Point mTouchPoint;
    public boolean mIsNeedTouchPoint = false;
//    private int mTabBackgroundResId = R.drawable.background_tab;

    private Locale mLocale;

    private int mTabType;

    private static final int TAB_TYPE_IMAGE = 0;
    private static final int TAB_TYPE_DRAWABLE = 1;

    public PagerSlidingTabStrip(Context context) {
        this(context, null);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setFillViewport(true);
        setWillNotDraw(false);

        mTabsContainer = new LinearLayout(context);
        mTabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        mTabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mTabsContainer.setGravity(Gravity.CENTER_VERTICAL);
        addView(mTabsContainer);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        mScrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mScrollOffset, dm);
        mIndicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mIndicatorHeight, dm);
        mUnderlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mUnderlineHeight, dm);
        mDividerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDividerPadding, dm);
        mTabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mTabPadding, dm);
        mDividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDividerWidth, dm);
        // get system attrs (android:textSize and android:textColor)
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip);

        mIndicatorColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsIndicatorColor, mIndicatorColor);
        mUnderlineColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsUnderlineColor, mUnderlineColor);
        mDividerColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsDividerColor, mDividerColor);
        mDefaultColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsDefaultColor, mDividerColor);
        mIndicatorHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsIndicatorHeight, mIndicatorHeight);
        mUnderlineHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsUnderlineHeight, mUnderlineHeight);
        mDividerPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsDividerPadding, mDividerPadding);
        mTabPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabPaddingLeftRight, mTabPadding);
        mTabItemWidth = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabItemWidth, mTabItemWidth);
        mTabPaddingTop = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabItemPaddingTop, mTabPaddingTop);
        mTabPaddingBottom = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabItemPaddingBottom, mTabPaddingBottom);
        mTabItemHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabItemHeight, mTabItemHeight);
//        mTabBackgroundResId = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsTabBackground, mTabBackgroundResId);
        mIsShouldExpand = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsShouldExpand, mIsShouldExpand);
        mTabDivider = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabDivider, mTabDivider);
        mScrollOffset = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsScrollOffset, mScrollOffset);
        mIsTextAllCaps = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsTextAllCaps, mIsTextAllCaps);

        a.recycle();

        mRectPaint = new Paint();
        mRectPaint.setAntiAlias(true);
        mRectPaint.setStyle(Style.FILL);

        mDividerPaint = new Paint();
        mDividerPaint.setAntiAlias(true);
        mDividerPaint.setStrokeWidth(mDividerWidth);

        mDefaultTabLayoutParams = new LinearLayout.LayoutParams(mTabItemWidth, LayoutParams.MATCH_PARENT);
        mExpandedTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mTouchPoint = new Point();
        if (mLocale == null) {
            mLocale = getResources().getConfiguration().locale;
        }
    }

    public void setOnTabItemClickListener(OnTabItemClickListener onTabItemClickListener) {
        this.mTabItemClickListener = onTabItemClickListener;
    }

    public void setIsNeedTouchPoint(boolean isNeedTouchPoint) {
        this.mIsNeedTouchPoint = isNeedTouchPoint;
    }

    public void setViewPager(ViewPager pager) {
        this.mPagerView = pager;

        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }

        pager.setOnPageChangeListener(mPageListener);

        notifyDataSetChanged();
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mDelegatePageListener = listener;
    }

    public void notifyDataSetChanged() {

        mTabsContainer.removeAllViews();
        mTabCount = mPagerView.getAdapter().getCount();
        for (int i = 0; i < mTabCount; i++) {
            if (mPagerView.getAdapter() instanceof IconTabProvider) {
                addIconImageTab(i, ((IconTabProvider) mPagerView.getAdapter()));
            } else if (mPagerView.getAdapter() instanceof DrawableTabProvider) {
                addDrawableTab(i, ((DrawableTabProvider) mPagerView.getAdapter()));
            }
        }
        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                mCurrentPosition = mPagerView.getCurrentItem();
                scrollToChild(mCurrentPosition, 0);
            }
        });
    }

    private void addIconImageTab(final int position, IconTabProvider iconTabProvider) {
        mTabType = TAB_TYPE_IMAGE;
        RelativeLayout tabLayout = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.tab_image, null);
        ((ImageView) tabLayout.findViewById(R.id.image_normal)).setImageResource(iconTabProvider.getPageIconResId(position * 2));
        ((ImageView) tabLayout.findViewById(R.id.image_selected)).setImageResource(iconTabProvider.getPageIconResId(position * 2 + 1));
        addTab(position, tabLayout, mTabType);
    }

    private void addDrawableTab(final int position, DrawableTabProvider drawableTabProvider) {
        mTabType = TAB_TYPE_DRAWABLE;
        LinearLayout tabLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.tab_text, null);
        TextView tabText = (TextView) tabLayout.findViewById(R.id.tv_tab);
        tabText.setText(drawableTabProvider.getTextId(position));
        addTab(position, tabLayout, mTabType);
    }

    private void addTab(final int position, View tab, int tabType) {
        tab.setFocusable(true);
        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTabItemClickListener != null) {
                    mTabItemClickListener.getClickItem(v, position);
                }
                mPagerView.setCurrentItem(position);
            }
        });
        tab.setOnTouchListener(this);
        if (mTabType == TAB_TYPE_DRAWABLE) {
            LinearLayout.LayoutParams tabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            if (position == 0) {
                tabLayoutParams.setMargins(mTabDivider, 0, mTabDivider / 2, 0);
            } else if (position == mTabCount - 1) {
                tabLayoutParams.setMargins(mTabDivider / 2, 0, mTabDivider, 0);
            } else {
                tabLayoutParams.setMargins(mTabDivider / 2, 0, mTabDivider / 2, 0);
            }
            tabLayoutParams.height = mTabItemHeight;

            mTabsContainer.addView(tab, position, tabLayoutParams);
        } else {
            tab.setPadding(mTabPadding, mTabPaddingTop, mTabPadding, mTabPaddingBottom);
            mTabsContainer.addView(tab, position, mIsShouldExpand ? mExpandedTabLayoutParams : mDefaultTabLayoutParams);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mIsNeedTouchPoint) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mTouchPoint.set((int) event.getRawX(), (int) event.getRawY());
            }
        }
        return false;
    }

    private void scrollToChild(int position, float viewPageoffset) {
        if (mTabCount == 0) {
            return;
        }

        int tabWith = 0;

        if (position == 0 || position == mTabCount - 1) {
            tabWith = mTabsContainer.getChildAt(position).getWidth() + mTabDivider * 3 / 2;
            if (position == 0) {
                mScrollOffset = (DisplayUtils.getScreenWidth(getContext()) - tabWith) / 2;
            }
        } else {
            tabWith = mTabsContainer.getChildAt(position).getWidth() + mTabDivider;
        }
        int offset = (int) (viewPageoffset * tabWith);


        int margin;
        if (position == 0) {
            margin = mTabDivider;
        } else {
            margin = mTabDivider / 2;

        }
        int newScrollX = mTabsContainer.getChildAt(position).getLeft() - margin + offset;
        newScrollX -= mScrollOffset;
        smoothScrollTo(newScrollX, 0);
    }

    private void updateTabColor(View tabView, float value) {
        updateIconImageAlpha(tabView, value);
    }

    private void updateIconImageAlpha(View tabView, float alpha) {
        if (tabView instanceof RelativeLayout
                && ((RelativeLayout) tabView).getChildAt(0) instanceof ImageView
                && ((RelativeLayout) tabView).getChildAt(1) instanceof ImageView) {
            (tabView).findViewById(R.id.image_normal).setAlpha(1 - alpha);
            (tabView).findViewById(R.id.image_selected).setAlpha(alpha);
        }
    }

    private void resetTab(int mCurrentPosition) {
        for (int i = 0; i < mTabCount; i++) {
            if (i != mCurrentPosition) {
                if (mTabType == TAB_TYPE_IMAGE) {
                    (mTabsContainer.getChildAt(i)).findViewById(R.id.image_normal).setAlpha(1);
                    (mTabsContainer.getChildAt(i)).findViewById(R.id.image_selected).setAlpha(0);
                } else if (mTabType == TAB_TYPE_DRAWABLE) {
                    (mTabsContainer.getChildAt(i)).getBackground().setColorFilter(mDefaultColor, PorterDuff.Mode.DST);
                }
            }else {
                if(mTabType == TAB_TYPE_DRAWABLE){
                    mCurrentColor = mTabColors[mCurrentPosition];
                    (mTabsContainer.getChildAt(i)).getBackground().setColorFilter(mCurrentColor, PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || mTabCount == 0) {
            return;
        }
        final int height = getHeight();
        // draw indicator line
        mRectPaint.setColor(mIndicatorColor);
        resetTab(mCurrentPosition);
        updateTabColor(mTabsContainer.getChildAt(mCurrentPosition), 1.0f);

        // default: line below current tab
        View currentTab = mTabsContainer.getChildAt(mCurrentPosition);
        float lineLeft = currentTab.getLeft();
        float lineRight = currentTab.getRight();

        // if there is an offset, start interpolating left and right coordinates between current and next tab
        if (mCurrentPositionOffset > 0f && mCurrentPosition < mTabCount - 1) {

            View nextTab = mTabsContainer.getChildAt(mCurrentPosition + 1);
            final float nextTabLeft = nextTab.getLeft();
            final float nextTabRight = nextTab.getRight();

            lineLeft = (mCurrentPositionOffset * nextTabLeft + (1f - mCurrentPositionOffset) * lineLeft);
            lineRight = (mCurrentPositionOffset * nextTabRight + (1f - mCurrentPositionOffset) * lineRight);

            if (mTabType == TAB_TYPE_DRAWABLE) {
                updateTabDrawableColor(currentTab, nextTab, mCurrentPositionOffset);
            }else {
                updateTabColor(currentTab, 1 - mCurrentPositionOffset);
                updateTabColor(nextTab, mCurrentPositionOffset);
            }
        }
        // draw line
        canvas.drawRect(getPaddingLeft() + lineLeft, height - mIndicatorHeight, lineRight + getPaddingLeft(), height, mRectPaint);
    }

    private void updateTabDrawableColor(View currentTab, View nextTab, float mCurrentPositionOffset) {
        mCurrentColor = mTabColors[mCurrentPosition];
        int nextColor = mTabColors[mCurrentPosition + 1];

        nextTab.getBackground().setColorFilter(DisplayUtils.changeColor(mDefaultColor, nextColor, mCurrentPositionOffset), PorterDuff.Mode.SRC_ATOP);
        currentTab.getBackground().setColorFilter(DisplayUtils.changeColor(mDefaultColor, mCurrentColor, 1 - mCurrentPositionOffset), PorterDuff.Mode.SRC_ATOP);
    }


    private class PageListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            mCurrentPosition = position;
            mCurrentPositionOffset = positionOffset;
            scrollToChild(position, positionOffset);
            invalidate();

            if (mDelegatePageListener != null) {
                mDelegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                scrollToChild(mPagerView.getCurrentItem(), 0);
            }

            if (mDelegatePageListener != null) {
                mDelegatePageListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mDelegatePageListener != null) {
                mDelegatePageListener.onPageSelected(position);
            }
        }

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = mCurrentPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public void setTabColors(int[] mTabColors, int postion) {
        this.mTabColors = mTabColors;
        if (mTabType == TAB_TYPE_DRAWABLE) {
            if (postion >= 0) {
                mCurrentPosition = postion;
            } else {
                mCurrentPosition = 0;
            }
            mTabsContainer.getChildAt(mCurrentPosition).getBackground().setColorFilter(mTabColors[mCurrentPosition], PorterDuff.Mode.SRC_ATOP);
        }
    }
}
