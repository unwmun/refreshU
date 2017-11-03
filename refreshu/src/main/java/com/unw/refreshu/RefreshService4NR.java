package com.unw.refreshu;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import android.widget.Toast;

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
 * Created by unw on 17. 11. 3..
 * RefreshService를 루팅없이 사용할 수 있도록 수정.
 */
public class RefreshService4NR extends AccessibilityService implements View.OnTouchListener, View.OnKeyListener, View.OnClickListener {
    private static final String TAG = "RefreshSevice4NR";

    // TODO 사용자 입력가능하도록
    private int KEY_BACK;
    private int KEY_PREVIOUS;
    private int KEY_NEXT;
    private int KEY_REFRESH;

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

    private Toast mToast;

    private TextView mRefreshTargetView;

    private List<ActivityInfo> mActivityInfos;

    private boolean bProcessAccessibility = false;

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

        mRefreshTargetView = new TextView(this);
        mRefreshTargetView.setOnTouchListener(this);
        mRefreshTargetView.setId(R.id.tv_refresh_target);
        // mRefreshTargetView.setOnClickListener(this);
        mRefreshTargetView.setOnKeyListener(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        WindowManager winmgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (winmgr != null) winmgr.addView(mRefreshTargetView, lp);

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

        Log.d(TAG, "user prefer key, next : " + KEY_NEXT + ", pre : " + KEY_PREVIOUS);

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        makeToast(getString(R.string.service_wait_fire_msg));

        WindowManager winmgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (winmgr != null) winmgr.removeView(mRefreshTargetView);

    }

    @Override
    public void onClick(View v) {

        checkAppAccessibility();


//        ActivityInfo currentActivity = getCurrentActivityInfo();
//
//        if (currentActivity.equals(IGNORE_ACTIVITY))
//            return;
//
//        if (isRegisteredActivity(currentActivity)) {
//            executeRefresh();
//
//        } else {
//            // makeToast(getString(R.string.service_wait_launch_msg));
//
//            // Register Activity
//            if (EDIT_MODE) {
//                mHandler.removeCallbacks(mResetCountRunnable);
//
//                mRegisterTouchCount = ++mRegisterTouchCount % REGISTER_ACTIVITY_TOUCH_COUNT;
//                Log.d(TAG, "mRegistTouchCount : " + mRegisterTouchCount + "/" + REGISTER_ACTIVITY_TOUCH_COUNT);
//
//                if (mRegisterTouchCount == 0) {
//                    makeToast(getString(R.string.service_register_activity_msg));
//                    registerActivity(currentActivity);
//                } else {
//                    makeToast(getString(R.string.service_register_activity_mode_msg_fmt, (REGISTER_ACTIVITY_TOUCH_COUNT - mRegisterTouchCount)));
//                    mHandler.postDelayed(mResetCountRunnable, USER_TOUCH_INTERVAL);
//                }
//            }
//        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        checkAppAccessibility();
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // 우선 키가 입력되면 터치가 입력된 것과 같이 리프레시 카운트를 내린다.

        ActivityInfo currentActivity = getCurrentActivityInfo();

        if (currentActivity.equals(IGNORE_ACTIVITY))
            return false;

        if (isRegisteredActivity(currentActivity)) {
            executeRefresh();
        }

        return false;
    }

    private void checkAppAccessibility() {
        boolean hasPermission = false;
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);

        // getEnabledAccessibilityServiceList는 현재 접근성 권한을 가진 리스트를 가져오게 된다
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.DEFAULT);

        for (int i = 0; i < list.size(); i++) {
            AccessibilityServiceInfo info = list.get(i);

            // 접근성 권한을 가진 앱의 패키지 네임과 패키지 네임이 같으면 현재앱이 접근성 권한을 가지고 있다고 판단함
            String srvPackageName = getBaseContext().getPackageName();
            String appPackageName = getApplication().getPackageName();
            if (info.getResolveInfo().serviceInfo.packageName.equals(getApplication().getPackageName())) {
                hasPermission = true;
            }
        }
        if (hasPermission) {
            if (bProcessAccessibility) bProcessAccessibility = false;
            Toast.makeText(this, "접근성 권한이 있습니다.", Toast.LENGTH_SHORT).show();
        } else {
            if (!bProcessAccessibility) {
                Toast.makeText(this, "접근성 권한이 없습니다.\n리프레시유 사용을 위해서는 접근성 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                bProcessAccessibility = true;
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        }

    }

    /**
     * 사용자가 설정한 리프레시 카운트에 도달한 경우 화면을 리프레시 한다.
     */
    private void executeRefresh() {
        mRefreshTouchCount = ++mRefreshTouchCount % FULL_REFRESH_COUNT;
        Log.d(TAG, "mRefreshTouchCount : " + mRefreshTouchCount + "/" + FULL_REFRESH_COUNT);

        // 설정 후 첫 실행시 표시
        // makeToast(getString(R.string.service_refreshu_launch_msg_fmt, FULL_REFRESH_COUNT));

        // ref : http://g-mini.ru/forum/topic/3827/
        if (mRefreshTouchCount == 0)
            mRefreshTargetView.invalidate(-1010, -1010, -1010, -1010);
    }

    private boolean isRegisteredActivity(ActivityInfo activityName) {
        boolean isRegistered = false;

        if (mActivityInfos != null && mActivityInfos.size() != 0) {
            for (ActivityInfo info : mActivityInfos) {
                if (info.equals(activityName))
                    isRegistered = true;
            }
        }

        return isRegistered;
    }

    private ActivityInfo getCurrentActivityInfo() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        /* Deprecated 5.0
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        */
        ActivityInfo activityInfo = null;
        ComponentName componentInfo = null;
        if (am != null) {
            List<ActivityManager.RunningAppProcessInfo> task = am.getRunningAppProcesses();

            componentInfo = task.get(0).importanceReasonComponent;

/*
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo = null;
            for (ActivityManager.RunningAppProcessInfo singleTask : task) {
                if (singleTask.importanceReasonComponent != null) {
                    runningAppProcessInfo = singleTask;
                    break;
                }
            }

            if (runningAppProcessInfo != null) {
                componentInfo = runningAppProcessInfo.importanceReasonComponent;
                Log.d(TAG, "componentInfo] className : " + componentInfo.getClassName()
                        + ", packageName : " + componentInfo.getPackageName());
            }
            */
        }
        if (componentInfo != null) {
            activityInfo = new ActivityInfo(componentInfo.getClassName(), componentInfo.getPackageName());
        } else {
            activityInfo = new ActivityInfo();
        }

        return activityInfo;
    }

    private void registerActivity(ActivityInfo activityInfo) {
        mActivityInfos.add(activityInfo);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.edit()
                .putString(getString(R.string.setting_activity_infos_key), Util.transListToJson(mActivityInfos))
                .apply();
    }

    private List<ActivityInfo> readSettingFile() {
        List<ActivityInfo> infos = null;

        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME_SETTING);

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            StringBuilder builder = new StringBuilder();
            while ((line = br.readLine()) != null) {
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

    private void writeSettingFile(String setting) {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME_SETTING);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(setting.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void makeToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }
}
