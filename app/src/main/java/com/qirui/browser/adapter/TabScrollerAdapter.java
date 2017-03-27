package com.qirui.browser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.luooh.stackoverview.model.OverviewAdapter;
import com.luooh.stackoverview.model.ViewHolder;
import com.qirui.browser.R;
import com.qirui.browser.Tab;

/**
 * Created by Luooh on 2017/3/27.
 */
public class TabScrollerAdapter extends OverviewAdapter<ViewHolder, Tab> {

    private LayoutInflater mInflater;

    public TabScrollerAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(Context context, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.nav_tab_view, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder) {
        Tab tab = (Tab) viewHolder.model;
        TextView title = (TextView) viewHolder.itemView.findViewById(R.id.title);
        ImageView tabView = (ImageView) viewHolder.itemView.findViewById(R.id.tab_view);
        title.setText(tab.getTitle());
        if(tab.getScreenshot() != null) {
            tabView.setImageBitmap(tab.getScreenshot());
        }
        viewHolder.itemView.findViewById(R.id.closetab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }
}
