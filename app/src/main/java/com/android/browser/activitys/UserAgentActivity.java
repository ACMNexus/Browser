package com.android.browser.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.browser.R;
import com.android.browser.adapter.UserAgentAdapter;
import com.android.browser.bean.UserAgentInfo;
import com.android.browser.util.Constants;

/**
 * Created by Luooh on 2017/2/21.
 */
public class UserAgentActivity extends BaseActivity {

    private UserAgentAdapter mAdapter;
    private ListView mUserAgentListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_user_agent);

        init();
        initView();
    }

    private void init() {

        UserAgentInfo userAgentInfo;
        mAdapter = new UserAgentAdapter(this);
        int userAgent = mSettingValues.getUserAgent();
        String userAgents[] = getResources().getStringArray(R.array.pref_user_agent_choices);
        for (int i = 0; i < userAgents.length; i++) {
            userAgentInfo = new UserAgentInfo(userAgents[i], Constants.USER_AGENTS[i], false);
            if (i == userAgent) {
                userAgentInfo.setIsChecked(true);
            }
            mAdapter.addItem(userAgentInfo, false);
        }
    }

    private void initView() {
        mUserAgentListView = (ListView) findViewById(R.id.userAgentView);
        mUserAgentListView.setOnItemClickListener(this);
        mUserAgentListView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        mSettingValues.setUserAgent(position);
        Intent intent = new Intent();
        intent.putExtra(Constants.USERAGENT, mAdapter.getItem(position).getUserAgentName());
        setResult(RESULT_OK, intent);
        finish();
    }
}
