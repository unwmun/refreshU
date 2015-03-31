package com.unw.device.epdcontrol;

import android.view.View;

import com.unw.device.epdcontrol.rockchip.T62EPDController;

/**
 * Created by unw on 15. 3. 31..
 */
public class EPDFactory {

    public static EPDController getEPDController()
    {
        EPDController epdController = null;

        switch (DeviceInfo.CURRENT_DEVICE) {
            case EINK_T62 :
                epdController = new T62EPDController();
                break;

            case UNKNOWN :
                epdController = new EPDController() {
                    @Override
                    public void setEpdMode(View targetView, String epdMode) {
                        // 아무것도 안해줌
                    }
                };
                break;

            default : break;
        }

        return epdController;
    }

}
