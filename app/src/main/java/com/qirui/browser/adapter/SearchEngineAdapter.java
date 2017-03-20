package com.qirui.browser.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.qirui.browser.R;
import com.qirui.browser.search.SearchEngineInfo;

/**
 * Created by Luooh on 2017/2/23.
 */
public class SearchEngineAdapter extends KBaseAdapter<SearchEngineInfo> {

    public SearchEngineAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View contentView, ViewGroup parent) {

        ViewHolder holder;
        SearchEngineInfo searchInfo = getItem(position);
        if(contentView == null) {
            holder = new ViewHolder();
            contentView = View.inflate(mContext, R.layout.layout_searchengine_item, null);
            holder.iconsView = (ImageView) contentView.findViewById(R.id.icon);
            holder.titleView = (TextView) contentView.findViewById(R.id.title);
            holder.checkedView = (ImageView) contentView.findViewById(R.id.checked);
            contentView.setTag(holder);
        }else {
            holder = (ViewHolder) contentView.getTag();
        }
        holder.titleView.setText(searchInfo.getLabel());
        holder.iconsView.setImageResource(searchInfo.getSeachEngineIcon());
        if(searchInfo.IsChecked()) {
            holder.checkedView.setImageResource(R.drawable.ic_browser_complete);
        }else {
            holder.checkedView.setImageResource(0);
        }
        return contentView;
    }

    class ViewHolder {
        TextView titleView;
        ImageView checkedView;
        ImageView iconsView;
    }
}
