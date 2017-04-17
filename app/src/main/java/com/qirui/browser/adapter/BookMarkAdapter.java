package com.qirui.browser.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.qirui.browser.R;
import com.qirui.browser.activitys.AddBookMarkActivity;
import com.qirui.browser.bean.BookMarkInfo;
import com.qirui.browser.provider.BrowserContract;

/**
 * Created by Luooh on 2017/3/1.
 */
public class BookMarkAdapter extends KBaseAdapter<BookMarkInfo> {

    private boolean mIsEditMode;
    private Activity mActivity;

    public BookMarkAdapter(Context context) {
        super(context);
        if(mContext instanceof  Activity) {
            mActivity = (Activity) context;
        }
    }

    @Override
    public View getView(int position, View contentView, ViewGroup parent) {
        ViewHolder holder;
        final BookMarkInfo bookMarkInfo = getItem(position);
        if (contentView == null) {
            holder = new ViewHolder();
            contentView = View.inflate(mContext, R.layout.layout_bookmark_item, null);
            holder.TitleView = (TextView) contentView.findViewById(R.id.title);
            holder.IconView = (ImageView) contentView.findViewById(R.id.favicon);
            holder.checkState = (CheckBox) contentView.findViewById(R.id.checked);
            holder.editView = (ImageView) contentView.findViewById(R.id.edit);
            contentView.setTag(holder);
        } else {
            holder = (ViewHolder) contentView.getTag();
        }
        holder.TitleView.setText(bookMarkInfo.getTitle());
        holder.editView.setVisibility(mIsEditMode ? View.VISIBLE : View.GONE);
        holder.checkState.setVisibility(mIsEditMode ? View.VISIBLE : View.GONE);
        holder.editView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBookMark(bookMarkInfo);
            }
        });
        return contentView;
    }

    private void editBookMark(BookMarkInfo bookMarkInfo) {
        Intent intent = new Intent(mContext, AddBookMarkActivity.class);
        intent.putExtra(BrowserContract.Bookmarks._ID, bookMarkInfo.getId());
        intent.putExtra(BrowserContract.Bookmarks.URL, bookMarkInfo.getUrl());
        intent.putExtra(BrowserContract.Bookmarks.TITLE, bookMarkInfo.getTitle());
        intent.putExtra(BrowserContract.Bookmarks.EXTRA_EDIT_BOOKMARK, "edit_bookmark");
        if(mActivity != null) {
            mActivity.startActivityForResult(intent, 1001);
        }
    }

    public void setEditMode(boolean isEdit) {
        mIsEditMode = isEdit;
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView TitleView;
        ImageView IconView;
        ImageView editView;
        CheckBox checkState;
    }
}
