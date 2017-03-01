package com.android.browser.activitys;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.android.browser.util.BookmarkUtils;
import com.android.browser.Bookmarks;
import com.android.browser.DownloadTouchIcon;
import com.android.browser.R;
import com.android.browser.UrlUtils;
import com.android.browser.provider.BrowserContract;
import com.android.browser.util.ToastUtils;

/**
 * Created by Luooh on 2017/2/28.
 */
public class AddBookMarkActivity extends BaseActivity implements Handler.Callback {

    private TextView mTitle;
    private EditText mAddress;

    private Handler mHandler;
    private Bundle mParamMap;

    private String mTouchIconUrl;

    public static final String TOUCH_ICON_URL = "touch_icon_url";
    // Place on an edited bookmark to remove the saved thumbnail
    public static final String USER_AGENT = "user_agent";
    public static final String CHECK_FOR_DUPE = "check_for_dupe";

    private long mCurrentFolder = 0;
    private static final int SAVE_BOOKMARK = 100;
    private static final int TOUCH_ICON_DOWNLOADED = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_addbookmark);

        initView();
        init();
    }

    private void init() {
        mParamMap = getIntent().getExtras();
        if (mParamMap != null) {
            mTouchIconUrl = mParamMap.getString(TOUCH_ICON_URL);
            mTitle.setText(mParamMap.getString(BrowserContract.Bookmarks.TITLE));
            mAddress.setText(mParamMap.getString(BrowserContract.Bookmarks.URL));
        }

        mHandler = new Handler(this);
    }

    private void initView() {
        mTitle = (TextView) findViewById(R.id.title);
        mAddress = (EditText) findViewById(R.id.address);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.confirm).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.confirm:
                save();
                break;
        }
    }

    private boolean save() {
        String title = mTitle.getText().toString().trim();
        String url = UrlUtils.fixUrl(mAddress.getText().toString().trim()).trim();
        if (TextUtils.isEmpty(title)) {
            return false;
        }
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        Bitmap favicon = mParamMap.getParcelable(BrowserContract.Bookmarks.FAVICON);
        Bundle bundle = new Bundle();
        bundle.putString(BrowserContract.Bookmarks.TITLE, title);
        bundle.putString(BrowserContract.Bookmarks.URL, url);
        bundle.putParcelable(BrowserContract.Bookmarks.FAVICON, favicon);
        bundle.putString(TOUCH_ICON_URL, mTouchIconUrl);
        Message msg = Message.obtain(mHandler, SAVE_BOOKMARK);
        msg.setData(bundle);
        // Start a new thread so as to not slow down the UI
        Thread t = new Thread(new SaveBookMarkRunnable(getApplicationContext(), msg));
        t.start();
        setResult(RESULT_OK);
        return true;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case SAVE_BOOKMARK:
                if (1 == msg.arg1) {
                    ToastUtils.show(mActivity, R.string.bookmark_saved);
                } else {
                    ToastUtils.show(mActivity, R.string.bookmark_not_saved);
                }
                break;
            case TOUCH_ICON_DOWNLOADED:
                Bundle bundle = msg.getData();
                sendBroadcast(BookmarkUtils.createAddToHomeIntent(mActivity,
                        bundle.getString(BrowserContract.Bookmarks.URL),
                        bundle.getString(BrowserContract.Bookmarks.TITLE),
                        (Bitmap) bundle.getParcelable(BrowserContract.Bookmarks.TOUCH_ICON),
                        (Bitmap) bundle.getParcelable(BrowserContract.Bookmarks.FAVICON)));
                break;
        }
        return false;
    }

    public class SaveBookMarkRunnable implements Runnable {

        private Message mMessage;
        private Context mContext;

        public SaveBookMarkRunnable(Context context, Message message) {
            this.mContext = context.getApplicationContext();
            this.mMessage = message;
        }

        @Override
        public void run() {
            Bundle bundle = mMessage.getData();
            String title = bundle.getString(BrowserContract.Bookmarks.TITLE);
            String url = bundle.getString(BrowserContract.Bookmarks.URL);
            Bitmap favicon = bundle.getParcelable(BrowserContract.Bookmarks.FAVICON);
            String touchIconUrl = bundle.getString(TOUCH_ICON_URL);

            // Save to the bookmarks DB.
            try {
                final ContentResolver cr = getContentResolver();
                Bookmarks.addBookmark(mContext, false, url, title, favicon, mCurrentFolder);
                if (touchIconUrl != null) {
                    new DownloadTouchIcon(mContext, cr, url).execute(mTouchIconUrl);
                }
                mMessage.arg1 = 1;
            } catch (IllegalStateException e) {
                mMessage.arg1 = 0;
            }
            mMessage.sendToTarget();
        }
    }

    public class UpdateBookmarkTask extends AsyncTask<ContentValues, Void, Void> {

        private Context mContext;
        private Long mId;

        public UpdateBookmarkTask(Context context, long id) {
            mContext = context.getApplicationContext();
            mId = id;
        }

        @Override
        protected Void doInBackground(ContentValues... params) {
            if (params.length != 1) {
                throw new IllegalArgumentException("No ContentValues provided!");
            }
            Uri uri = ContentUris.withAppendedId(BookmarkUtils.getBookmarksUri(mContext), mId);
            mContext.getContentResolver().update(uri, params[0], null, null);
            return null;
        }
    }

    @Override
    protected void setWindowFeature() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
}
