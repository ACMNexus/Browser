package com.qirui.browser.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.webkit.WebView;

import com.qirui.browser.Bookmarks;
import com.qirui.browser.R;
import com.qirui.browser.Tab;
import com.qirui.browser.provider.BrowserContract;

import java.io.ByteArrayOutputStream;

/**
 * Created by lenvo on 2017/3/24.
 */
public class Utils {

    /**
     * Share a page, providing the title, url, favicon, and a screenshot.  Uses
     * an {@link Intent} to launch the Activity chooser.
     * @param c Context used to launch a new Activity.
     * @param title Title of the page.  Stored in the Intent with
     *          {@link Intent#EXTRA_SUBJECT}
     * @param url URL of the page.  Stored in the Intent with
     *          {@link Intent#EXTRA_TEXT}
     * @param favicon Bitmap of the favicon for the page.  Stored in the Intent
     *          with {@link Constants#EXTRA_SHARE_FAVICON}
     * @param screenshot Bitmap of a screenshot of the page.  Stored in the
     *          Intent with {@link Constants#EXTRA_SHARE_SCREENSHOT}
     */
    public static final void sharePage(Context c, String title, String url, Bitmap favicon, Bitmap screenshot) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, url);
        send.putExtra(Intent.EXTRA_SUBJECT, title);
        send.putExtra(Constants.EXTRA_SHARE_FAVICON, favicon);
        send.putExtra(Constants.EXTRA_SHARE_SCREENSHOT, screenshot);
        try {
            c.startActivity(Intent.createChooser(send, c.getString(R.string.choosertitle_sharevia)));
        } catch(android.content.ActivityNotFoundException ex) {
            // if no app handles it, do nothing
        }
    }

    /**
     * Return the desired width for thumbnail screenshots, which are stored in
     * the database, and used on the bookmarks screen.
     * @param context Context for finding out the density of the screen.
     * @return desired width for thumbnail screenshot.
     */
    public static int getDesiredThumbnailWidth(Context context) {
        return context.getResources().getDimensionPixelOffset(R.dimen.bookmarkThumbnailWidth);
    }

    /**
     * Return the desired height for thumbnail screenshots, which are stored in
     * the database, and used on the bookmarks screen.
     * @param context Context for finding out the density of the screen.
     * @return desired height for thumbnail screenshot.
     */
    public static int getDesiredThumbnailHeight(Context context) {
        return context.getResources().getDimensionPixelOffset(R.dimen.bookmarkThumbnailHeight);
    }
}
