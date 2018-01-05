package com.weather.demo.utils;

import com.weather.demo.R;

import java.util.HashMap;

/**
 * Created by SMY on 2018/1/5.
 */

public class WeatherUtils {
    public static HashMap<String,Integer> WeatherKV = new HashMap<>();
    public static HashMap<String,Integer> weatherView = new HashMap<>();
    public static HashMap<String,String> weatherName = new HashMap<>();
    public static HashMap<String,String> weatherWind = new HashMap<>();
    public static HashMap<String,String> weatherDirection = new HashMap<>();

    static {
        WeatherKV.put("晴",R.drawable.l128);
        WeatherKV.put("多云",R.drawable.l129);
        WeatherKV.put("阴",R.drawable.l130);
        WeatherKV.put("阵雨",R.drawable.l135);
        WeatherKV.put("雷阵雨",R.drawable.l138);
        WeatherKV.put("雷阵雨并伴有冰雹",R.drawable.l138);
        WeatherKV.put("雨夹雪",R.drawable.l128);
        WeatherKV.put("小雨",R.drawable.l135);
        WeatherKV.put("中雨", R.drawable.l133);
        WeatherKV.put("大雨",R.drawable.l135);
        WeatherKV.put("暴雨",R.drawable.l138);
        WeatherKV.put("大暴雨",R.drawable.l138);
        WeatherKV.put("特大暴雨",R.drawable.l138);
        WeatherKV.put("阵雪",R.drawable.l137);
        WeatherKV.put("小雪",R.drawable.l136);
        WeatherKV.put("中雪",R.drawable.l137);
        WeatherKV.put("大雪",R.drawable.l139);
        WeatherKV.put("暴雪",R.drawable.l139);
        WeatherKV.put("雾",R.drawable.l141);
        WeatherKV.put("冻雨",R.drawable.l136);
        WeatherKV.put("沙尘",R.drawable.l129);
        WeatherKV.put("小雨",R.drawable.l133);
        WeatherKV.put("中雨",R.drawable.l135);
        WeatherKV.put("大雨-暴雨",R.drawable.l138);
        WeatherKV.put("暴雨-大暴雨",R.drawable.l138);
        WeatherKV.put("大暴雨-特大暴雨",R.drawable.l138);
        WeatherKV.put("小雪-中雪",R.drawable.l136);
        WeatherKV.put("中雪-大雪",R.drawable.l137);
        WeatherKV.put("大雪-暴雪",R.drawable.l139);
        WeatherKV.put("浮尘",R.drawable.l129);
        WeatherKV.put("扬沙",R.drawable.l129);
        WeatherKV.put("强沙尘暴",R.drawable.l130);
        WeatherKV.put("飑",R.drawable.l129);
        WeatherKV.put("龙卷风",R.drawable.l138);
        WeatherKV.put("弱高吹雪",R.drawable.l139);
        WeatherKV.put("轻雾",R.drawable.l134);
        WeatherKV.put("霾",R.drawable.l132);

        weatherName.put("00","晴");
        weatherName.put("01","多云");
        weatherName.put("02","阴");
        weatherName.put("03","阵雨");
        weatherName.put("04","雷阵雨");
        weatherName.put("05","雷阵雨并伴有冰雹");
        weatherName.put("06","雨夹雪");
        weatherName.put("07","小雨");
        weatherName.put("08","中雨");
        weatherName.put("09","大雨");
        weatherName.put("10","暴雨");
        weatherName.put("11","大暴雨");
        weatherName.put("12","特大暴雨");
        weatherName.put("13","阵雪");
        weatherName.put("14","小雪");
        weatherName.put("15","中雪");
        weatherName.put("16","大雪");
        weatherName.put("17","暴雪");
        weatherName.put("18","雾");
        weatherName.put("19","冻雨");
        weatherName.put("20","沙尘暴");
        weatherName.put("21","小雨-中雨");
        weatherName.put("22","中雨-大雨");
        weatherName.put("23","大雨-暴雨");
        weatherName.put("24","暴雨-大暴雨");
        weatherName.put("25","大暴雨-特大暴雨");
        weatherName.put("26","小雪-中雪");
        weatherName.put("27","中雪-大雪");
        weatherName.put("28","大雪-暴雪");
        weatherName.put("29","浮尘");
        weatherName.put("30","扬沙");
        weatherName.put("31","强沙尘暴");
        weatherName.put("32","飑");
        weatherName.put("33","龙卷风");
        weatherName.put("34","弱高吹雪");
        weatherName.put("35","轻雾");
        weatherName.put("53","霾");

        weatherView.put("00", R.drawable.l128);
        weatherView.put("01",R.drawable.l129);
        weatherView.put("02",R.drawable.l130);
        weatherView.put("03",R.drawable.l135);
        weatherView.put("04",R.drawable.l138);
        weatherView.put("05",R.drawable.l138);
        weatherView.put("06",R.drawable.l128);
        weatherView.put("07",R.drawable.l135);
        weatherView.put("08",R.drawable.l133);
        weatherView.put("09",R.drawable.l135);
        weatherView.put("10",R.drawable.l138);
        weatherView.put("11",R.drawable.l138);
        weatherView.put("12",R.drawable.l138);
        weatherView.put("13",R.drawable.l137);
        weatherView.put("14",R.drawable.l136);
        weatherView.put("15",R.drawable.l137);
        weatherView.put("16",R.drawable.l139);
        weatherView.put("17",R.drawable.l139);
        weatherView.put("18",R.drawable.l141);
        weatherView.put("19",R.drawable.l136);
        weatherView.put("20",R.drawable.l129);
        weatherView.put("21",R.drawable.l133);
        weatherView.put("22",R.drawable.l135);
        weatherView.put("23",R.drawable.l138);
        weatherView.put("24",R.drawable.l138);
        weatherView.put("25",R.drawable.l138);
        weatherView.put("26",R.drawable.l136);
        weatherView.put("27",R.drawable.l137);
        weatherView.put("28",R.drawable.l139);
        weatherView.put("29",R.drawable.l129);
        weatherView.put("30",R.drawable.l129);
        weatherView.put("31",R.drawable.l130);
        weatherView.put("32",R.drawable.l129);
        weatherView.put("33",R.drawable.l138);
        weatherView.put("34",R.drawable.l139);
        weatherView.put("35",R.drawable.l134);
        weatherView.put("53",R.drawable.l132);

        weatherWind.put("0","≤3级");
        weatherWind.put("1","4级");
        weatherWind.put("2","5级");
        weatherWind.put("3","6级");
        weatherWind.put("4","7级");
        weatherWind.put("5","8级");
        weatherWind.put("6","9级");
        weatherWind.put("7","10级");
        weatherWind.put("8","11级");
        weatherWind.put("9","12级");

        weatherDirection.put("0","无风");
        weatherDirection.put("1","东北风");
        weatherDirection.put("2","东风");
        weatherDirection.put("3","东南风");
        weatherDirection.put("4","南风");
        weatherDirection.put("5","西南风");
        weatherDirection.put("6","西风");
        weatherDirection.put("7","西北风");
        weatherDirection.put("8","北风");
        weatherDirection.put("9","N/A");
    }
}
