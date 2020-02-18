package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by bowenshen on 2020/2/18.
 */

public class Forecast {
    public String date;

    @SerializedName("cond")
    public More more;

    @SerializedName("tmp")
    public Tempareture tempareture;

    public class More{
        @SerializedName("txt_d")
        public String info;
    }

    public class Tempareture{
        public String max;
        public String min;
    }
}
