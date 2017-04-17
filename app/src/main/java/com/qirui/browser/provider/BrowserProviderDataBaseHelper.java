package com.qirui.browser.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Process;
import com.qirui.browser.R;
import com.qirui.browser.util.IOUtils;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by lenvo on 2017/4/17.
 */
public class BrowserProviderDataBaseHelper extends SQLiteOpenHelper {

    private Context mContext;
    public static final String sDatabaseName = "browser.db";

    public BrowserProviderDataBaseHelper(Context context) {
        super(context, sDatabaseName, null, BrowserProviderKey.DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE bookmarks (" +
                "_id INTEGER PRIMARY KEY," +
                "title TEXT," +
                "url TEXT NOT NULL," +
                "visits INTEGER," +
                "date LONG," +
                "created LONG," +
                "description TEXT," +
                "bookmark INTEGER," +
                "favicon BLOB DEFAULT NULL," +
                "thumbnail BLOB DEFAULT NULL," +
                "touch_icon BLOB DEFAULT NULL," +
                "user_entered INTEGER" +
                ");");

        final CharSequence[] bookmarks = mContext.getResources()
                .getTextArray(R.array.bookmarks);
        int size = bookmarks.length;
        try {
            for (int i = 0; i < size; i = i + 2) {
                CharSequence bookmarkDestination = replaceSystemPropertyInString(mContext, bookmarks[i + 1]);
                db.execSQL("INSERT INTO bookmarks (title, url, visits, " +
                        "date, created, bookmark)" + " VALUES('" +
                        bookmarks[i] + "', '" + bookmarkDestination +
                        "', 0, 0, 0, 1);");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        db.execSQL("CREATE TABLE searches (" +
                "_id INTEGER PRIMARY KEY," +
                "search TEXT," +
                "date LONG" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 18) {
            db.execSQL("DROP TABLE IF EXISTS labels");
        }
        if (oldVersion <= 19) {
            db.execSQL("ALTER TABLE bookmarks ADD COLUMN thumbnail BLOB DEFAULT NULL;");
        }
        if (oldVersion < 21) {
            db.execSQL("ALTER TABLE bookmarks ADD COLUMN touch_icon BLOB DEFAULT NULL;");
        }
        if (oldVersion < 22) {
            db.execSQL("DELETE FROM bookmarks WHERE (bookmark = 0 AND url LIKE \"%.google.%client=ms-%\")");
            removeGears();
        }
        if (oldVersion < 23) {
            db.execSQL("ALTER TABLE bookmarks ADD COLUMN user_entered INTEGER;");
        }
        if (oldVersion < 24) {
                /* SQLite does not support ALTER COLUMN, hence the lengthy code. */
            db.execSQL("DELETE FROM bookmarks WHERE url IS NULL;");
            db.execSQL("ALTER TABLE bookmarks RENAME TO bookmarks_temp;");
            db.execSQL("CREATE TABLE bookmarks (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "url TEXT NOT NULL," +
                    "visits INTEGER," +
                    "date LONG," +
                    "created LONG," +
                    "description TEXT," +
                    "bookmark INTEGER," +
                    "favicon BLOB DEFAULT NULL," +
                    "thumbnail BLOB DEFAULT NULL," +
                    "touch_icon BLOB DEFAULT NULL," +
                    "user_entered INTEGER" +
                    ");");
            db.execSQL("INSERT INTO bookmarks SELECT * FROM bookmarks_temp;");
            db.execSQL("DROP TABLE bookmarks_temp;");
        } else {
            db.execSQL("DROP TABLE IF EXISTS bookmarks");
            db.execSQL("DROP TABLE IF EXISTS searches");
            onCreate(db);
        }
    }

    private void removeGears() {
        new Thread() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                String browserDataDirString = mContext.getApplicationInfo().dataDir;
                final String appPluginsDirString = "app_plugins";
                final String gearsPrefix = "gears";
                File appPluginsDir = new File(browserDataDirString + File.separator
                        + appPluginsDirString);
                if (!appPluginsDir.exists()) {
                    return;
                }
                // Delete the Gears plugin files
                File[] gearsFiles = appPluginsDir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String filename) {
                        return filename.startsWith(gearsPrefix);
                    }
                });
                for (int i = 0; i < gearsFiles.length; ++i) {
                    if (gearsFiles[i].isDirectory()) {
                        deleteDirectory(gearsFiles[i]);
                    } else {
                        gearsFiles[i].delete();
                    }
                }
                // Delete the Gears data files
                File gearsDataDir = new File(browserDataDirString + File.separator
                        + gearsPrefix);
                if (!gearsDataDir.exists()) {
                    return;
                }
                deleteDirectory(gearsDataDir);
            }

            private void deleteDirectory(File currentDir) {
                File[] files = currentDir.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    files[i].delete();
                }
                currentDir.delete();
            }
        }.start();
    }

    private static CharSequence replaceSystemPropertyInString(Context context, CharSequence srcString) {
        StringBuffer sb = new StringBuffer();
        int lastCharLoc = 0;

        final String client_id = getClientId(context.getContentResolver());

        for (int i = 0; i < srcString.length(); ++i) {
            char c = srcString.charAt(i);
            if (c == '{') {
                sb.append(srcString.subSequence(lastCharLoc, i));
                lastCharLoc = i;
                inner:
                for (int j = i; j < srcString.length(); ++j) {
                    char k = srcString.charAt(j);
                    if (k == '}') {
                        String propertyKeyValue = srcString.subSequence(i + 1, j).toString();
                        if (propertyKeyValue.equals("CLIENT_ID")) {
                            sb.append(client_id);
                        } else {
                            sb.append("unknown");
                        }
                        lastCharLoc = j + 1;
                        i = j;
                        break inner;
                    }
                }
            }
        }
        if (srcString.length() - lastCharLoc > 0) {
            // Put on the tail, if there is one
            sb.append(srcString.subSequence(lastCharLoc, srcString.length()));
        }
        return sb;
    }

    // XXX: This is a major hack to remove our dependency on gsf constants and
    // its content provider. http://b/issue?id=2425179
    public static String getClientId(ContentResolver cr) {
        String ret = "android-google";
        Cursor legacyClientIdCursor = null;
        Cursor searchClientIdCursor = null;

        // search_client_id includes search prefix, legacy client_id does not include prefix
        try {
            searchClientIdCursor = cr.query(Uri.parse("content://com.google.settings/partner"),
                    new String[]{"value"}, "name='search_client_id'", null, null);
            if (searchClientIdCursor != null && searchClientIdCursor.moveToNext()) {
                ret = searchClientIdCursor.getString(0);
            } else {
                legacyClientIdCursor = cr.query(Uri.parse("content://com.google.settings/partner"),
                        new String[]{"value"}, "name='client_id'", null, null);
                if (legacyClientIdCursor != null && legacyClientIdCursor.moveToNext()) {
                    ret = "ms-" + legacyClientIdCursor.getString(0);
                }
            }
        } catch (RuntimeException ex) {

        } finally {
            IOUtils.closeCursor(legacyClientIdCursor);
            IOUtils.closeCursor(searchClientIdCursor);
        }
        return ret;
    }
}
