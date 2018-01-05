package com.weather.demo.model

/**
 * Created by SMY on 2018/1/5.
 */

class Weather (var status: String ? = null,
               var count: String? = null,
               var info: String? = null,
               var infoCode: String? = null,
               var lives: List<Lives>? = null)