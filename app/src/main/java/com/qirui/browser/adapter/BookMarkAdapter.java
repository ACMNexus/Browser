package com.qirui.browser.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.browser.R;
import com.qirui.browser.bean.BookMarkInfo;

/**
 * Created by Luooh on 2017/3/1.
 */
public class BookMarkAdapter extends KBaseAdapter<BookMarkInfo> {

    private boolean mIsEditMode;

    public BookMarkAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View contentView, ViewGroup parent) {
        ViewHolder holder;
        BookMarkInfo bookMarkInfo = getItem(position);
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
        return contentView;
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
