package com.qirui.browser.activitys;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ParseException;
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
import com.qirui.browser.http.WebAddress;
import com.qirui.browser.util.BookmarkUtils;
import com.qirui.browser.Bookmarks;
import com.qirui.browser.task.DownloadTouchIcon;
import com.qirui.browser.R;
import com.qirui.browser.util.UrlUtils;
import com.qirui.browser.provider.BrowserContract;
import com.qirui.browser.util.ToastUtils;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Luooh on 2017/2/28.
 */
public class AddBookMarkActivity extends BaseActivity implements Handler.Callback {

    private TextView mTitle;
    private EditText mAddress;

    private Handler mHandler;
    private Bundle mParamMap;

    private String mTouchIconUrl;
    private boolean mEditingExisting;

    public static final String TOUCH_ICON_URL = "touch_icon_url";
    // Place on an edited bookmark to remove the saved thumbnail
    public static final String USER_AGENT = "user_agent";
    public static final String CHECK_FOR_DUPE = "check_for_dupe";

    private long mCurrentFolder = 0;
    private static final int SAVE_BOOKMARK = 100;
    private static final int TOUCH_ICON_DOWNLOADED = 101;
    public static final int EDIT_BOOKMARK_REQUEST_CODE = 1001;

    private String mOriginUrl;
    private String mOriginTitle;

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
            String edit_bookmark = mParamMap.getString(BrowserContract.Bookmarks.EXTRA_EDIT_BOOKMARK);
            if(!TextUtils.isEmpty(edit_bookmark)) {
                mEditingExisting = true;
            }
            if(!mEditingExisting) {
                mTouchIconUrl = mParamMap.getString(TOUCH_ICON_URL);
            }
            mOriginTitle = mParamMap.getString(BrowserContract.Bookmarks.TITLE);
            mOriginUrl = mParamMap.getString(BrowserContract.Bookmarks.URL);
            mTitle.setText(mOriginTitle);
            mAddress.setText(mOriginUrl);
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
            ToastUtils.show(this, R.string.bookmark_needs_title);
            return false;
        }
        if (TextUtils.isEmpty(url)) {
            ToastUtils.show(this, R.string.bookmark_needs_url);
            return false;
        }

        if(!url.toLowerCase().startsWith("javascript:")) {
            try {
                URI uriObj = new URI(url);
                String scheme = uriObj.getScheme();
                if(!Bookmarks.urlHasAcceptableScheme(url)) {
                    if(scheme != null) {
                        ToastUtils.show(mActivity, R.string.bookmark_cannot_save_url);
                        return false;
                    }
                    WebAddress address = null;
                    try {
                        address = new WebAddress(url);
                    } catch (ParseException e) {
                        throw new URISyntaxException("", "");
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (address.getHost().length() == 0) {
                        throw new URISyntaxException("", "");
                    }
                    url = address.toString();
                }
            } catch (URISyntaxException e) {
                ToastUtils.show(this, R.string.bookmark_url_not_valid);
                return false;
            }
        }

        if(mEditingExisting) {
            boolean urlUnmodified = url.equals(mOriginUrl);
            urlUnmodified = urlUnmodified && title.equals(mOriginTitle);
            if(urlUnmodified) {
                ToastUtils.show(this, "the content not modify");
                return false;
            }

            Long id = mParamMap.getLong(BrowserContract.Bookmarks._ID);
            ContentValues values = new ContentValues();
            values.put(BrowserContract.Bookmarks.TITLE, title);
            values.put(BrowserContract.Bookmarks.URL, url);
            if(values.size() > 0) {
                new UpdateBookmarkTask(getApplicationContext(), id).execute(values);
            }
            setResult(RESULT_OK);
        }else {
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
        }

        finish();

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
