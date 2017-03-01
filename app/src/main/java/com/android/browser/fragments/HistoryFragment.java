package com.android.browser.fragments;

import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.android.browser.CombinedBookmarksCallbacks;
import com.android.browser.HistoryItem;
import com.android.browser.R;
import com.android.browser.adapter.HistoryAdapter;
import com.android.browser.provider.BrowserContract;
import com.android.browser.util.ToastUtils;

/**
 * Created by Luooh on 2017/3/1.
 */
public class HistoryFragment extends BaseFragment implements ExpandableListView.OnChildClickListener {

    private View mEmptyView;
    private View mBottomBar;
    private ExpandableListView mHistoryListView;

    private ClearHistoryTask mClearThread;
    private HistoryAdapter mHistoryAdapter;
    private CombinedBookmarksCallbacks mCallback;

    private static final int LOADER_HISTORY = 1;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentview = inflater.inflate(R.layout.fragment_history, container, false);
        init();
        initView();
        return mContentview;
    }

    private void init() {
        mCallback = (CombinedBookmarksCallbacks) getActivity();
        ContentResolver resolver = getActivity().getContentResolver();
        mClearThread = new ClearHistoryTask(resolver);
        mHistoryAdapter = new HistoryAdapter(mActivity);
        getActivity().getLoaderManager().restartLoader(LOADER_HISTORY, null, this);
    }

    private void initView() {
        mEmptyView = mContentview.findViewById(R.id.empty);
        mBottomBar = mContentview.findViewById(R.id.bottomBar);
        mHistoryListView = (ExpandableListView) mContentview.findViewById(R.id.history);
        mHistoryListView.setAdapter(mHistoryAdapter);
        mContentview.findViewById(R.id.clear).setOnClickListener(this);
        mHistoryListView.setOnChildClickListener(this);
        //设置 属性 GroupIndicator 去掉默认向下的箭头
        mHistoryListView.setGroupIndicator(null);
        mHistoryListView.expandGroup(0);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri.Builder combinedBuilder = BrowserContract.Combined.CONTENT_URI.buildUpon();
        switch (id) {
            case LOADER_HISTORY: {
                String sort = BrowserContract.Combined.DATE_LAST_VISITED + " DESC";
                String where = BrowserContract.Combined.VISITS + " > 0";
                CursorLoader loader = new CursorLoader(getActivity(), combinedBuilder.build(), HistoryQuery.PROJECTION, where, null, sort);
                return loader;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_HISTORY: {
                if(cursor != null && cursor.getCount() > 0) {
                    mHistoryAdapter.changeCursor(cursor);
                    mHistoryListView.setVisibility(View.VISIBLE);
                    mBottomBar.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.clear:
                mClearThread.start();
                break;
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
        mCallback.openUrl(((HistoryItem) view).getUrl());
        return true;
    }

    public static class ClearHistoryTask extends Thread {
        ContentResolver mResolver;

        public ClearHistoryTask(ContentResolver resolver) {
            mResolver = resolver;
        }

        @Override
        public void run() {
            Browser.clearHistory(mResolver);
        }
    }

    public interface HistoryQuery {
        String[] PROJECTION = new String[]{
                BrowserContract.Combined._ID, // 0
                BrowserContract.Combined.DATE_LAST_VISITED, // 1
                BrowserContract.Combined.TITLE, // 2
                BrowserContract.Combined.URL, // 3
                BrowserContract.Combined.FAVICON, // 4
                BrowserContract.Combined.VISITS, // 5
                BrowserContract.Combined.IS_BOOKMARK, // 6
        };

        int INDEX_ID = 0;
        int INDEX_DATE_LAST_VISITED = 1;
        int INDEX_TITE = 2;
        int INDEX_URL = 3;
        int INDEX_FAVICON = 4;
        int INDEX_VISITS = 5;
        int INDEX_IS_BOOKMARK = 6;
    }

    @Override
    public void onDestroy() {
        getLoaderManager().destroyLoader(LOADER_HISTORY);
        super.onDestroy();
    }
}
