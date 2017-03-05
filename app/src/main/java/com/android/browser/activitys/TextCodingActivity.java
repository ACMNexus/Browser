package com.android.browser.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.android.browser.R;
import com.android.browser.adapter.TextCodingAdapter;
import com.android.browser.bean.TextCodeInfo;
import com.android.browser.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luooh on 2017/2/21.
 */
public class TextCodingActivity extends BaseActivity {

    private ListView mTextCodeListView;
    private TextCodingAdapter mAdapter;
    private List<TextCodeInfo> mTextCodeInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_textcoding);

        init();
        initView();
    }

    private void init() {
        TextCodeInfo textCodeInfo;
        mTextCodeInfos = new ArrayList();
        String defalutCode = mSettingValues.getDefaultTextEncoding();
        String textCodeValue[] = getResources().getStringArray(R.array.pref_default_text_encoding_choices);
        String textCodeKeys[] = getResources().getStringArray(R.array.pref_default_text_encoding_values);
        for(int i = 0; i < textCodeKeys.length; i++) {
            textCodeInfo = new TextCodeInfo(textCodeKeys[i], textCodeValue[i], false);
            if(defalutCode.equals(textCodeKeys[i])) {
                textCodeInfo.setIsChecked(true);
            }
            mTextCodeInfos.add(textCodeInfo);
        }
    }

    private void initView() {
        mAdapter = new TextCodingAdapter(this);
        mTextCodeListView = (ListView) findViewById(R.id.codeListView);
        mTextCodeListView.setOnItemClickListener(this);
        mTextCodeListView.setAdapter(mAdapter);
        mAdapter.setItems(mTextCodeInfos);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        mSettingValues.setDefalutTextCoding(mAdapter.getItem(position).getTextCodeName());
        Intent intent = new Intent();
        intent.putExtra(Constants.TEXTCODING, mAdapter.getItem(position).getTextCodeName());
        setResult(RESULT_OK, intent);
        finish();
    }
}
