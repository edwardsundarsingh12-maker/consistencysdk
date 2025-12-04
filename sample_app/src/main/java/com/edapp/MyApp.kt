package com.edapp

import android.app.Application
import androidx.activity.viewModels
import com.edapp.habittracker.ui.HabitViewModel
import com.edapp.habittracker.util.ConsistencySDK
import com.edapp.habittracker.util.SDK

import dagger.hilt.android.HiltAndroidApp
import kotlin.getValue

@HiltAndroidApp
class MyApp: Application() {


    override fun onCreate() {
        super.onCreate()
        consistencySDK = ConsistencySDK.Builder()
            .setCanShowAllMonth(false)
            .setEnableLineChart(true)
            .setEnableAddNewHabit(true)
            .setEnableRowEditOption(true)
            .setAppContext(this)
            .build()
        appContext = this
    }
    companion object{
        private var appContext: MyApp? = null
        private var consistencySDK: ConsistencySDK? = null
        fun getAppContext():MyApp = appContext!!

        fun getConsistencySDK() : ConsistencySDK = consistencySDK!!
    }

}