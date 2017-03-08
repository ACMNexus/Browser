package com.android.browser.activitys;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.android.browser.R;
import com.android.browser.adapter.SearchEngineAdapter;
import com.android.browser.search.SearchEngine;
import com.android.browser.search.SearchEngineInfo;
import com.android.browser.search.SearchEngines;
import com.android.browser.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luooh on 2017/2/22.
 */
public class SearchEngineActivity extends BaseActivity {

    private ListView mListView;
    private SearchEngineAdapter mAdapter;
    private List<SearchEngineInfo> mSearchEngineInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_search_engine);

        init();
        initView();
    }

    private void init() {
        mSearchEngineInfos = new ArrayList<>();
        String defaultSearchEngineName = mSettingValues.getSearchEngineName();
        if (TextUtils.isEmpty(defaultSearchEngineName)) {
            SearchEngine defaultSearchEngine = SearchEngines.getDefaultSearchEngine(this);
            defaultSearchEngineName = defaultSearchEngine == null ? "" : defaultSearchEngine.getName();
        }

        List<SearchEngineInfo> list = SearchEngines.getSearchEngineInfos(this);
        TypedArray typedArray = getResources().obtainTypedArray(R.array.search_engine_icons);
        for (int i = 0; i < list.size(); i++) {
            SearchEngineInfo searchEngineInfo = list.get(i);
            String name = searchEngineInfo.getName();
            searchEngineInfo.setSeachEngineIcon(typedArray.getResourceId(i, 0));
            if (name.equals(defaultSearchEngineName)) {
                searchEngineInfo.setChecked(true);
            }
            mSearchEngineInfos.add(searchEngineInfo);
        }

        if(TextUtils.isEmpty(defaultSearchEngineName)) {
            mSearchEngineInfos.get(0).setChecked(true);
        }

        typedArray.recycle();
    }

    private void initView() {
        mAdapter = new SearchEngineAdapter(this);
        mListView = (ListView) findViewById(R.id.searchListView);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);
        mAdapter.setItems(mSearchEngineInfos);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        SearchEngineInfo searchEngineInfo = mAdapter.getItem(position);
        mSettingValues.setSearchEngineName(searchEngineInfo.getName());
        mSettingValues.setSearchIconResId(searchEngineInfo.getSeachEngineIcon());
        Intent intent = new Intent();
        intent.putExtra(Constants.SEARCHICON, searchEngineInfo.getSeachEngineIcon());
        setResult(RESULT_OK, intent);
        finish();
    }
}
