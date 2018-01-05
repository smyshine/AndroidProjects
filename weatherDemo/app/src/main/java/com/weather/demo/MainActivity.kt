package com.weather.demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.TextureView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.gson.Gson
import com.weather.demo.model.DisCity
import com.weather.demo.model.DistrictsRoot
import com.weather.demo.model.Lives
import com.weather.demo.model.Weather
import com.weather.demo.utils.IHttpCallback
import com.weather.demo.utils.WeatherClient
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    var cities : MutableList<Int> ?= null
    private var cityKeyVal: MutableList<String>? = null
    private var cityVal: MutableList<String>? = null

    private var selectName: String? = null
    private var selectCode: String? = null

    val jsonConverter = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView.movementMethod = ScrollingMovementMethod()

        initCities()

//        queryData("110101")

        findCity()

        val autoTextString = ArrayAdapter(this, android.R.layout.simple_list_item_1, cityKeyVal!!)
        city_textview.setAdapter(autoTextString)
        city_textview.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val obj = parent.getItemAtPosition(position)
            val index = cityKeyVal!!.indexOf(obj)
            selectCode = cityVal!![index]
            selectName = obj.toString()
        }

        done_btn!!.setOnClickListener {
            if (!TextUtils.isEmpty(selectCode) && !TextUtils.isEmpty(selectName)) {
                queryData(selectCode)
            } else {
                log("please choose city")
            }
        }
    }

    private fun initCities() {
        cities = ArrayList()
        cities!!.add(R.raw.anhui)
        cities!!.add(R.raw.aomeng)
        cities!!.add(R.raw.beijin)
        cities!!.add(R.raw.chongqing)
        cities!!.add(R.raw.fujiang)
        cities!!.add(R.raw.gangsu)
        cities!!.add(R.raw.guangdong)
        cities!!.add(R.raw.guangxi)
        cities!!.add(R.raw.guizhou)
        cities!!.add(R.raw.hainang)
        cities!!.add(R.raw.hebei)
        cities!!.add(R.raw.heilongjiang)
        cities!!.add(R.raw.henang)
        cities!!.add(R.raw.hongkong)
        cities!!.add(R.raw.hubei)
        cities!!.add(R.raw.hunang)
        cities!!.add(R.raw.jiangsu)
        cities!!.add(R.raw.jiangxi)
        cities!!.add(R.raw.jiling)
        cities!!.add(R.raw.liaoning)
        cities!!.add(R.raw.neimenggu)
        cities!!.add(R.raw.ningxia)
        cities!!.add(R.raw.qinghai)
        cities!!.add(R.raw.shangdong)
        cities!!.add(R.raw.shanghai)
        cities!!.add(R.raw.shangxi)
        cities!!.add(R.raw.shanxi)
        cities!!.add(R.raw.sichuang)
        cities!!.add(R.raw.tianjin)
        cities!!.add(R.raw.xinjiang)
        cities!!.add(R.raw.xizan)
        cities!!.add(R.raw.yunnang)
        cities!!.add(R.raw.zhejiang)

        cityKeyVal = java.util.ArrayList()
        cityVal = java.util.ArrayList()
    }

    private fun findCity() {
        for (i in cities!!) {
            val stringBuilder = StringBuilder()
            val inputStream = resources.openRawResource(i)
            val reader = BufferedReader(InputStreamReader(inputStream))

            try {
                var line = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }

                val dis = jsonConverter.fromJson<DistrictsRoot>(stringBuilder.toString(), DistrictsRoot::class.java!!)

                if (dis.districts.size > 0) {
                    val _dis = dis.districts
                    if (_dis.size > 0) {
                        val currentDis = _dis[0]
                        var disCity = DisCity()
                        disCity.adcode = currentDis.adcode
                        disCity.name = currentDis.name
                        disCity.districts = currentDis.districts

                        whileCity(currentDis.districts, disCity)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun whileCity(districts: List<DisCity>, parentCity: DisCity) {
        for (c in districts) {
            if (c.districts!!.isNotEmpty()) {
                whileCity(c.districts!!, c)
            } else {
                cityKeyVal!!.add(parentCity.name + " " + c.name)
                cityVal!!.add(c.adcode!!)
            }
        }
    }


    private fun queryData(code: String ?= "110101") {
        log("start query for data")

        WeatherClient.query(code, WeatherClient.WEATHER_TYPE_BASE, Weather::class.java, object : IHttpCallback {

            override fun <T> onSuccess(result: T, success: Boolean) {
                if (success) {
                    val weather = result as Weather
                    if (weather.info == "OK" && weather.count == "1") {
                        var info = weather.lives?.get(0)
                        log("get Weather success")
                        log("province : " + info?.province)
                        log("city : " + info?.city)
                        log("temperature : " + info?.temperature)
//                        log("time : " + info?.reportTime)
//                        log("wind direction : " + info?.windDirection)
                        log("wind power : " + info?.windPower)
                        log("humidity : " + info?.humidity)
                        log("")
                    } else {
                        log("Fail to get weather")
                    }
                } else {
                    log("Fail to get infos")
                }
            }

        })
    }

    fun log(message: String) {
        Log.d("MainActivity", message)
        runOnUiThread { textView.append("\n" + message) }
    }
}
