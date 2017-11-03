package com.unw.refreshu;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

/**
 * Created by unw on 15. 4. 8..
 */
public class SettingFragment extends PreferenceFragment {

    private String TAG;

    private static final String KEY_VOLUME_BUTTONS = "persist.sys.keyChange";

    private SwitchPreference mServiceRunPref;
    private SwitchPreference mRegisterModePref;
    private CheckBoxPreference mPreventDoubleTouchPref;
    private EditTextPreference mRefreshCountPref;
    private SwitchPreference mSystemVolumePref;

    private boolean bServiceRun = false;

    private Preference.OnPreferenceClickListener mOffServiceListener
            = new Preference.OnPreferenceClickListener() {
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public boolean onPreferenceClick(Preference preference) {
            // 서비스 종료되도록
            mServiceRunPref.setChecked(false);

            return true;
        }
    };

    private Preference.OnPreferenceChangeListener mRestartServiceListener
            = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            if (bServiceRun) { // 돌고 있지 않는 경우는 갱신할 필요 없음
                Intent intent = new Intent(getActivity(), RefreshService4NR.class);
                getActivity().startService(intent);
            }
            return true;
        }
    };

    private Preference.OnPreferenceChangeListener mSummaryListener
            = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String value = (String)newValue;
            preference.setSummary(value);
            preference.setDefaultValue(value);

            if (bServiceRun) { // 돌고 있지 않는 경우는 갱신할 필요 없음
                Intent intent = new Intent(getActivity(), RefreshService4NR.class);
                getActivity().startService(intent);
            }

            return true;
        }
    };

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
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean on = (Boolean) newValue;

                Intent intent = new Intent(getActivity(), RefreshService4NR.class);
                if (on) {
                    getActivity().startService(intent);
                    bServiceRun = true;
                } else {
                    getActivity().stopService(intent);
                    bServiceRun = false;
                }

                return true;
            }
        });

        //-----
        // 액티비티 등록 모드
        mRegisterModePref = (SwitchPreference) findPreference(getString(R.string.setting_edit_mode_key));
        mRegisterModePref.setOnPreferenceChangeListener(mRestartServiceListener);

        //-----
        // 이중 터치 막기
        mPreventDoubleTouchPref = (CheckBoxPreference) findPreference(getString(R.string.setting_prevent_double_touch_key));
        mPreventDoubleTouchPref.setOnPreferenceChangeListener(mRestartServiceListener);

        //-----
        // 시스템 볼륨키 on/off 설정
        mSystemVolumePref = (SwitchPreference) findPreference(getString(R.string.setting_system_volumn_mode_key));
        int volumeSettingOn = SystemProperties.getInt(KEY_VOLUME_BUTTONS, 0);
        mSystemVolumePref.setChecked(volumeSettingOn == 0 ? false : true);
        mSystemVolumePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean on = (Boolean) newValue;
                SystemProperties.set(KEY_VOLUME_BUTTONS, on ? "1" : "0");
                return true;
            }
        });

        //-----
        // 리프레시 주기
        mRefreshCountPref = (EditTextPreference) findPreference(getString(R.string.setting_refresh_count_key));
        mRefreshCountPref.setSummary(mRefreshCountPref.getText());
        mRefreshCountPref.setOnPreferenceChangeListener(mSummaryListener);

        //-----
        // 사용자 키설정
        findPreference(getString(R.string.setting_keymap_next_page_key))
                .setOnPreferenceClickListener(mOffServiceListener);
        findPreference(getString(R.string.setting_keymap_pre_page_key))
                .setOnPreferenceClickListener(mOffServiceListener);

    }
}