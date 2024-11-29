package com.app.householdtracing.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun stringToArrayList(value: String): List<String> {
        val listPlayers = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listPlayers)
    }

    @TypeConverter
    fun arrayListToString(list: List<String>) = Gson().toJson(list)



}