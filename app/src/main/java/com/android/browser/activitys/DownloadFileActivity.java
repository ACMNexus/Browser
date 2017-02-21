package com.android.browser.activitys;

import android.os.Bundle;
import android.view.View;

import com.android.browser.R;

/**
 * Created by Luooh on 2017/2/15.
 */

public class DownloadFileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_file);

        setListener();
    }

    private void setListener() {
        findViewById(R.id.filebrowser).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.filebrowser:
                startNextPager(FileBrowserActivity.class, null);
                break;
        }
    }
}
