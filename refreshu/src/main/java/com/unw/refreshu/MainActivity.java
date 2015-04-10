package com.unw.refreshu;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by unw on 15. 4. 8..
 */
public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
    }
}
