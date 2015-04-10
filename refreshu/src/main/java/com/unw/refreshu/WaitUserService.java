package com.unw.refreshu;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by unw on 15. 4. 9..
 */
public class WaitUserService extends Service implements View.OnTouchListener
{
    private static final String TAG = "WaitUserService";

    /**
     * TODO 사용자화
     * 리프레시 서비스 실행 터치 횟수 설정
     */
    private static final int RUN_TOUCH_COUNT = 3;

    /**
     * TODO 사용자화
     * 사용자가 리프레시 서비스를 실행하기 위해 입력하는 터치간의 인터벌
     */
    private static final int USER_TOUCH_INTERVAL = 1000;

    private Handler mHandler = new Handler();

    private Runnable mResetCountRunnable = new Runnable() {
        @Override
        public void run() {
            mTouchCount = 0;
        }
    };

    private BroadcastReceiver mBroadcastReceiver;

    private int mTouchCount;

    private Toast mToast;

    private TextView mContentView;

    private boolean bRefreshSeviceRun = false;

    @Override
    public void onCreate() {
        super.onCreate();

        mContentView = new TextView(this);
        mContentView.setOnTouchListener(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        WindowManager winmgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        winmgr.addView(mContentView, lp);

        makeToast(getString(R.string.service_launch_msg_fmt, TAG));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter intentFilter = new IntentFilter(RefreshSevice.BROADCAST_REFRESH_SERVICE);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                bRefreshSeviceRun = intent.getBooleanExtra(RefreshSevice.STATE_REFRESH_SERVICE, false);
            }
        };

        registerReceiver(mBroadcastReceiver, intentFilter);

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        makeToast(getString(R.string.service_fire_msg_fmt, TAG));

        stopService(new Intent(this, RefreshSevice.class));

        unregisterReceiver(mBroadcastReceiver);

        WindowManager winmgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        winmgr.removeView(mContentView);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        mHandler.removeCallbacks(mResetCountRunnable);

        mTouchCount = ++mTouchCount % RUN_TOUCH_COUNT;
        Log.d(TAG, "TouchCount : " + mTouchCount + "/" + RUN_TOUCH_COUNT);

        if (mTouchCount == 0) {

            Intent intent = new Intent(this, RefreshSevice.class);
            if (bRefreshSeviceRun) {
                stopService(intent);
            } else {
                startService(intent);
            }

        } else {
            mHandler.postDelayed(mResetCountRunnable, USER_TOUCH_INTERVAL);
        }

        return false;
    }

    public void makeToast(String msg)
    {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }


}
