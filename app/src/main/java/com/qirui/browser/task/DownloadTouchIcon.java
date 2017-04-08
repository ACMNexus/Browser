/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qirui.browser.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

import com.qirui.browser.Bookmarks;
import com.qirui.browser.Browser;
import com.qirui.browser.Tab;
import com.qirui.browser.provider.BrowserContract;
import com.qirui.browser.provider.BrowserContract.Images;
import com.qirui.browser.util.IOUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTouchIcon extends AsyncTask<String, Void, Void> {

    private final ContentResolver mContentResolver;
    private Cursor mCursor;
    private final String mOriginalUrl;
    private final String mUrl;
    private final String mUserAgent; // Sites may serve a different icon to different UAs
    private Message mMessage;

    private Context mContext;
    public Tab mTab;

    /**
     * Use this ctor to store the touch icon in the bookmarks database for
     * the originalUrl so we take account of redirects. Used when the user
     * bookmarks a page from outside the bookmarks activity.
     */
    public DownloadTouchIcon(Tab tab, Context ctx, ContentResolver cr, WebView view) {
        mTab = tab;
        mContext = ctx.getApplicationContext();
        mContentResolver = cr;
        // Store these in case they change.
        mOriginalUrl = view.getOriginalUrl();
        mUrl = view.getUrl();
        mUserAgent = view.getSettings().getUserAgentString();
    }

    /**
     * Use this ctor to download the touch icon and update the bookmarks database
     * entry for the given url. Used when the user creates a bookmark from
     * within the bookmarks activity and there haven't been any redirects.
     * TODO: Would be nice to set the user agent here so that there is no
     * potential for the three different ctors here to return different icons.
     */
    public DownloadTouchIcon(Context ctx, ContentResolver cr, String url) {
        mTab = null;
        mContext = ctx.getApplicationContext();
        mContentResolver = cr;
        mOriginalUrl = null;
        mUrl = url;
        mUserAgent = null;
    }

    /**
     * Use this ctor to not store the touch icon in a database, rather add it to
     * the passed Message's data bundle with the key
     * {@link BrowserContract.Bookmarks#TOUCH_ICON} and then send the message.
     */
    public DownloadTouchIcon(Context context, Message msg, String userAgent) {
        mMessage = msg;
        mContext = context.getApplicationContext();
        mContentResolver = null;
        mOriginalUrl = null;
        mUrl = null;
        mUserAgent = userAgent;
    }

    @Override
    public Void doInBackground(String... values) {
        if (mContentResolver != null) {
            mCursor = Bookmarks.queryCombinedForUrl(mContentResolver, mOriginalUrl, mUrl);
        }

        boolean inDatabase = mCursor != null && mCursor.getCount() > 0;

        if (inDatabase || mMessage != null) {
            if(Browser.LOGD_ENABLED){
                Log.i("LOH", "download touch icon " + values[0]) ;
            }

            HttpURLConnection connection = null;
            try {
                URL url = new URL(values[0]);
                connection = (HttpURLConnection) url.openConnection();
                if (mUserAgent != null) {
                    connection.addRequestProperty("User-Agent", mUserAgent);
                }

                if (connection.getResponseCode() == 200) {
                    InputStream content = connection.getInputStream();
                    Bitmap icon = null;
                    try {
                        icon = BitmapFactory.decodeStream(content, null, null);
                    } finally {
                        IOUtils.closeStream(content);
                    }

                    if (inDatabase) {
                        storeIcon(icon);
                    } else if (mMessage != null) {
                        Bundle b = mMessage.getData();
                        b.putParcelable(BrowserContract.Bookmarks.TOUCH_ICON, icon);
                    }
                }
            } catch (IOException ignored) {
                Log.i("LOH", ignored.toString());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        IOUtils.closeCursor(mCursor);
        if (mMessage != null) {
            mMessage.sendToTarget();
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        IOUtils.closeCursor(mCursor);
    }

    private void storeIcon(Bitmap icon) {
        // Do this first in case the download failed.
        if (mTab != null) {
            // Remove the touch icon loader from the BrowserActivity.
            mTab.mTouchIconLoader = null;
        }

        if (icon == null || mCursor == null || isCancelled()) {
            return;
        }

        if (mCursor.moveToFirst()) {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            icon.compress(Bitmap.CompressFormat.PNG, 100, os);

            ContentValues values = new ContentValues();
            values.put(Images.TOUCH_ICON, os.toByteArray());

            do {
                values.put(Images.URL, mCursor.getString(0));
                mContentResolver.update(Images.CONTENT_URI, values, null, null);
            } while (mCursor.moveToNext());
        }
    }
}