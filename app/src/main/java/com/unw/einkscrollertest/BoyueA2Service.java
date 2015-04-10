package com.unw.einkscrollertest;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by unw on 15. 3. 25..
 */
public class BoyueA2Service extends Service implements View.OnTouchListener, RefreshView.OnPageKeyListener {

    private String TAG;
    private RefreshView mContentView = null;
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

    private int FULL_REFRESH_COUNT;

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

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = getClass().getSimpleName();

        Toast.makeText(getBaseContext(), "Service is Created", Toast.LENGTH_SHORT).show();

        mContentView = new RefreshView(this);
        mContentView.setOnTouchListener(this);
        mContentView.setOnPageKeyListener(this);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                //WindowManager.LayoutParams.TYPE_PHONE,
               WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        //WindowManager.LayoutParams. FLAG_NOT_TOUCH_MODAL
                        //| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        WindowManager winmgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        winmgr.addView(mContentView, lp);
/*
        IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);

        intentFilter.setPriority(500);

        final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.w("onReceive", "음흥?");
                String intentAction = intent.getAction();
                if ("android.media.VOLUME_CHANGED_ACTION".equals(intentAction)) {

                    int vol = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1);
                    int preVol = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1);
                    Log.w("onReceive", "vol : " + (vol > preVol ? "+" : "-"));

                    if (vol > preVol) { // +
                        am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_RAISE,
                                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        //am.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                    }


                    //abortBroadcast();
                }
            }
        };

        registerReceiver(mBroadcastReceiver, intentFilter);

*//*
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ComponentName rec = new ComponentName(getPackageName(),
                RemoteControlReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(rec);
*/

        //makeToast("refreshU 서비스 시작됨");
        Log.w("TTT", "refreshU 서비스 시작됨");

        test();

        isRunning = true;
    }

    private void test() {

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    Process process = Runtime.getRuntime().exec("logcat -d");
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    StringBuilder log = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        log.append(line);
                    }
                    mContentView.setText(log.toString());
                } catch (IOException e) {
                }
            }
        };
        mHandler.post(runnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_STICKY;
    }

    private void makeToast(String msg)
    {
        if (mToast != null) mToast.cancel();

        mToast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private static int touchCount = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 즉시 업데이트 되기 위해서임
        FULL_REFRESH_COUNT = Settings.System.getInt(getContentResolver(), "fullscreen_flush", 0);

        touchCount = (++touchCount) % FULL_REFRESH_COUNT;
        Log.w("", "Touch count : " + touchCount + "/" + FULL_REFRESH_COUNT);

        if (touchCount == 0) {
            mContentView.invalidate(-1010, -1010,-1010,-1010);
        }

        return false;
    }

    @Override
    public boolean onPageNext() {
        Log.w(TAG, "onPageNext");
        /*
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        MotionEvent touchDown = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 100.0f, 100.0f, 0);
        mContentView.dispatchTouchEvent(touchDown);
        */

        return false;
    }

    @Override
    public boolean onPagePrevious() {
        Log.w(TAG, "onPagePrevious");
        return false;
    }
}
