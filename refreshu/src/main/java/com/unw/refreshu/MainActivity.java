package com.unw.refreshu;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;

/**
 * Created by unw on 15. 4. 8..
 */
public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.d("MainActivity", "Screen Width : " + metrics.widthPixels  + ", Height : " + metrics.heightPixels);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
    }

}
