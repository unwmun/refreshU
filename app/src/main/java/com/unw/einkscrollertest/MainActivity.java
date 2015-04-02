package com.unw.einkscrollertest;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.unw.device.epdcontrol.DeviceInfo;
import com.unw.device.epdcontrol.rockchip.T62EPDController;
import com.unw.webkit.EPDWebView;
import com.unw.webkit.EPDWebViewClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class MainActivity extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener{

    private static final String TAG = "MainActivity";

    private boolean bRun;

    private Runnable mA2ModeRunnable = new Runnable() {
        @Override
        public void run() {
            T62EPDController.requestEpdMode(mA2ModeButton, T62EPDController.EPD_A2);

            if (bRun)
                mHandler.postDelayed(this, 500L);
            else
                T62EPDController.requestEpdMode(mA2ModeButton, T62EPDController.EPD_FULL_DITHER);
        }
    };

    private Handler mHandler = new Handler();

    private EPDWebView mWebView;
    private ToggleButton mPartModeButton;
    private ToggleButton mAutoModeButton;
    private ToggleButton mA2ModeButton;

    private static final String URL = "http://navercast.naver.com";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (EPDWebView) findViewById(R.id.wv_epd);
        mPartModeButton = (ToggleButton) findViewById(R.id.btn_part_mode);
        mAutoModeButton = (ToggleButton) findViewById(R.id.btn_auto_mode);
        mA2ModeButton = (ToggleButton) findViewById(R.id.btn_a2_mode);

        mPartModeButton.setOnCheckedChangeListener(this);
        mAutoModeButton.setOnCheckedChangeListener(this);
        mA2ModeButton.setOnCheckedChangeListener(this);

        mWebView.setWebViewClient(new EPDWebViewClient());

        WebSettings webSettings = mWebView.getSettings();
        //int fontSize = getResources().getDimensionPixelSize(R.dimen.font_size_xl);
        //webSettings.setDefaultFontSize(fontSize);
        webSettings.setJavaScriptEnabled(true);

        mWebView.loadUrl(URL);

        checkDeviceInfomation();

    }

    @Override
    public void onCheckedChanged(CompoundButton v, boolean on) {

        String title = "";

        if (on) {
            switch (v.getId()) {
                case R.id.btn_part_mode:
                    mAutoModeButton.setEnabled(false);
                    mA2ModeButton.setEnabled(false);

                    mWebView.setEpdMode(T62EPDController.EPD_PART);
                    title = T62EPDController.EPD_PART;
                    break;

                case R.id.btn_auto_mode:
                    mPartModeButton.setEnabled(false);
                    mA2ModeButton.setEnabled(false);

                    mWebView.setEpdMode(T62EPDController.EPD_AUTO);
                    title = T62EPDController.EPD_AUTO;
                    break;

                case R.id.btn_a2_mode:
                    mPartModeButton.setEnabled(false);
                    mAutoModeButton.setEnabled(false);

                    bRun = true;
                    mHandler.post(mA2ModeRunnable);
                    title = T62EPDController.EPD_A2;
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

                    mWebView.setEpdMode(T62EPDController.EPD_FULL_DITHER);
                    title = T62EPDController.EPD_FULL_DITHER;
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
}
