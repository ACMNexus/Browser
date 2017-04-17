package com.qirui.browser.fragments;

import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import com.qirui.browser.BookmarksLoader;
import com.qirui.browser.R;
import com.qirui.browser.activitys.AddBookMarkActivity;
import com.qirui.browser.activitys.MarkHistoryActivity;
import com.qirui.browser.adapter.BookMarkAdapter;
import com.qirui.browser.bean.BookMarkInfo;
import com.qirui.browser.util.IOUtils;
import com.qirui.browser.provider.BrowserContract;
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
    private MarkHistoryActivity mMarkHistory;
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
        mMarkHistory = (MarkHistoryActivity) getActivity();
        mAdapter = new BookMarkAdapter(mMarkHistory);
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
                    bookMarkInfo.setId(cursor.getInt(cursor.getColumnIndexOrThrow(BrowserContract.Bookmarks._ID)));
                    bookMarkInfo.setUrl(cursor.getString(cursor.getColumnIndexOrThrow(BrowserContract.Bookmarks.URL)));
                    bookMarkInfo.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(BrowserContract.Bookmarks.TITLE)));
                    bookMarkInfo.setParent(cursor.getInt(cursor.getColumnIndexOrThrow(BrowserContract.Bookmarks.PARENT)));
                    bookMarkInfo.setPosition(cursor.getInt(cursor.getColumnIndexOrThrow(BrowserContract.Bookmarks.POSITION)));
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
        mMarkHistory.openUrl(mAdapter.getItem(position).getUrl());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == AddBookMarkActivity.EDIT_BOOKMARK_REQUEST_CODE) {
//            String title = data.getStringExtra(BrowserContract.Bookmarks.TITLE);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
