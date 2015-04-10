package com.unw.refreshu;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

/**
 * Created by unw on 15. 4. 8..
 */
public class SettingFragment extends PreferenceFragment {
    private String TAG;

    private SwitchPreference mServiceRunPref;
    private EditTextPreference mRefreshCountPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = getClass().getSimpleName();

        addPreferencesFromResource(R.xml.setting_main);

        //-----
        // 터치 대기 서비스 스위치
        mServiceRunPref = (SwitchPreference) findPreference(getString(R.string.setting_refresh_service_run_key));
        mServiceRunPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                boolean on = (boolean) newValue;

                Intent intent = new Intent(getActivity(), WaitUserService.class);
                if (on) {
                    getActivity().startService(intent);
                } else {
                    getActivity().stopService(intent);
                }

                return true;
            }

        });

        //-----
        // 리프레시 주기
        mRefreshCountPref = (EditTextPreference) findPreference(getString(R.string.setting_refresh_count_key));
        mRefreshCountPref.setSummary(mRefreshCountPref.getText());
        mRefreshCountPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mServiceRunPref.setChecked(false);
                return true;
            }
        });
        mRefreshCountPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                String value = (String)newValue;
                preference.setSummary(value);
                preference.setDefaultValue(value);

                return true;
            }
        });
    }



}