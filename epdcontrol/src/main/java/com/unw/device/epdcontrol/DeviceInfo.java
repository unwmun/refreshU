package com.unw.device.epdcontrol;

import android.os.Build;
import android.util.Log;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by unw on 15. 3. 31..
 */
public class DeviceInfo {

    private static final String TAG = "DeviceInfo";

    public enum Device {
        UNKNOWN,
        EINK_T62;
    }

    public final static String MANUFACTURER;
    public final static String MODEL;
    public final static String DEVICE;
    public final static String PRODUCT;

    public static Device CURRENT_DEVICE = Device.UNKNOWN;

    public static final boolean EINK_T62;

    static {
        MANUFACTURER = getBuildField("MANUFACTURER");
        MODEL = getBuildField("MODEL");
        DEVICE = getBuildField("DEVICE");
        PRODUCT = getBuildField("PRODUCT");

        HashMap<Device, Boolean> deviceMap = new HashMap<Device, Boolean>();

        Log.i(TAG, "DeviceInfo: MANUFACTURER=" + MANUFACTURER + ", MODEL=" + MODEL + ", DEVICE=" + DEVICE + ", PRODUCT=" + PRODUCT);
        // 자기들 회사 이름도 모르는지 boeye로 나옴
        EINK_T62 = (MANUFACTURER.toLowerCase().contentEquals("boeye") || MANUFACTURER.toLowerCase().contentEquals("boyue"))
                && (PRODUCT.toLowerCase().startsWith("t62") || MODEL.contentEquals("rk30sdk"))
                && DEVICE.toLowerCase().startsWith("t62");
        deviceMap.put(Device.EINK_T62, EINK_T62);

        // ~ 기타등등


        // 현재 장비 찾기
        Iterator<Device> iter = deviceMap.keySet().iterator();

        while (iter.hasNext()) {
            Device device = iter.next();
            Boolean flag = deviceMap.get(device);

            if (flag) {
                CURRENT_DEVICE = device;
            }
        }

        Log.i(TAG, "Current device is : " + CURRENT_DEVICE.name());
    }

    private static String getBuildField(String fieldName) {

        try {
            return (String)Build.class.getField(fieldName).get(null);
        } catch (Exception e) {
            Log.d(TAG, "Exception while trying to check Build." + fieldName);
            return "";
        }
    }
}
