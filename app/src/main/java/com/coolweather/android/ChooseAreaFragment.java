package com.coolweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.*;
import com.coolweather.android.util.*;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by bowenshen on 2020/2/16.
 */

public class ChooseAreaFragment extends Fragment{
    public static final int PROVINCE=0;

    public static final int CITY=1;

    public static final int COUNTY=2;

    public static final String INDEX="http://guolin.tech/api/china";

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList=new ArrayList<>();

    private List<Province> provinceList;

    private List<City> cityList;

    private List<County> countyList;

    private Province selectedProvince;

    private City selectedCity;

    private int currentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView)view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        listView=(ListView) view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentLevel==PROVINCE){
                    selectedProvince=provinceList.get(i);
                    queryCities();
                }
                else if(currentLevel==CITY){
                    selectedCity=cityList.get(i);
                    queryCounties();
                }
                else if(currentLevel==COUNTY){
                    String weatherId=countyList.get(i).getWeatherId();
                    if(getActivity() instanceof MainActivity){
                        Intent intent=new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity weatherActivity=(WeatherActivity) getActivity();
                        weatherActivity.drawerLayout.closeDrawers();
                        weatherActivity.swipeRefresh.setRefreshing(true);
                        weatherActivity.requestWeather(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(currentLevel==CITY){
                    queryProvinces();
                }
                else if(currentLevel==COUNTY){
                    queryCities();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList=DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for(Province p:provinceList){
                dataList.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=PROVINCE;
        }
        else{
            String address=INDEX;
            queryFromServer(address,PROVINCE);
        }
    }

    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City c:cityList){
                dataList.add(c.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=CITY;
        }
        else{
            String address=INDEX+"/"+selectedProvince.getProvinceCode();
            queryFromServer(address,CITY);
        }
    }

    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County c:countyList){
                dataList.add(c.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=COUNTY;
        }
        else{
            String address=INDEX+"/"+selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode();
            queryFromServer(address,COUNTY);
        }
    }

    private void queryFromServer(String address, final int queryType){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new HttpUtil.HttpCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result;
                switch (queryType){
                    case PROVINCE:
                        result=Utility.handleProvinceResponse(responseText);
                        break;
                    case CITY:
                        result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                        break;
                    case COUNTY:
                        result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                        break;
                    default:
                        result=false;
                        break;
                }
                if(result){
                   getActivity().runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           closeProgressDialog();
                           switch (queryType){
                               case PROVINCE:
                                   queryProvinces();
                                   break;
                               case CITY:
                                   queryCities();
                                   break;
                               case COUNTY:
                                   queryCounties();
                                   break;
                               default:
                                   break;
                           }
                       }
                   });
                }
                else{
                    Toast.makeText(getContext(),"加载失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setTitle("加载界面");
            progressDialog.setMessage("正在加载......");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
