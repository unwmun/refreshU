package com.unw.refreshu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by unw on 15. 4. 10..
 */
public class Util {

    /**
     * https://github.com/ztoday21/refreshPie/blob/master/src/com/ztoday21/refreshPie/Setting.java
     * 
     * @param json
     * @return
     */
    public static ArrayList<ActivityInfo> transActivityInfoJsonToList(String json)
    {
        ArrayList<ActivityInfo> list = new ArrayList<>();
        Gson gson = new GsonBuilder().create();
        ActivityInfo[] arr = gson.fromJson(json, ActivityInfo[].class);

        if (arr != null)
            list = new ArrayList<ActivityInfo>(Arrays.asList(arr));

        return list;
    }

    public static <T extends List> String transListToJson(T list)
    {
        return new Gson().toJson(list);
    }



}
