package com.unw.einkscrollertest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by unw on 15. 3. 25..
 */
public class BoyueA2Service extends Service implements View.OnTouchListener {

    private TextView mContentView = null;
    private Toast mToast;

    private boolean isRunning = false;

    private Handler mHandler = new Handler();

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            //Log.w("Service", "작동중");

            //EpdController.requestEpdMode(mContentView, EpdController.stringToEnum("EPD_FULL"));

            if (isRunning)
                mHandler.postDelayed(this, 500L);
            //mHandler.post(this);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        makeToast("refreshU 서비스 중지됨");

        isRunning = false;

        WindowManager winmgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        winmgr.removeView(mContentView);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getBaseContext(), "Service is Created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

            mContentView = new TextView(this);
        mContentView.setText("항상위에 있는 뷰");
        mContentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mContentView.setTextColor(Color.BLACK);
        mContentView.setBackgroundColor(Color.WHITE);
            mContentView.setOnTouchListener(this);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
            WindowManager winmgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
            winmgr.addView(mContentView, lp);

            mHandler.postDelayed(mRunnable, 1L);

            //makeToast("refreshU 서비스 시작됨");
        Log.w("TTT", "refreshU 서비스 시작됨");

        isRunning = true;

        return Service.START_STICKY;
    }

    private void makeToast(String msg)
    {
        if (mToast != null) mToast.cancel();

        mToast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT);
        mToast.show();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        return false;
    }
}
