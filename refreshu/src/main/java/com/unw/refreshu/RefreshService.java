package com.unw.refreshu;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import net.pocketmagic.android.eventinjector.Events;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by unw on 15. 4. 17..
 */
    public class RefreshService extends Service implements View.OnTouchListener, InputDeviceEvent.KeyInputEventListener
{
    private static final String TAG = "RefreshSevice";

    // TODO 사용자 입력가능하도록
    private int KEY_BACK;
    private int KEY_PREVIOUS;
    private int KEY_NEXT;

    /**
     * 화면 리프레시 실행 터치 횟수 설정
     */
    private int FULL_REFRESH_COUNT;

    /**
     * 액티비티 등록 실행 터치 횟수 설정
     */
    private static final int REGISTER_ACTIVITY_TOUCH_COUNT = 3;

    private boolean EDIT_MODE;

    /**
     * TODO 사용자화
     * 사용자가 리프레시 서비스를 실행하기 위해 입력하는 터치간의 인터벌
     */
    private static final int USER_TOUCH_INTERVAL = 500;

    private static final String FILE_NAME_SETTING = "refreshU.json";

    private static final ActivityInfo IGNORE_ACTIVITY = new ActivityInfo("com.unw.refreshu.MainActivity", "com.unw.refreshu");

    private int mRegisterTouchCount;
    private int mRefreshTouchCount;

    private boolean bPreventedDoubleTouch;
    private RefreshView mPreventTouchView;

    private Toast mToast;

    private InputDeviceEvent mInputDeviceEvent;
    private Events.InputDevice mTouchInputDevice;

    private TextView mRefreshTargetView;

    private List<ActivityInfo> mActivityInfos;

    private int mKeyCount = 1;
    private Handler mHandler = new Handler();
    private Runnable mResetCountRunnable = new Runnable() {
        @Override
        public void run() {
            mRegisterTouchCount = 0;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        //mInputDeviceEvent = InputDeviceEvent.getInstance();
        mInputDeviceEvent = new InputDeviceEvent();
        mInputDeviceEvent.setKeyInputEventListener(this);
        mInputDeviceEvent.openInputEvents();
        // TODO 현재 T62+에만 초점이 맞춰져있고 확인한 상태라서 event2가 터치InputDevice가 아닐 수 도 있다.
        // 모든 기기(RK3026)에서 작동되기 위해서는 모든 InputDevice를 다 열어서 확인해 볼 필요가 있을 수도
        mTouchInputDevice = mInputDeviceEvent.getInputDevice("cyttsp");

        mRefreshTargetView = new TextView(this);
        mRefreshTargetView.setOnTouchListener(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        WindowManager winmgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        winmgr.addView(mRefreshTargetView, lp);

        makeToast(getString(R.string.service_wait_launch_msg));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        KEY_NEXT = settings.getInt(getString(R.string.setting_keymap_next_page_key), 0);
        KEY_PREVIOUS = settings.getInt(getString(R.string.setting_keymap_pre_page_key), 0);
        FULL_REFRESH_COUNT = Integer.parseInt(settings.getString(getString(R.string.setting_refresh_count_key), "0"));
        EDIT_MODE = settings.getBoolean(getString(R.string.setting_edit_mode_key), false);
        String json = settings.getString(getString(R.string.setting_activity_infos_key), "");
        mActivityInfos = Util.transActivityInfoJsonToList(json);
        bPreventedDoubleTouch = settings.getBoolean(getString(R.string.setting_prevent_double_touch_key), false);

        Log.w(TAG, "next : " + KEY_NEXT + ", pre : " + KEY_PREVIOUS);

        return Service.START_STICKY;
    }

    private void setPreventTouchView(boolean add)
    {
        WindowManager winmgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

        if (add) {
            if (mPreventTouchView == null) {
                mPreventTouchView = new RefreshView(getBaseContext());
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        PixelFormat.TRANSLUCENT);

                winmgr.addView(mPreventTouchView, lp);
            }
        } else {
            if (mPreventTouchView != null) {
                winmgr.removeView(mPreventTouchView);
                mPreventTouchView = null;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        makeToast(getString(R.string.service_wait_fire_msg));

        if (mInputDeviceEvent != null) {
            if (mInputDeviceEvent.isRun())
                mInputDeviceEvent.stopEventMonitor();

            mInputDeviceEvent.closeInputEvents();
        }

        WindowManager winmgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        winmgr.removeView(mRefreshTargetView);

        setPreventTouchView(false);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        ActivityInfo currentActivity = getCurrentActivityInfo();

        if (currentActivity.equals(IGNORE_ACTIVITY))
            return true;

        if (isRegisteredActivity(currentActivity)) {

            mRefreshTouchCount = ++mRefreshTouchCount % FULL_REFRESH_COUNT;
            Log.d(TAG, "mRefreshTouchCount : " + mRefreshTouchCount + "/" + FULL_REFRESH_COUNT);

            setPreventTouchView(bPreventedDoubleTouch);

            if (mInputDeviceEvent != null && !mInputDeviceEvent.isRun()) {
                mInputDeviceEvent.startEventMonitor();
                makeToast(getString(R.string.service_refreshu_launch_msg_fmt, FULL_REFRESH_COUNT));
            }

            // ref : http://g-mini.ru/forum/topic/3827/
            if (mRefreshTouchCount == 0)
                mRefreshTargetView.invalidate(-1010, -1010,-1010,-1010);

        } else {

            setPreventTouchView(false);

            if (mInputDeviceEvent != null && mInputDeviceEvent.isRun()) {
                mInputDeviceEvent.stopEventMonitor();
                makeToast(getString(R.string.service_wait_launch_msg));
            }

            // Register Activity
            if (EDIT_MODE) {
                mHandler.removeCallbacks(mResetCountRunnable);

                mRegisterTouchCount = ++mRegisterTouchCount % REGISTER_ACTIVITY_TOUCH_COUNT;
                Log.d(TAG, "mRegistTouchCount : " + mRegisterTouchCount + "/" + REGISTER_ACTIVITY_TOUCH_COUNT);

                if (mRegisterTouchCount == 0) {
                    makeToast(getString(R.string.service_register_activity_msg));
                    registerActivity(currentActivity);
                } else {
                    makeToast(getString(R.string.service_register_activity_mode_msg_fmt, (REGISTER_ACTIVITY_TOUCH_COUNT - mRegisterTouchCount)));
                    mHandler.postDelayed(mResetCountRunnable, USER_TOUCH_INTERVAL);
                }
            }
        }

        return true;
    }

    private boolean isRegisteredActivity(ActivityInfo activityName)
    {
        boolean isRegistered = false;

        if (mActivityInfos != null && mActivityInfos.size() != 0) {
            for (ActivityInfo info : mActivityInfos) {
                if (info.equals(activityName))
                    isRegistered = true;
            }
        }

        return isRegistered;
    }

    private ActivityInfo getCurrentActivityInfo()
    {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1);

        ComponentName componentInfo = taskInfo.get(0).topActivity;

        Log.d(TAG, "componentInfo] className : " + componentInfo.getClassName()
                                + ", packageName : " + componentInfo.getPackageName());

        return new ActivityInfo(componentInfo.getClassName(), componentInfo.getPackageName());
    }

    private void registerActivity(ActivityInfo activityInfo)
    {
        mActivityInfos.add(activityInfo);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.edit()
                .putString(getString(R.string.setting_activity_infos_key), Util.transListToJson(mActivityInfos))
                .commit();

//        String json = settings.getString(getString(R.string.setting_activity_infos_key), "");
//        Log.d(TAG, "Json : " + json);

    }

    private List<ActivityInfo> readSettingFile()
    {
        List<ActivityInfo> infos = null;

        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME_SETTING);

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            StringBuilder builder = new StringBuilder();
            while ( (line = br.readLine()) != null )
            {
                builder.append(line);
            }
            br.close();

            JSONArray jsonArray = new JSONArray(builder.toString());
            int size = jsonArray.length();
            infos = new ArrayList<ActivityInfo>(size);
            for (int i = 0; i < size; i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                String className = json.getString(ActivityInfo.COLUMN_ACTIVITY_INFO_ACTIVITY_NAME);
                String packageName = json.getString(ActivityInfo.COLUMN_ACTIVITY_INFO_PACKAGE_NAME);

                ActivityInfo info = new ActivityInfo(className, packageName);
                infos.add(info);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return infos;
    }

    private void writeSettingFile(String setting)
    {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME_SETTING);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(setting.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void makeToast(String msg)
    {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onKeyDown(int inputType, int keyCode)
    {

        return false;
    }

    @Override
    public boolean onKeyUp(int inputType, int keyCode)
    {
        if (keyCode == KEY_BACK) {
            //stopSelf();
        } else if (keyCode == KEY_NEXT) {
            //TODO 실제 작동중인 기기의 화면사이즈를 가져와서 적용
            //TODO 사용자가 설정가능
            sendTouchEvent(552, 104);
        } else  if (keyCode == KEY_PREVIOUS) {
            sendTouchEvent(552, 674);
        }
        return false;
    }

    public void sendTouchEvent(int x, int y) {
        if (mTouchInputDevice != null)
            mTouchInputDevice.SendTouchEvent(x, y);
    }


    /******************
     * Class
     */
    class RefreshView extends TextView
    {
        private static final String TAG = "RefreshView";

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
            // 단지 키 이벤트를 잡아두기 위함.
            // 리디에서 두페이지 넘어가는 걸 막기 위함.
            Log.d(this.TAG, "onKeyDown() : " + keyCode);

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                setPreventTouchView(false);
            }
            return true;
        }


    }
}
