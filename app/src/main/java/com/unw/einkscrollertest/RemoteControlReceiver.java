package com.unw.einkscrollertest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by unw on 15. 4. 7..
 */
public class RemoteControlReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("", "keycccoddd : hihihi");

        abortBroadcast();
    }
}
