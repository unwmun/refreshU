package com.unw.device.epdcontrol.rockchip;

import android.util.Log;
import android.view.View;

import com.unw.device.epdcontrol.EPDController;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by unw on 15. 3. 26..
 */
public class T62EPDController implements RK3026EPDController, EPDController
{
    private static Class<Enum> eInkEnum;
    private static Method requestEpdModeMethod1;
    private static Method requestEpdModeMethod2;

    private static Field isInA2;


    static {
        try {
            eInkEnum = (Class<Enum>) Class.forName("android.view.View$EINK_MODE");

            requestEpdModeMethod1 = View.class.getMethod("requestEpdMode", eInkEnum);

            requestEpdModeMethod2 = View.class.getMethod("requestEpdMode", eInkEnum, boolean.class);

            isInA2 = View.class.getDeclaredField("mIsInA2");
            isInA2.setAccessible(true);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static boolean isInA2(View view)
    {
        try {
            boolean value = (Boolean)isInA2.get(view);
            Log.d("TAGGG", "isInA2 : " + value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean requestEpdMode(View view, String mode) {

        try {
            requestEpdModeMethod1.invoke(view, stringToEnum(mode));
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean requestEpdMode(View view, String mode, boolean flag) {
        try {
            requestEpdModeMethod2.invoke(view, stringToEnum(mode), flag);
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Object stringToEnum(String str)
    {
        return Enum.valueOf(eInkEnum, str);
    }

    @Override
    public void setEpdMode(View targetView, String epdMode) {
        requestEpdMode(targetView, epdMode);
    }
}
