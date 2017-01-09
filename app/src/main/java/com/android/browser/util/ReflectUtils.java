package com.android.browser.util;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Proxy;
import android.net.http.SslCertificate;
import android.provider.Browser;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.apache.http.client.methods.HttpPost;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Luooh on 2017/1/8.
 */

public class ReflectUtils {

    private static ReflectUtils mInstance = new ReflectUtils();

    private ReflectUtils() {
    }

    public static ReflectUtils getInstance() {
        return mInstance;
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?> ...values) {
        Method method = null;
        try {
            if(values != null) {
                method = clazz.getDeclaredMethod(methodName, values);
            }else {
                method = clazz.getDeclaredMethod(methodName);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }

    public static Method getMethod(String className, String methodName, Class<?> ...values) {
        Method method = null;
        try {
            Class<?> clazz = Class.forName(className);
            if(clazz != null) {
                if(values != null) {
                    method = clazz.getDeclaredMethod(methodName, values);
                }else {
                    method = clazz.getDeclaredMethod(methodName);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }

    public static Object invokeStaticMethod(Method method, Object ...values) {
        if(method != null) {
            try {
                return method.invoke(null, values);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Object invokeMethod(Method method, Object classValue, Object ...values) {
        if(method != null) {
            try {
                return method.invoke(classValue, values);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /*public Object invokeMethod(Method method, Object classValue) {
        if(method != null) {
            try {
                return method.invoke(classValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }*/

    public static void sendString(Context context, String url, String title) {
        Method method = getMethod(Browser.class, "sendString", context.getClass(), String.class, String.class);
        invokeStaticMethod(method, context, url, title);
    }

    public static HttpPost getPreferredHttpHost(Context context, String url) {
        Method method = getMethod(Proxy.class, "getPreferredHttpHost", context.getClass(), String.class);
        HttpPost httpPost = (HttpPost) invokeStaticMethod(method, context, url);
        return httpPost;
    }

    public static String[] getVisitedHistory(ContentResolver resolver) {
        Method method = getMethod(Browser.class, "getVisitedHistory", ContentResolver.class);
        return (String[]) invokeStaticMethod(method, resolver);
    }

    public static void enablePlatformNotifications() {
        Method method = getMethod(WebView.class, "enablePlatformNotifications");
        invokeStaticMethod(method);
    }

    public static void debugDump(WebView webView) {
        Method method = getMethod(WebView.class, "debugDump");
        invokeMethod(method, webView);
    }

    public static void disablePlatformNotifications() {
        Method method = getMethod(WebView.class, "disablePlatformNotifications");
        invokeStaticMethod(method);
    }

    public static int getVisibleTitleHeight(WebView webView){
        Method method = getMethod(WebView.class, "getVisibleTitleHeight");
        return (Integer) invokeMethod(method, webView);
    }

    public static void setNavDump(WebSettings webSettings, boolean enabled) {
        Method method = getMethod(WebSettings.class, "setNavDump", boolean.class);
        invokeMethod(method, webSettings, enabled);
    }

    public static String getTouchIconUrl(WebView webView) {
        Method method = getMethod(WebView.class, "getTouchIconUrl");
        return (String)invokeMethod(method, webView);
    }

    public static int getContentWidth(WebView webView) {
        Method method = getMethod(WebView.class, "getContentWidth");
        int width = (Integer) invokeMethod(method, webView);
        return width;
    }

    public static Cursor getSuggestions(SearchManager searchManager, String query) {
        Method method = getMethod(SearchManager.class, "getSuggestions", String.class);
        Cursor cursor = (Cursor) invokeMethod(method, searchManager, query);
        return cursor;
    }

    public static ComponentName getWebSearchActivity(SearchManager searchManager){
        Method method = getMethod(SearchManager.class, "getWebSearchActivity");
        ComponentName componentName = (ComponentName) invokeMethod(method, searchManager);
        return componentName;
    }

    public static boolean suppressDialog(HttpAuthHandler httpAuthHandler) {
        Method method = getMethod(HttpAuthHandler.class, "suppressDialog");
        return (Boolean) invokeMethod(method, httpAuthHandler);
    }

    public File getSharedPrefsFile(Context context, String fileName) {
        Method method = getMethod(Context.class, "getSharedPrefsFile", String.class);
        File file = (File) invokeMethod(method, context, fileName);
        return file;
    }

    public View inflateCertificateView(Context context, SslCertificate certificate) {
        Method method = getMethod(SslCertificate.class, "inflateCertificateView", Context.class);
        return (View) invokeMethod(method, certificate, context);
    }
}
