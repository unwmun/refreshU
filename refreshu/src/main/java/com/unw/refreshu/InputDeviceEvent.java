package com.unw.refreshu;

import android.util.Log;

import net.pocketmagic.android.eventinjector.Events;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by unw on 15. 4. 12..
 */
public class InputDeviceEvent {

    private static final String TAG = "KeyInputEvent";

    public static final String KEY_PAGE_NEXT = "KEY_PAGE_NEXT";
    public static final String KEY_PAGE_PREVIOUS = "KEY_PAGE_PREVIOUS";
    public static final String KEY_BACK = "KEY_BACK";

    private static final int INPUT_TYPE_KEYPAD = 1;
    private static final int INPUT_TYPE_TOUCH = 3;

    private List<KeyInputEventListener> mKeyInputEventListeners;

    private Events mEvent = new Events();

    private boolean bEventMonitor = false;

    private static InputDeviceEvent sInstance = new InputDeviceEvent();

    public InputDeviceEvent()
    {
        mKeyInputEventListeners = new ArrayList<KeyInputEventListener>(10);
        //openInputEvents();
    }

    public static InputDeviceEvent getInstance()
    {
        return sInstance;
    }

    public void openInputEvents() {
        Events.intEnableDebug(1);
        mEvent.Init();

        for (Events.InputDevice inputDevice : mEvent.m_Devs) {
            StringBuilder builder = new StringBuilder("[InputDevice] ")
                    .append("Name : ").append(inputDevice.getName())
                    .append(", Id : ").append(inputDevice.getId())
                    .append(", Path : ").append(inputDevice.getPath())
                    .append(", Open? : ").append(inputDevice.getOpen());

            Log.d(TAG, "[before open] " + builder.toString());

            inputDevice.Open(true);

            builder = new StringBuilder("[InputDevice] ")
                    .append("Name : ").append(inputDevice.getName())
                    .append(", Id : ").append(inputDevice.getId())
                    .append(", Path : ").append(inputDevice.getPath())
                    .append(", Open? : ").append(inputDevice.getOpen());
            Log.d(TAG, "[after open] " + builder.toString());

            /*
            [InputDevice] Name : rk29-keypad, Id : 0, Path : /dev/input/event1, Open? : true
            [InputDevice] Name : cyttsp4_mt, Id : 1, Path : /dev/input/event2, Open? : true << Touch
            [InputDevice] Name : axp22-supplyer, Id : 2, Path : /dev/input/event0, Open? : true
            [InputDevice] Name : rk_headsetdet, Id : 3, Path : /dev/input/event3, Open? : true
             */
        }
    }

    public void closeInputEvents() {
        for (Events.InputDevice inputDevice : mEvent.m_Devs) {
            inputDevice.Close();
        }
        mEvent.Release();
    }

    public void startEventMonitor()
    {
        bEventMonitor = true;

        Thread b = new Thread(new Runnable() {
            public void run() {
                while (bEventMonitor) {
                    for (Events.InputDevice idev:mEvent.m_Devs) {
                        if (idev.getOpen() && (0 == idev.getPollingEvent())) {
                            final String line = idev.getName()+
                                    ":" + idev.getSuccessfulPollingType()+
                                    " " + idev.getSuccessfulPollingCode() +
                                    " " + idev.getSuccessfulPollingValue();
                            Log.d(TAG, "Event:"+line);

                            if (idev.getName().contains("keypad")
                                    && mKeyInputEventListeners != null) {

                                int pollingType = idev.getSuccessfulPollingType();
                                int keyCode = idev.getSuccessfulPollingCode();

                                if (idev.getSuccessfulPollingValue() == 1) {
                                    for (KeyInputEventListener listener : mKeyInputEventListeners) {
                                        listener.onKeyDown(pollingType, keyCode);
                                    }
                                } else if (idev.getSuccessfulPollingValue() == 0) {
                                    for (KeyInputEventListener listener : mKeyInputEventListeners) {
                                        listener.onKeyUp(pollingType, keyCode);
                                    }
                                }
                            }
                        }

                    }// for
                }// while
            }// run
        });// Thread
        b.setDaemon(true);
        b.start();
    }

    public boolean isRun()
    {
        return bEventMonitor;
    }

    public void stopEventMonitor()
    {
        bEventMonitor = false;
    }

    public Events.InputDevice getInputDevice(String name) {
        Events.InputDevice rtn = null;
        for (Events.InputDevice inputDevice : mEvent.m_Devs) {
            if (inputDevice.getName().contains(name)) {
                rtn = inputDevice;
            }
        }

        return rtn;
    }

    public void setKeyInputEventListener(KeyInputEventListener keyInputEventListener) {
        mKeyInputEventListeners.add(keyInputEventListener);
    }

    public interface KeyInputEventListener
    {
        boolean onKeyDown(int inputType, int keyCode);
        boolean onKeyUp(int inputType, int keyCode);
    }
}
