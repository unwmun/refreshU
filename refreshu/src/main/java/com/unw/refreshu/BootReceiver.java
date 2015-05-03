package com.unw.refreshu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by unw on 15. 4. 10..
 */
public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean isRunOnBoot = pref.getBoolean(context.getString(R.string.setting_service_run_on_boot_key), false);

        if ( intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ) {
            if (isRunOnBoot) {
                Intent i = new Intent(context, RefreshService.class);
                context.startService(i);
            }
            pref.edit().putBoolean(context.getString(R.string.setting_refresh_service_run_key), isRunOnBoot).commit();
        }
    }
}
