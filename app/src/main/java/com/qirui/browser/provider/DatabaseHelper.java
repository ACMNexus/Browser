package com.qirui.browser.provider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.provider.Browser;
import android.text.TextUtils;
import com.qirui.browser.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Luooh on 2017/4/17.
 */
public class DatabaseHelper extends SQLiteOpenHelper implements BrowserProvider2Key {

    private Context mContext;
    static final int DATABASE_VERSION = 32;
    static final String DATABASE_NAME = "browser2.db";
    private SyncStateContentProviderHelper mSyncHelper;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        setWriteAheadLoggingEnabled(true);
    }

    public void setSyncStateHelper(SyncStateContentProviderHelper helper) {
        this.mSyncHelper = helper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_BOOKMARKS + "(" +
                BrowserContract.Bookmarks._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                BrowserContract.Bookmarks.TITLE + " TEXT," +
                BrowserContract.Bookmarks.URL + " TEXT," +
                BrowserContract.Bookmarks.IS_FOLDER + " INTEGER NOT NULL DEFAULT 0," +
                BrowserContract.Bookmarks.PARENT + " INTEGER," +
                BrowserContract.Bookmarks.POSITION + " INTEGER NOT NULL," +
                BrowserContract.Bookmarks.INSERT_AFTER + " INTEGER," +
                BrowserContract.Bookmarks.IS_DELETED + " INTEGER NOT NULL DEFAULT 0," +
                BrowserContract.Bookmarks.ACCOUNT_NAME + " TEXT," +
                BrowserContract.Bookmarks.ACCOUNT_TYPE + " TEXT," +
                BrowserContract.Bookmarks.SOURCE_ID + " TEXT," +
                BrowserContract.Bookmarks.VERSION + " INTEGER NOT NULL DEFAULT 1," +
                BrowserContract.Bookmarks.DATE_CREATED + " INTEGER," +
                BrowserContract.Bookmarks.DATE_MODIFIED + " INTEGER," +
                BrowserContract.Bookmarks.DIRTY + " INTEGER NOT NULL DEFAULT 0," +
                BrowserContract.Bookmarks.SYNC1 + " TEXT," +
                BrowserContract.Bookmarks.SYNC2 + " TEXT," +
                BrowserContract.Bookmarks.SYNC3 + " TEXT," +
                BrowserContract.Bookmarks.SYNC4 + " TEXT," +
                BrowserContract.Bookmarks.SYNC5 + " TEXT" +
                ");");

        // TODO indices
        db.execSQL("CREATE TABLE " + TABLE_HISTORY + "(" +
                BrowserContract.History._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                BrowserContract.History.TITLE + " TEXT," +
                BrowserContract.History.URL + " TEXT NOT NULL," +
                BrowserContract.History.DATE_CREATED + " INTEGER," +
                BrowserContract.History.DATE_LAST_VISITED + " INTEGER," +
                BrowserContract.History.VISITS + " INTEGER NOT NULL DEFAULT 0," +
                BrowserContract.History.USER_ENTERED + " INTEGER" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_IMAGES + " (" +
                BrowserContract.Images.URL + " TEXT UNIQUE NOT NULL," +
                BrowserContract.Images.FAVICON + " BLOB," +
                BrowserContract.Images.THUMBNAIL + " BLOB," +
                BrowserContract.Images.TOUCH_ICON + " BLOB" +
                ");");
        db.execSQL("CREATE INDEX imagesUrlIndex ON " + TABLE_IMAGES +
                "(" + BrowserContract.Images.URL + ")");

        db.execSQL("CREATE TABLE " + TABLE_SEARCHES + " (" +
                BrowserContract.Searches._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                BrowserContract.Searches.SEARCH + " TEXT," +
                BrowserContract.Searches.DATE + " LONG" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_SETTINGS + " (" +
                BrowserContract.Settings.KEY + " TEXT PRIMARY KEY," +
                BrowserContract.Settings.VALUE + " TEXT NOT NULL" +
                ");");

        createAccountsView(db);
        createThumbnails(db);

        mSyncHelper.createDatabase(db);

        if (!importFromBrowserProvider(db)) {
            createDefaultBookmarks(db);
        }

        enableSync(db);
        createOmniboxSuggestions(db);
    }

    void createOmniboxSuggestions(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_VIEW_OMNIBOX_SUGGESTIONS);
    }

    public void createThumbnails(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_THUMBNAILS + " (" +
                BrowserProvider2Key.Thumbnails._ID + " INTEGER PRIMARY KEY," +
                BrowserProvider2Key.Thumbnails.THUMBNAIL + " BLOB NOT NULL" +
                ");");
    }

    public void enableSync(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(BrowserContract.Settings.KEY, BrowserContract.Settings.KEY_SYNC_ENABLED);
        values.put(BrowserContract.Settings.VALUE, 1);
        insertSettingsInTransaction(db, values);
        // Enable bookmark sync on all accounts
        AccountManager am = (AccountManager) mContext.getSystemService(
                Context.ACCOUNT_SERVICE);
        if (am == null) {
            return;
        }
        Account[] accounts = am.getAccountsByType("com.google");
        if (accounts == null || accounts.length == 0) {
            return;
        }
        for (Account account : accounts) {
            if (ContentResolver.getIsSyncable(
                    account, BrowserContract.AUTHORITY) == 0) {
                // Account wasn't syncable, enable it
                ContentResolver.setIsSyncable(
                        account, BrowserContract.AUTHORITY, 1);
                ContentResolver.setSyncAutomatically(
                        account, BrowserContract.AUTHORITY, true);
            }
        }
    }

    public boolean importFromBrowserProvider(SQLiteDatabase db) {
        Context context = mContext;
        File oldDbFile = context.getDatabasePath(BrowserProviderDataBaseHelper.sDatabaseName);
        if (oldDbFile.exists()) {
            BrowserProviderDataBaseHelper helper = new BrowserProviderDataBaseHelper(context);
            SQLiteDatabase oldDb = helper.getWritableDatabase();
            Cursor c = null;
            try {
                String table = BrowserProvider.TABLE_NAMES[BrowserProvider.URI_MATCH_BOOKMARKS];
                // Import bookmarks
                c = oldDb.query(table,
                        new String[]{
                                Browser.BookmarkColumns.URL, // 0
                                Browser.BookmarkColumns.TITLE, // 1
                                Browser.BookmarkColumns.FAVICON, // 2
                                "touch_icon", // 3
                                Browser.BookmarkColumns.CREATED, // 4
                        }, Browser.BookmarkColumns.BOOKMARK + "!=0", null,
                        null, null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        String url = c.getString(0);
                        if (TextUtils.isEmpty(url))
                            continue; // We require a valid URL
                        ContentValues values = new ContentValues();
                        values.put(BrowserContract.Bookmarks.URL, url);
                        values.put(BrowserContract.Bookmarks.TITLE, c.getString(1));
                        values.put(BrowserContract.Bookmarks.DATE_CREATED, c.getInt(4));
                        values.put(BrowserContract.Bookmarks.POSITION, 0);
                        values.put(BrowserContract.Bookmarks.PARENT, FIXED_ID_ROOT);
                        ContentValues imageValues = new ContentValues();
                        imageValues.put(BrowserContract.Images.URL, url);
                        imageValues.put(BrowserContract.Images.FAVICON, c.getBlob(2));
                        imageValues.put(BrowserContract.Images.TOUCH_ICON, c.getBlob(3));
                        db.insert(TABLE_IMAGES, BrowserContract.Images.THUMBNAIL, imageValues);
                        db.insert(TABLE_BOOKMARKS, BrowserContract.Bookmarks.DIRTY, values);
                    }
                    c.close();
                }
                // Import history
                c = oldDb.query(table,
                        new String[]{
                                Browser.BookmarkColumns.URL, // 0
                                Browser.BookmarkColumns.TITLE, // 1
                                Browser.BookmarkColumns.VISITS, // 2
                                Browser.BookmarkColumns.DATE, // 3
                                Browser.BookmarkColumns.CREATED, // 4
                        }, Browser.BookmarkColumns.VISITS + " > 0 OR "
                                + Browser.BookmarkColumns.BOOKMARK + " = 0",
                        null, null, null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        ContentValues values = new ContentValues();
                        String url = c.getString(0);
                        if (TextUtils.isEmpty(url))
                            continue; // We require a valid URL
                        values.put(BrowserContract.History.URL, url);
                        values.put(BrowserContract.History.TITLE, c.getString(1));
                        values.put(BrowserContract.History.VISITS, c.getInt(2));
                        values.put(BrowserContract.History.DATE_LAST_VISITED, c.getLong(3));
                        values.put(BrowserContract.History.DATE_CREATED, c.getLong(4));
                        db.insert(TABLE_HISTORY, BrowserContract.History.FAVICON, values);
                    }
                    c.close();
                }
                // Wipe the old DB, in case the delete fails.
                oldDb.delete(table, null, null);
            } finally {
                if (c != null) c.close();
                oldDb.close();
                helper.close();
            }
            if (!oldDbFile.delete()) {
                oldDbFile.deleteOnExit();
            }
            return true;
        }
        return false;
    }

    void createAccountsView(SQLiteDatabase db) {
        db.execSQL("CREATE VIEW IF NOT EXISTS v_accounts AS "
                + "SELECT NULL AS " + BrowserContract.Accounts.ACCOUNT_NAME
                + ", NULL AS " + BrowserContract.Accounts.ACCOUNT_TYPE
                + ", " + FIXED_ID_ROOT + " AS " + BrowserContract.Accounts.ROOT_ID
                + " UNION ALL SELECT " + BrowserContract.Accounts.ACCOUNT_NAME
                + ", " + BrowserContract.Accounts.ACCOUNT_TYPE + ", "
                + BrowserContract.Bookmarks._ID + " AS " + BrowserContract.Accounts.ROOT_ID
                + " FROM " + TABLE_BOOKMARKS + " WHERE "
                + BrowserContract.ChromeSyncColumns.SERVER_UNIQUE + " = \""
                + BrowserContract.ChromeSyncColumns.FOLDER_NAME_BOOKMARKS_BAR + "\" AND "
                + BrowserContract.Bookmarks.IS_DELETED + " = 0");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 32) {
            createOmniboxSuggestions(db);
        }
        if (oldVersion < 31) {
            createThumbnails(db);
        }
        if (oldVersion < 30) {
            db.execSQL("DROP VIEW IF EXISTS " + VIEW_SNAPSHOTS_COMBINED);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SNAPSHOTS);
        }
        if (oldVersion < 28) {
            enableSync(db);
        }
        if (oldVersion < 27) {
            createAccountsView(db);
        }
        if (oldVersion < 26) {
            db.execSQL("DROP VIEW IF EXISTS combined");
        }
        if (oldVersion < 25) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCHES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
            mSyncHelper.onAccountsChanged(db, new Account[]{}); // remove all sync info
            onCreate(db);
        }
    }

    public void onOpen(SQLiteDatabase db) {
        mSyncHelper.onDatabaseOpened(db);
    }

    private void createDefaultBookmarks(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        // TODO figure out how to deal with localization for the defaults

        // Bookmarks folder
        values.put(BrowserContract.Bookmarks._ID, FIXED_ID_ROOT);
        values.put(BrowserContract.ChromeSyncColumns.SERVER_UNIQUE, BrowserContract.ChromeSyncColumns.FOLDER_NAME_BOOKMARKS);
        values.put(BrowserContract.Bookmarks.TITLE, "Bookmarks");
        values.putNull(BrowserContract.Bookmarks.PARENT);
        values.put(BrowserContract.Bookmarks.POSITION, 0);
        values.put(BrowserContract.Bookmarks.IS_FOLDER, true);
        values.put(BrowserContract.Bookmarks.DIRTY, true);
        db.insertOrThrow(TABLE_BOOKMARKS, null, values);

        addDefaultBookmarks(db, FIXED_ID_ROOT);
    }

    private void addDefaultBookmarks(SQLiteDatabase db, long parentId) {
        Resources res = mContext.getResources();
        final CharSequence[] bookmarks = res.getTextArray(
                R.array.bookmarks);
        int size = bookmarks.length;
        TypedArray preloads = null/*res.obtainTypedArray(R.array.bookmark_preloads)*/;
        if (preloads == null) return;
        try {
            String parent = Long.toString(parentId);
            String now = Long.toString(System.currentTimeMillis());
            for (int i = 0; i < size; i = i + 2) {
                CharSequence bookmarkDestination = replaceSystemPropertyInString(mContext, bookmarks[i + 1]);
                db.execSQL("INSERT INTO bookmarks (" +
                        BrowserContract.Bookmarks.TITLE + ", " +
                        BrowserContract.Bookmarks.URL + ", " +
                        BrowserContract.Bookmarks.IS_FOLDER + "," +
                        BrowserContract.Bookmarks.PARENT + "," +
                        BrowserContract.Bookmarks.POSITION + "," +
                        BrowserContract.Bookmarks.DATE_CREATED +
                        ") VALUES (" +
                        "'" + bookmarks[i] + "', " +
                        "'" + bookmarkDestination + "', " +
                        "0," +
                        parent + "," +
                        Integer.toString(i) + "," +
                        now +
                        ");");

                int faviconId = preloads.getResourceId(i, 0);
                int thumbId = preloads.getResourceId(i + 1, 0);
                byte[] thumb = null, favicon = null;
                try {
                    thumb = readRaw(res, thumbId);
                } catch (IOException e) {
                }
                try {
                    favicon = readRaw(res, faviconId);
                } catch (IOException e) {
                }
                if (thumb != null || favicon != null) {
                    ContentValues imageValues = new ContentValues();
                    imageValues.put(BrowserContract.Images.URL, bookmarkDestination.toString());
                    if (favicon != null) {
                        imageValues.put(BrowserContract.Images.FAVICON, favicon);
                    }
                    if (thumb != null) {
                        imageValues.put(BrowserContract.Images.THUMBNAIL, thumb);
                    }
                    db.insert(TABLE_IMAGES, BrowserContract.Images.FAVICON, imageValues);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        } finally {
            preloads.recycle();
        }
    }

    private byte[] readRaw(Resources res, int id) throws IOException {
        if (id == 0) {
            return null;
        }
        InputStream is = res.openRawResource(id);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int read;
            while ((read = is.read(buf)) > 0) {
                bos.write(buf, 0, read);
            }
            bos.flush();
            return bos.toByteArray();
        } finally {
            is.close();
        }
    }

    // XXX: This is a major hack to remove our dependency on gsf constants and
    // its content provider. http://b/issue?id=2425179
    private String getClientId(ContentResolver cr) {
        String ret = "android-google";
        Cursor c = null;
        try {
            c = cr.query(Uri.parse("content://com.google.settings/partner"),
                    new String[]{"value"}, "name='client_id'", null, null);
            if (c != null && c.moveToNext()) {
                ret = c.getString(0);
            }
        } catch (RuntimeException ex) {
            // fall through to return the default
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return ret;
    }

    private CharSequence replaceSystemPropertyInString(Context context, CharSequence srcString) {
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

    /**
     * Settings are unique, so perform an UPSERT manually since SQLite doesn't support them.
     */
    public long insertSettingsInTransaction(SQLiteDatabase db, ContentValues values) {
        String key = values.getAsString(BrowserContract.Settings.KEY);
        if (TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException("Must include the KEY field");
        }
        String[] keyArray = new String[]{key};
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_SETTINGS, new String[]{BrowserContract.Settings.KEY},
                    BrowserContract.Settings.KEY + "=?", keyArray, null, null, null);
            if (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                db.update(TABLE_SETTINGS, values, BrowserContract.Settings.KEY + "=?", keyArray);
                return id;
            } else {
                return db.insertOrThrow(TABLE_SETTINGS, BrowserContract.Settings.VALUE, values);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
