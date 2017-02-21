package com.android.browser.activitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luooh on 2017/2/15.
 */

public class BaseActivity extends Activity implements View.OnClickListener, AbsListView.OnItemClickListener {

    protected Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivity = this;
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    }

    protected void startNextPager(Class<?> clazz, Map<String, ? extends Serializable> map) {
        Intent intent = new Intent(this, clazz);
        if(map != null && map.size() > 0) {
            Set<String> keySets = map.keySet();
            for(String key : keySets) {
                intent.putExtra(key, map.get(key));
            }
        }
        startActivity(intent);
    }
}
