package com.qirui.browser.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import com.qirui.browser.R;
import com.qirui.browser.TitleBar;

/**
 * Created by Luooh on 2017/3/27.
 */
public class AnimScreen {

    public View mMain;
    public ImageView mTitle;
    public ImageView mContent;
    public float mScale;
    public Bitmap mTitleBarBitmap;
    public Bitmap mContentBitmap;

    public AnimScreen(Context ctx) {
        mMain = LayoutInflater.from(ctx).inflate(R.layout.anim_screen, null);
        mTitle = (ImageView) mMain.findViewById(R.id.title);
        mContent = (ImageView) mMain.findViewById(R.id.content);
        mContent.setScaleType(ImageView.ScaleType.MATRIX);
        mContent.setImageMatrix(new Matrix());
        mScale = 1.0f;
        setScaleFactor(getScaleFactor());
    }

    public void set(TitleBar tbar, WebView web) {
        if (tbar == null || web == null) {
            return;
        }
        if (tbar.getWidth() > 0 && tbar.getEmbeddedHeight() > 0) {
            if (mTitleBarBitmap == null || mTitleBarBitmap.getWidth() != tbar.getWidth()
                    || mTitleBarBitmap.getHeight() != tbar.getEmbeddedHeight()) {
                mTitleBarBitmap = safeCreateBitmap(tbar.getWidth(), tbar.getEmbeddedHeight());
            }
            if (mTitleBarBitmap != null) {
                Canvas c = new Canvas(mTitleBarBitmap);
                tbar.draw(c);
                c.setBitmap(null);
            }
        } else {
            mTitleBarBitmap = null;
        }
        mTitle.setImageBitmap(mTitleBarBitmap);
        mTitle.setVisibility(View.VISIBLE);
        int h = web.getHeight() - tbar.getEmbeddedHeight();
        if (mContentBitmap == null
                || mContentBitmap.getWidth() != web.getWidth()
                || mContentBitmap.getHeight() != h) {
            mContentBitmap = safeCreateBitmap(web.getWidth(), h);
        }
        if (mContentBitmap != null) {
            Canvas c = new Canvas(mContentBitmap);
            int tx = web.getScrollX();
            int ty = web.getScrollY();
            c.translate(-tx, -ty - tbar.getEmbeddedHeight());
            web.draw(c);
            c.setBitmap(null);
        }
        mContent.setImageBitmap(mContentBitmap);
    }

    private Bitmap safeCreateBitmap(int width, int height) {
        if (width <= 0 || height <= 0) {
            Log.w("LOH", "safeCreateBitmap failed! width: " + width + ", height: " + height);
            return null;
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    }

    public void set(Bitmap image) {
        mTitle.setVisibility(View.GONE);
        mContent.setImageBitmap(image);
    }

    public void setScaleFactor(float sf) {
        mScale = sf;
        Matrix m = new Matrix();
        m.postScale(sf, sf);
        mContent.setImageMatrix(m);
    }

    private float getScaleFactor() {
        return mScale;
    }
}
