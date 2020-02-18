package com.coolweather.android.gson;

/**
 * Created by bowenshen on 2020/2/18.
 */

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
