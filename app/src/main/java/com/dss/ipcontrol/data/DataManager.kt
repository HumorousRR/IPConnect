package com.dss.ipcontrol.data

import android.util.Log


object DataManager {
    private val dataList: ArrayList<String> = ArrayList()

    fun getDataList(): ArrayList<String> {
        val list = ArrayList(dataList)
        return list
    }

    fun clearData() {
        dataList.clear()
    }

    fun addData(data: String) {
        dataList.add(data)
    }
}