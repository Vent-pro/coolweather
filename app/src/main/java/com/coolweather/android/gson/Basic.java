package com.coolweather.android.gson;

import com.coolweather.android.util.HttpUtil;
import com.google.gson.annotations.SerializedName;

import okhttp3.OkHttpClient;

/**
 * Created by bowenshen on 2020/2/18.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}