package com.qirui.browser.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.qirui.browser.R;
import com.qirui.browser.bean.WebSiteInfo;

/**
 * Created by Luooh on 2017/3/28.
 */
public class HomePagerAdapter extends KBaseAdapter<WebSiteInfo> {

    public HomePagerAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View contentView, ViewGroup parent) {
        ViewHolder holder;
        WebSiteInfo info = getItem(position);
        if(contentView == null) {
            holder = new ViewHolder();
            contentView = View.inflate(mContext, R.layout.layout_homepage_item, null);
            holder.webIcon = (ImageView) contentView.findViewById(R.id.web_icon);
            holder.webTitle = (TextView) contentView.findViewById(R.id.web_title);
            contentView.setTag(holder);
        }else {
            holder = (ViewHolder) contentView.getTag();
        }
        holder.webTitle.setText(info.getName());
        holder.webIcon.setImageResource(info.getIconResId());
        return contentView;
    }

    class ViewHolder {
        ImageView webIcon;
        TextView webTitle;
    }
}
