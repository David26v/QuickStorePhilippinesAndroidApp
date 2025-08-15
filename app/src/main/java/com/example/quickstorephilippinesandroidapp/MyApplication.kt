package com.example.quickstorephilippinesandroidapp

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
        lateinit var prefs: SharedPreferences
        const val PREF_NAME = "quickstore_prefs"
        const val DEVICE_REGISTERED_KEY = "device_registered"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}
