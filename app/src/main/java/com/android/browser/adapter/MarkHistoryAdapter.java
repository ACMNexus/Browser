package com.android.browser.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

import com.android.browser.BrowserBookmarksPage;
import com.android.browser.R;
import com.android.browser.fragments.BaseFragment;
import com.android.browser.fragments.BookMarkFragment;
import com.android.browser.fragments.HistoryFragment;
import com.android.browser.view.PagerSlidingTabStrip;

/**
 * Created by Luooh on 2017/3/1.
 */
public class MarkHistoryAdapter extends FragmentPagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

    private int mTabImages[];
    private Context mContext;
    private SparseArray<BaseFragment> mfragments;

    public MarkHistoryAdapter(FragmentManager fm) {
        super(fm);
    }

    public MarkHistoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.mContext = context;
        mfragments = new SparseArray<>();
        TypedArray ar = mContext.getResources().obtainTypedArray(R.array.tab_images);
        int len = ar.length();
        mTabImages = new int[len];
        for (int i = 0; i < len; i++) {
            mTabImages[i] = ar.getResourceId(i, 0);
        }
        ar.recycle();
    }

    @Override
    public Fragment getItem(int position) {
        BaseFragment baseFragment = mfragments.get(position);
        if(baseFragment == null) {
            switch (position) {
                case 0:
//                    baseFragment = BookMarkFragment.newInstance();
                    baseFragment = new BrowserBookmarksPage();
                    break;
                case 1:
                    baseFragment = HistoryFragment.newInstance();
                    break;
            }
            mfragments.put(position, baseFragment);
        }
        return baseFragment;
    }

    @Override
    public int getCount() {
        return mTabImages.length / 2;
    }

    @Override
    public int getPageIconResId(int position) {
        return mTabImages[position];
    }
}
