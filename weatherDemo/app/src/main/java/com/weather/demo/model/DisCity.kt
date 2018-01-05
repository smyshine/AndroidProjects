package com.weather.demo.model

/**
 * Created by SMY on 2018/1/5.
 */
class DisCity (var adcode: String ?= null,
               var name: String ?= null,
               var center: String ?= null,
               var level: String ?= null,
               var districts: List<DisCity> ?= null)