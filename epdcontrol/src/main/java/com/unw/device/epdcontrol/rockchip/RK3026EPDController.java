package com.unw.device.epdcontrol.rockchip;

/**
 * Created by unw on 15. 3. 25..
 */
public interface RK3026EPDController
{
    enum EINK_MODE
    {
        EPD_AUTO,
        EPD_FULL,
        EPD_A2,
        EPD_PART,
        EPD_FULL_DITHER,

        EPD_RESET,
        EPD_BLACK_WHITE,
        EPD_TEXT,
        EPD_BLOCK,
        EPD_FULL_WIN,

        EPD_OED_PART,
        EPD_DIRECT_PART,
        EPD_DIRECT_A2,
        EPD_STANDBY,
        EPD_POWEROFF
    }

    String EPD_NULL = "EPD_NULL";

    String EPD_AUTO = "EPD_AUTO";
    String EPD_FULL = "EPD_FULL";
    String EPD_A2 = "EPD_A2";
    String EPD_PART = "EPD_PART";
    String EPD_FULL_DITHER = "EPD_FULL_DITHER";

    String EPD_RESET = "EPD_RESET";
    String EPD_BLACK_WHITE = "EPD_BLACK_WHITE";
    String EPD_TEXT = "EPD_TEXT";
    String EPD_BLOCK = "EPD_BLOCK";
    String EPD_FULL_WIN = "EPD_FULL_WIN";

    String EPD_OED_PART = "EPD_OED_PART";
    String EPD_DIRECT_PART = "EPD_DIRECT_PART";
    String EPD_DIRECT_A2 = "EPD_DIRECT_A2";
    String EPD_STANDBY = "EPD_STANDBY";
    String EPD_POWEROFF = "EPD_POWEROFF";

}
