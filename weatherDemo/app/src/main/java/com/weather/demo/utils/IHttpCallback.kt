package com.weather.demo.utils

/**
 * Created by SMY on 2018/1/5.
 */

interface IHttpCallback {
    fun <T> onSuccess(result: T, success: Boolean)
}