package com.qirui.browser.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luooh on 2017/2/23.
 */
public abstract class KBaseAdapter<T> extends BaseAdapter {

    protected List<T> mList;
    protected Context mContext;

    public KBaseAdapter(Context context) {
        this.mContext = context;
        this.mList = new ArrayList<>();
    }

    public void addItems(List<T> datas) {
        if(datas != null && datas.size() > 0) {
            mList.addAll(datas);
            notifyDataSetChanged();
        }
    }

    public void addItem(T data, boolean refresh) {
        if(data != null) {
            mList.add(data);
            if(refresh) {
                notifyDataSetChanged();
            }
        }
    }

    public void setItems(List<T> datas) {
        if(datas != null && datas.size() > 0) {
            mList.clear();
            mList.addAll(datas);
            notifyDataSetChanged();
        }
    }

    public List<T> getList() {
        return mList;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    abstract public View getView(int position, View contentView, ViewGroup parent);
}
