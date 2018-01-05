package com.weather.demo.utils

import android.util.Log
import com.google.gson.Gson
import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import java.io.IOException

/**
 * Created by SMY on 2018/1/5.
 */

object WeatherClient {

    val WEATHER_TYPE_BASE = "base"
    val WEATHER_TYPE_ALL = "all"

    private val jsonConver = Gson()
    private val okHttpClient = OkHttpClient()

    val SERVER_HOST = "http://restapi.amap.com/v3/weather/weatherInfo?"

    var APP_KEY = "4eb2611665938e6c19d90efb630da820"

    fun <T> query(adcode: String?, type: String, tClass: Class<T>, callback: IHttpCallback) {
        var params = (
                  "key=" + APP_KEY
                + "&city=" + adcode
                + "&extensions=" + type
                + "&output=JSON"
                )

        var request = Request.Builder().url(SERVER_HOST + params).get().build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(request: Request, e: IOException) {
                callback.onSuccess<Any>("", false)
            }

            @Throws(IOException::class)
            override fun onResponse(response: Response) {
                var jsonData = response.body().string()
                Log.d("HttpClient", "onResponse: " + jsonData)
                var result = jsonConver.fromJson(jsonData, tClass)
                callback.onSuccess(result, true)
            }
        })

    }

}