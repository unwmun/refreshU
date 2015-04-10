package com.unw.einkscrollertest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.unw.device.epdcontrol.DeviceInfo;
import com.unw.device.epdcontrol.rockchip.RK30xxEPDController;
import com.unw.device.epdcontrol.rockchip.T62EPDController;
import com.unw.webkit.EPDWebView;
import com.unw.webkit.EPDWebViewClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class MainActivity extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MainActivity";

    private boolean bRun;

    private Runnable mA2ModeRunnable = new Runnable() {
        @Override
        public void run() {
            RK30xxEPDController.requestEpdMode(mA2ModeButton, RK30xxEPDController.EPD_A2);

            if (bRun)
                mHandler.postDelayed(this, 500L);
            else
                RK30xxEPDController.requestEpdMode(mA2ModeButton, RK30xxEPDController.EPD_FULL_DITHER);
        }
    };

    private Handler mHandler = new Handler();

    private EPDWebView mWebView;
    private ToggleButton mPartModeButton;
    private ToggleButton mAutoModeButton;
    private ToggleButton mA2ModeButton;
    private ToggleButton mRefreshButton;

    private static final String URL = "http://navercast.naver.com";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (EPDWebView) findViewById(R.id.wv_epd);
        mPartModeButton = (ToggleButton) findViewById(R.id.btn_part_mode);
        mAutoModeButton = (ToggleButton) findViewById(R.id.btn_auto_mode);
        mA2ModeButton = (ToggleButton) findViewById(R.id.btn_a2_mode);
        mRefreshButton = (ToggleButton) findViewById(R.id.btn_refresh_service);

        mPartModeButton.setOnCheckedChangeListener(this);
        mAutoModeButton.setOnCheckedChangeListener(this);
        mA2ModeButton.setOnCheckedChangeListener(this);
        mRefreshButton.setOnCheckedChangeListener(this);

        mWebView.setWebViewClient(new EPDWebViewClient());

        WebSettings webSettings = mWebView.getSettings();
        //int fontSize = getResources().getDimensionPixelSize(R.dimen.font_size_xl);
        //webSettings.setDefaultFontSize(fontSize);
        webSettings.setJavaScriptEnabled(true);

        mWebView.loadUrl(URL);

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), RemoteControlReceiver.class.getName()));

        //checkDeviceInfomation();

    }

    @Override
    public void onCheckedChanged(CompoundButton v, boolean on) {

        String title = "";

        if (on) {
            switch (v.getId()) {
                case R.id.btn_part_mode:
                    mAutoModeButton.setEnabled(false);
                    mA2ModeButton.setEnabled(false);

                    mWebView.setEpdMode(RK30xxEPDController.EPD_PART);
                    title = RK30xxEPDController.EPD_PART;
                    break;

                case R.id.btn_auto_mode:
                    mPartModeButton.setEnabled(false);
                    mA2ModeButton.setEnabled(false);

                    mWebView.setEpdMode(RK30xxEPDController.EPD_AUTO);
                    title = RK30xxEPDController.EPD_AUTO;
                    break;

                case R.id.btn_a2_mode:
                    mPartModeButton.setEnabled(false);
                    mAutoModeButton.setEnabled(false);

                    bRun = true;
                    mHandler.post(mA2ModeRunnable);
                    title = RK30xxEPDController.EPD_A2;
                    break;

                case R.id.btn_refresh_service:
                    startService(new Intent("com.unw.einkscrollertest.startService"));
                    Log.w(TAG, "hi");
                    break;

                default:
                    break;
            }
        } else {
            switch (v.getId()) {
                case R.id.btn_a2_mode:
                    bRun = false;

                case R.id.btn_part_mode:
                case R.id.btn_auto_mode:
                    mPartModeButton.setEnabled(true);
                    mAutoModeButton.setEnabled(true);
                    mA2ModeButton.setEnabled(true);

                    mWebView.setEpdMode(RK30xxEPDController.EPD_FULL_DITHER);
                    title = RK30xxEPDController.EPD_FULL_DITHER;
                    break;

                default:
                    break;
            }
        }

        getSupportActionBar().setTitle(title);
    }

    private void checkDeviceInfomation()
    {
        Class[] viewInnerClass = View.class.getDeclaredClasses();
        Method[] viewMtd = View.class.getDeclaredMethods();
        Field[] viewField = View.class.getDeclaredFields();

        StringBuilder builder = new StringBuilder();

        //device info
        builder.append("\n==========<Device Info>==========\n");
        builder.append(DeviceInfo.getDeviceInfo());

        //class
        builder.append("\n==========<Class>==========\n");
        for (int i = 0; i < viewInnerClass.length; i++) {
            Class cls = viewInnerClass[i];
            builder.append("[" + i + "] ")
                    .append(cls.toString())
                    .append("\n");
        }
        //method
        builder.append("\n==========<Method>==========\n");
        for (int i = 0; i < viewMtd.length; i++) {
            Method mtd = viewMtd[i];
            builder.append("[" + i + "] ")
                    .append(mtd.toString())
                    .append("\n");

            Annotation[] mthAnnos = mtd.getAnnotations();
            if (mthAnnos.length > 0) {
                builder.append("\n\t\t\t==========<Annotation>==========\n");
                for (int j = 0; j < mthAnnos.length; j++) {
                    Annotation anno = mthAnnos[j];
                    builder.append("\t\t\t[" + j + "] ")
                            .append(anno.toString())
                            .append("\n");
                }
            }
        }
        //method
        builder.append("\n==========<Field>==========\n");
        for (int i = 0; i < viewField.length; i++) {
            Field field = viewField[i];
            builder.append("[" + i + "] ")
                    .append(field.toString())
                    .append("\n");
        }

        print(DeviceInfo.DEVICE, builder.toString());

        Log.i(TAG, builder.toString());
    }

    private void print(String title, String msg)
    {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), title + "_info.txt");

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(msg.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static RemoteControlReceiver mHomeKeyReceiver = null;

    private void registerHomeKeyReceiver(Context context) {
        Log.i("", "registerHomeKeyReceiver");
        AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);
        manager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), RemoteControlReceiver.class.getName()));
    }

    private static void unregisterHomeKeyReceiver(Context context) {
        Log.i("", "unregisterHomeKeyReceiver");
        if (null != mHomeKeyReceiver) {
            context.unregisterReceiver(mHomeKeyReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerHomeKeyReceiver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterHomeKeyReceiver(this);
    }
}
