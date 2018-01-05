package com.weather.demo.model

import com.weather.demo.R
import com.weather.demo.utils.WeatherUtils

/**
 * Created by SMY on 2018/1/5.
 *
 * 实时天气类
 *
 */

class Lives {

    var province: String? = null

    var city: String? = null

    var adcode: String? = null

    var weather: String? = null

    var temperature: String? = null
        get() = field!! + "°"

    var windDirection: String? = null
        get() = field!! + R.string.wind

    var windPower: String? = null
    get() = if (WeatherUtils.weatherWind.containsKey(field)) {
        WeatherUtils.weatherWind[field]
    } else {
        "N/A"
    }

    var humidity: String? = null

    var reportTime: String? = null

}