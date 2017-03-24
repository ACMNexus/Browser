package com.qirui.browser.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.URLUtil;
import com.qirui.browser.R;

/**
 * Created by Luooh on 2017/3/24.
 */
public class PageState {

    public String mUrl;
    public String mOriginalUrl;
    public String mTitle;
    public SecurityState mSecurityState;
    // This is non-null only when mSecurityState is SECURITY_STATE_BAD_CERTIFICATE.
    public SslError mSslCertificateError;
    public Bitmap mFavicon;
    public boolean mIsBookmarkedSite;
    public boolean mIncognito;

    public PageState(Context c, boolean incognito) {
        mIncognito = incognito;
        if (mIncognito) {
            mOriginalUrl = mUrl = "browser:incognito";
            mTitle = c.getString(R.string.new_incognito_tab);
        } else {
            mOriginalUrl = mUrl = "";
            mTitle = c.getString(R.string.new_tab);
        }
        mSecurityState = SecurityState.SECURITY_STATE_NOT_SECURE;
    }

    public PageState(Context c, boolean incognito, String url, Bitmap favicon) {
        mIncognito = incognito;
        mOriginalUrl = mUrl = url;
        if (URLUtil.isHttpsUrl(url)) {
            mSecurityState = SecurityState.SECURITY_STATE_SECURE;
        } else {
            mSecurityState = SecurityState.SECURITY_STATE_NOT_SECURE;
        }
        mFavicon = favicon;
    }
}
