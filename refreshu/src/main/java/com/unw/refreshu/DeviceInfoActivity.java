package com.unw.refreshu;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.pocketmagic.android.eventinjector.Events;

/**
 * Created by unw on 15. 4. 10..
 */
public class DeviceInfoActivity extends Activity
{
    private static final String TAG = "DeviceInfoActivity";

    private TextView mDeviceInfoLabel;
    private Button mPasteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_info);

        mDeviceInfoLabel = (TextView) findViewById(R.id.tv_device_info_info);

        mPasteButton = (Button) findViewById(R.id.btn_device_info_copy);
        mPasteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("device_info", mDeviceInfoLabel.getText());
                clipboardManager.setPrimaryClip(clipData);

                Toast.makeText(getBaseContext(), R.string.setting_device_info_copy_msg, Toast.LENGTH_SHORT).show();
            }
        });

        String str = getDeviceInformation() + "\n\n" + getInputDeviceInformaiton();
        mDeviceInfoLabel.setText(str);
    }

    private String getDeviceInformation(){
        StringBuilder builder = new StringBuilder()
                .append("=====================\n")
                .append("Device Information\n")
                .append("=====================\n")
                .append("MANUFACTURER : ").append(getBuildField("MANUFACTURER"))
                .append(", MODEL : ").append(getBuildField("MODEL"))
                .append(", DEVICE : ").append(getBuildField("DEVICE"))
                .append(", PRODUCT : ").append(getBuildField("PRODUCT"));

        return builder.toString();
    }

    private static String getBuildField(String fieldName) {

        try {
            return (String)Build.class.getField(fieldName).get(null);
        } catch (Exception e) {
            Log.d(TAG, "Exception while trying to check Build." + fieldName);
            return "";
        }
    }

    private String getInputDeviceInformaiton()
    {
        Events events = new Events();
        Events.intEnableDebug(1);
        events.Init();

        StringBuilder builder = new StringBuilder()
                .append("=====================\n")
                .append("Input Device Information\n")
                .append("=====================\n");

        for (Events.InputDevice inputDevice : events.m_Devs) {

            inputDevice.Open(true);

                builder.append("[InputDevice] ")
                        .append("Name : ").append(inputDevice.getName())
                        .append(", Id : ").append(inputDevice.getId())
                        .append(", Path : ").append(inputDevice.getPath())
                        .append(", Open : ").append(inputDevice.getOpen())
                        .append("\n");

        }

        return builder.toString();
    }
}
