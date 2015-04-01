package com.unw.webkit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

import com.unw.device.epdcontrol.EPDController;
import com.unw.device.epdcontrol.EPDFactory;
import com.unw.device.epdcontrol.rockchip.T62EPDController;

/**
 * Created by unw on 15. 3. 31..
 */
public class EPDWebView extends WebView
{
    private static final String TAG = "EPDWebView";

    public static String EPD_MODE = T62EPDController.EPD_FULL_DITHER;

    private EPDController mEpdController;

    public EPDWebView(Context context) {
        super(context);

        init();
    }

    public EPDWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public EPDWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init()
    {
        mEpdController = EPDFactory.getEPDController();
    }

    public void setEpdMode(String mode) {
        EPD_MODE = mode;
    }
    
    private void excuteEpd()
    {
        mEpdController.setEpdMode(this, EPD_MODE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        excuteEpd();
        super.onDraw(canvas);
        Log.d(TAG, "onDraw()");
    }

    @Override
    public void invalidate() {
        excuteEpd();
        super.invalidate();
        Log.d(TAG, "invalidate()");
    }

    @Override
    public void invalidate(Rect dirty) {
        excuteEpd();
        super.invalidate(dirty);
        Log.d(TAG, "invalidate(Rect dirty)");
    }

    @Override
    public void invalidate(int l, int t, int r, int b) {
        excuteEpd();
        super.invalidate(l, t, r, b);
        Log.d(TAG, "invalidate(int l, int t, int r, int b)");
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        excuteEpd();
        super.invalidateDrawable(drawable);
        Log.d(TAG, "invalidateDrawable(Drawable drawable)");
    }

    @Override
    public void postInvalidate() {
        excuteEpd();
        super.postInvalidate();
        Log.d(TAG, "postInvalidate()");
    }

    @Override
    public void postInvalidate(int left, int top, int right, int bottom) {
        excuteEpd();
        super.postInvalidate(left, top, right, bottom);
        Log.d(TAG, "postInvalidate(int left, int top, int right, int bottom)");
    }

    @Override
    public void postInvalidateDelayed(long delayMilliseconds) {
        excuteEpd();
        super.postInvalidateDelayed(delayMilliseconds);
        Log.d(TAG, "postInvalidate(long delayMilliseconds)");
    }

    @Override
    public void postInvalidateDelayed(long delayMilliseconds, int left, int top, int right, int bottom) {
        excuteEpd();
        super.postInvalidateDelayed(delayMilliseconds, left, top, right, bottom);
        Log.d(TAG, "postInvalidateDelayed(long delayMilliseconds, int left, int top, int right, int bottom)");
    }

    @Override
    public void postInvalidateOnAnimation() {
        excuteEpd();
        super.postInvalidateOnAnimation();
        Log.d(TAG, "postInvalidateOnAnimation()");
    }

    @Override
    public void postInvalidateOnAnimation(int left, int top, int right, int bottom) {
        excuteEpd();
        super.postInvalidateOnAnimation(left, top, right, bottom);
        Log.d(TAG, "postInvalidateOnAnimation(int left, int top, int right, int bottom)");
    }

    @Override
    public void scrollBy(int x, int y) {
        excuteEpd();
        super.scrollBy(x, y);
        Log.d(TAG, "scrollBy()");
    }

    @Override
    public void scrollTo(int x, int y) {
        excuteEpd();
        super.scrollTo(x, y);
        Log.d(TAG, "scrollTo()");
    }
}
