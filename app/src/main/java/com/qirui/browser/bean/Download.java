package com.qirui.browser.bean;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import com.qirui.browser.DataUri;
import com.qirui.browser.DownloadHandler;
import com.qirui.browser.util.IOUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Luooh on 2017/3/24.
 */
public class Download implements MenuItem.OnMenuItemClickListener {

    private Activity mActivity;
    private String mText;
    private boolean mPrivateBrowsing;
    private String mUserAgent;
    private static final String FALLBACK_EXTENSION = "dat";
    private static final String IMAGE_BASE_FORMAT = "yyyy-MM-dd-HH-mm-ss-";

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (DataUri.isDataUri(mText)) {
            saveDataUri();
        } else {
            DownloadHandler.onDownloadStartNoStream(mActivity, mText, mUserAgent, null, null, null, mPrivateBrowsing);
        }
        return true;
    }

    public Download(Activity activity, String toDownload, boolean privateBrowsing, String userAgent) {
        mActivity = activity;
        mText = toDownload;
        mPrivateBrowsing = privateBrowsing;
        mUserAgent = userAgent;
    }

    /**
     * Treats mText as a data URI and writes its contents to a file
     * based on the current time.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void saveDataUri() {
        FileOutputStream outputStream = null;
        try {
            DataUri uri = new DataUri(mText);
            File target = getTarget(uri);
            outputStream = new FileOutputStream(target);
            outputStream.write(uri.getData());
            final DownloadManager manager = (DownloadManager) mActivity.getSystemService(Context.DOWNLOAD_SERVICE);
            manager.addCompletedDownload(target.getName(),
                    mActivity.getTitle().toString(), false,
                    uri.getMimeType(), target.getAbsolutePath(),
                    uri.getData().length, true);
        } catch (IOException e) {
            Log.e("Download", "Could not save data URL");
        } finally {
            IOUtils.closeStream(outputStream);
        }
    }

    /**
     * Creates a File based on the current time stamp and uses
     * the mime type of the DataUri to get the extension.
     */
    private File getTarget(DataUri uri) throws IOException {
        File dir = mActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        DateFormat format = new SimpleDateFormat(IMAGE_BASE_FORMAT, Locale.US);
        String nameBase = format.format(new Date());
        String mimeType = uri.getMimeType();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = mimeTypeMap.getExtensionFromMimeType(mimeType);
        if (extension == null) {
            Log.w("Download", "Unknown mime type in data URI" + mimeType);
            extension = FALLBACK_EXTENSION;
        }
        extension = "." + extension; // createTempFile needs the '.'
        File targetFile = File.createTempFile(nameBase, extension, dir);
        return targetFile;
    }
}
