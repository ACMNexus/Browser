package com.qirui.browser.bean;

/**
 * Created by Luooh on 2017/3/24.
 */
public enum SecurityState {

    // The page's main resource does not use SSL. Note that we use this
    // state irrespective of the SSL authentication state of sub-resources.
    SECURITY_STATE_NOT_SECURE,
    // The page's main resource uses SSL and the certificate is good. The
    // same is true of all sub-resources.
    SECURITY_STATE_SECURE,
    // The page's main resource uses SSL and the certificate is good, but
    // some sub-resources either do not use SSL or have problems with their
    // certificates.
    SECURITY_STATE_MIXED,
    // The page's main resource uses SSL but there is a problem with its
    // certificate.
    SECURITY_STATE_BAD_CERTIFICATE,
}
