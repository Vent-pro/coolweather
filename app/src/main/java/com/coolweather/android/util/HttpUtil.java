package com.coolweather.android.util;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by bowenshen on 2020/2/16.
 */

public class HttpUtil{
    public static void sendOkHttpRequest(String address,HttpCallback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
    public interface HttpCallback extends Callback{
        @Override
        void onFailure(Call call, IOException e);

        @Override
        void onResponse(Call call, Response response) throws IOException;
    }
}
