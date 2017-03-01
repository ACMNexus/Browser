package com.android.browser.fragments;

import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.browser.R;
import com.android.browser.adapter.BookMarkAdapter;

/**
 * Created by Luooh on 2017/3/1.
 */
public class BookMarkFragment extends BaseFragment implements AbsListView.OnItemLongClickListener {

    private View mEmptyView;
    private ListView mListView;
    private BookMarkAdapter mAdapter;
    private static final int LOADER_BOOKMARKS = 100;

    public static BaseFragment newInstance() {
        return new BookMarkFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentview = inflater.inflate(R.layout.fragment_bookmark, container, false);
        initView();
        initData();
        return mContentview;
    }

    private void initView() {
        mAdapter = new BookMarkAdapter(mActivity);
        mEmptyView = mContentview.findViewById(R.id.empty);
        mListView = (ListView) mContentview.findViewById(R.id.bookmarks);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
    }

    private void initData() {
        mLoaderManager.restartLoader(LOADER_BOOKMARKS, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id >= LOADER_BOOKMARKS) {
            return null;
        } else {
            throw new UnsupportedOperationException("Unknown loader id " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        super.onLoadFinished(loader, data);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    public void onDestroy() {
        mLoaderManager.destroyLoader(LOADER_BOOKMARKS);
        super.onDestroy();
    }
}
