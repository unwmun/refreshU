package com.unw.refreshu;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.pocketmagic.android.eventinjector.Events;

/**
 * Created by unw on 15. 4. 10..
 * http://android.hlidskialf.com/blog/code/android-seekbar-preference
 */
public class KeyInputListPreference extends DialogPreference implements InputDeviceEvent.KeyInputEventListener
{
    private static final String TAG = "KeyInputListPreference";

    private static final String androidns="http://schemas.android.com/apk/res/android";

    private int mInputKeyCode;
    private int mDefault;

    private InputDeviceEvent mInputDeviceEvent;

    private TextView mKeyInputLabel;

    public KeyInputListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {

        String dialogTitle =  getContext().getString(R.string.setting_key_input_title_fmt, getTitle());
        setDialogTitle(dialogTitle);

        setSummary(mInputKeyCode + "");

        return super.onCreateView(parent);
    }

    @Override
    protected View onCreateDialogView() {

        LinearLayout dialogLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.dialog_key_input, null);
        mKeyInputLabel = (TextView) dialogLayout.findViewById(R.id.tv_key_input);

        if (shouldPersist())
            mInputKeyCode = getPersistedInt(mDefault);

        mKeyInputLabel.setText(mInputKeyCode + "");

        // Input Events
        mInputDeviceEvent = new InputDeviceEvent();
        mInputDeviceEvent.openInputEvents();
        mInputDeviceEvent.setKeyInputEventListener(this);
        mInputDeviceEvent.startEventMonitor();

        return dialogLayout;
    }

    public int getKeyCode()
    {
        return mInputKeyCode;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mKeyInputLabel.setText(mInputKeyCode + "");
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);

        if (restore)
            mInputKeyCode = shouldPersist() ? getPersistedInt(mDefault) : 0;
        else
            mInputKeyCode = (Integer)defaultValue;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        setSummary(mInputKeyCode + "");

        if (mInputDeviceEvent != null) {
            if (mInputDeviceEvent.isRun())
                mInputDeviceEvent.stopEventMonitor();

            mInputDeviceEvent.closeInputEvents();
        }
    }

    @Override
    public boolean onKeyDown(int inputType, int keyCode) {

        mInputKeyCode = keyCode;

        mKeyInputLabel.post(new Runnable() {
            @Override
            public void run() {
                if (shouldPersist())
                    persistInt(mInputKeyCode);

                callChangeListener(mInputKeyCode);
                mKeyInputLabel.setText(mInputKeyCode + "");
                setSummary(mInputKeyCode + "");
            }
        });

        return false;
    }

    @Override
    public boolean onKeyUp(int inputType, int keyCode) {
        return false;
    }
}
