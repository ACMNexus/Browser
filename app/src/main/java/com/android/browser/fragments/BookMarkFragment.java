package com.android.browser.fragments;

import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import com.android.browser.BookmarksLoader;
import com.android.browser.R;
import com.android.browser.adapter.BookMarkAdapter;
import com.android.browser.bean.BookMarkInfo;
import com.android.browser.provider.BrowserContract.Bookmarks;
import com.android.browser.util.IOUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luooh on 2017/3/1.
 */
public class BookMarkFragment extends BaseFragment implements AbsListView.OnItemLongClickListener {

    private View mEmptyView;
    private ListView mListView;
    private BookMarkAdapter mAdapter;
    private boolean mIsEditMode = false;
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
        mContentview.findViewById(R.id.clear).setOnClickListener(this);
        mListView = (ListView) mContentview.findViewById(R.id.bookmarks);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mListView.setAdapter(mAdapter);
    }

    private void initData() {
        mLoaderManager.restartLoader(LOADER_BOOKMARKS, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id >= LOADER_BOOKMARKS) {
            return new BookmarksLoader(mActivity);
        } else {
            throw new UnsupportedOperationException("Unknown loader id " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(loader.getId() == LOADER_BOOKMARKS) {
            if(IOUtils.isValidCursor(cursor)) {
                mEmptyView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
                BookMarkInfo bookMarkInfo;
                List<BookMarkInfo> list = new ArrayList();
                while (cursor.moveToNext()) {
                    bookMarkInfo = new BookMarkInfo();
                    bookMarkInfo.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Bookmarks._ID)));
                    bookMarkInfo.setUrl(cursor.getString(cursor.getColumnIndexOrThrow(Bookmarks.URL)));
                    bookMarkInfo.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(Bookmarks.TITLE)));
                    bookMarkInfo.setParent(cursor.getInt(cursor.getColumnIndexOrThrow(Bookmarks.PARENT)));
                    bookMarkInfo.setPosition(cursor.getInt(cursor.getColumnIndexOrThrow(Bookmarks.POSITION)));
                    list.add(bookMarkInfo);
                }
                mAdapter.setItems(list);
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.clear:
                break;
        }
    }

    public void cancelEditMode() {
        mIsEditMode = false;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.setEditMode(mIsEditMode);
        mIsEditMode = !mIsEditMode;
        return true;
    }

    @Override
    public void onDestroy() {
        mLoaderManager.destroyLoader(LOADER_BOOKMARKS);
        super.onDestroy();
    }
}
