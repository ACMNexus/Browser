package com.qirui.browser.fragments;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

/**
 * Created by Luooh on 2017/3/1.
 */
public abstract class BaseFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>,
            AbsListView.OnItemClickListener {

    protected Activity mActivity;
    protected View mContentview;
    protected LoaderManager mLoaderManager;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
        mLoaderManager = mActivity.getLoaderManager();
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
