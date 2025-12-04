package com.edapp.habittracker.util

import android.app.Application

object SDK {

    private lateinit var appContext : Application

    fun setAppContext(contexts: Application) {
        appContext = contexts
    }

    fun getAppContext() : Application = appContext

    lateinit var config: ConsistencySDK
        private set

    fun init(sdk: ConsistencySDK) {
        this.config = sdk
    }

}