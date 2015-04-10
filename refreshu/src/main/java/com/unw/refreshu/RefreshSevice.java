package com.unw.refreshu;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import net.pocketmagic.android.eventinjector.Events;

/**
 * Created by unw on 15. 4. 9..
 */
public class RefreshSevice extends Service implements View.OnTouchListener
{
    private static final String TAG = "RefreshSevice";

    public static final String BROADCAST_REFRESH_SERVICE = "BROADCAST_REFRESH_SERVICE";

    public static final String STATE_REFRESH_SERVICE = "STATE_REFRESH_SERVICE";

    /**
     * 화면 리프레시 실행 터치 횟수 설정
     */
    private int FULL_REFRESH_COUNT;

    private int mTouchCount;

    private Toast mToast;

    private RefreshView mContentView;

    private Events mEvent = new Events();

    private Events.InputDevice mTouchInputDevice;

    /******************
     * Life cycle
     */

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate()");

        Events.intEnableDebug(1);
        mEvent.Init();

        mContentView = new RefreshView(this);
        mContentView.setOnTouchListener(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams. FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        WindowManager winmgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        winmgr.addView(mContentView, lp);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        FULL_REFRESH_COUNT = Integer.parseInt(settings.getString(getString(R.string.setting_refresh_count_key), "0"));

        makeToast(getString(R.string.service_launch_msg_fmt, TAG));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand()");

        Intent refreshIntent = new Intent(BROADCAST_REFRESH_SERVICE);
        refreshIntent.putExtra(STATE_REFRESH_SERVICE, true);
        sendBroadcast(refreshIntent);

        //test();
        setTouchInputDevice();

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy()");

        makeToast(getString(R.string.service_fire_msg_fmt, TAG));

        //
        Intent refreshIntent = new Intent(BROADCAST_REFRESH_SERVICE);
        refreshIntent.putExtra(STATE_REFRESH_SERVICE, false);
        sendBroadcast(refreshIntent);

        //
        WindowManager winmgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        winmgr.removeView(mContentView);

        //
        mTouchInputDevice.Close();
        mEvent.Release();
    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG, "onBind()");

        return null;
    }

    /*****************
     *
     */

    private void setTouchInputDevice()
    {
        for (Events.InputDevice inputDevice : mEvent.m_Devs) {
            //TODO 현재 T62+에만 초점이 맞춰져있고 확인한 상태라서 event2가 터치InputDevice가 아닐 수 도 있다.
            // 모든 기기(RK3026)에서 작동되기 위해서는 모든 InputDevice를 다 열어서 확인해 볼 필요가 있을 수도
            if (inputDevice.getPath().contains("event2")) {
                inputDevice.Open(true);
                mTouchInputDevice = inputDevice;
            }
        }
    }

    private void test() {
        Events.intEnableDebug(1);
        mEvent.Init();

        for (Events.InputDevice inputDevice : mEvent.m_Devs) {
            StringBuilder builder = new StringBuilder("[InputDevice] ")
                    .append("Name : ").append(inputDevice.getName())
                    .append(", Id : ").append(inputDevice.getId())
                    .append(", Path : ").append(inputDevice.getPath())
                    .append(", Open? : ").append(inputDevice.getOpen());

            Log.d(TAG, "[before open] " + builder.toString());

            inputDevice.Open(true);

            builder = new StringBuilder("[InputDevice] ")
                    .append("Name : ").append(inputDevice.getName())
                    .append(", Id : ").append(inputDevice.getId())
                    .append(", Path : ").append(inputDevice.getPath())
                    .append(", Open? : ").append(inputDevice.getOpen());
            Log.d(TAG, "[after open] " + builder.toString());

            /*
            [InputDevice] Name : rk29-keypad, Id : 0, Path : /dev/input/event1, Open? : true
            [InputDevice] Name : cyttsp4_mt, Id : 1, Path : /dev/input/event2, Open? : true << Touch
            [InputDevice] Name : axp22-supplyer, Id : 2, Path : /dev/input/event0, Open? : true
            [InputDevice] Name : rk_headsetdet, Id : 3, Path : /dev/input/event3, Open? : true
             */
        }

        //startEventMonitor();


    }

    public void startEventMonitor() {
        Thread b = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    for (Events.InputDevice idev:mEvent.m_Devs) {
                        // Open more devices to see their messages
                        if (idev.getOpen() && (0 == idev.getPollingEvent())) {
                            final String line = idev.getName()+
                                    ":" + idev.getSuccessfulPollingType()+
                                    " " + idev.getSuccessfulPollingCode() +
                                    " " + idev.getSuccessfulPollingValue();
                            Log.d(TAG, "Event:"+line);
                        }

                    }
                }
            }
        });
        b.start();
    }



    /******************
     *
     */

    public void sendTouchEvent(int x, int y) {
        /*
        for (Events.InputDevice idev : mEvent.m_Devs) {
            if (idev.getOpen() && idev.getName().contains("cyttsp4_mt")) {
                idev.SendTouchEvent(x, y);
            }
        }
        */

        mTouchInputDevice.SendTouchEvent(x, y);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        mTouchCount = (++mTouchCount) % FULL_REFRESH_COUNT;
        Log.d("", "Touch count : " + mTouchCount + "/" + FULL_REFRESH_COUNT);

        if (mTouchCount == 0) {
            // full refresh
            mContentView.invalidate(-1010, -1010,-1010,-1010);
        }

        return true;
    }

    public void makeToast(String msg)
    {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    /******************
     * Class
     */

    class RefreshView extends TextView
    {
        private static final String TAG = "RefreshView";

        // TODO 사용자 입력가능하도록
        private static final int KEY_BACK = 4;
        private static final int KEY_PAGE_PREVIOUS = 92;
        private static final int KEY_PAGE_NEXT = 93;

        public RefreshView(Context context) {
            super(context);
        }

        public RefreshView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {

            Log.d(TAG, "onKeyDown(), keyCode : " +keyCode);
            switch (keyCode) {
                case KEY_BACK :
                    stopSelf();
                    break;
                case KEY_PAGE_NEXT :
                    //TODO 실제 작동중인 기기의 화면사이즈를 가져와서 적용
                    //TODO 사용자가 설정가능
                    sendTouchEvent(552, 104);
                    break;
                case KEY_PAGE_PREVIOUS :
                    sendTouchEvent(552, 674);
                    break;

            }
            return true;
        }
    }

}
