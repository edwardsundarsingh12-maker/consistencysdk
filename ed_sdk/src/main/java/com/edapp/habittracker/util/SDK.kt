package com.edapp.habittracker.util

import android.app.Application
import com.edapp.habittracker.data.HabitRepository
import com.edapp.habittracker.domain.UpdateHabit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object SDK {

    private lateinit var appContext : Application

    fun setAppContext(contexts: Application) {
        appContext = contexts
    }

    fun getAppContext() : Application = appContext

    lateinit var config: ConsistencySDK
        private set

    private lateinit var habitRepository: HabitRepository

    fun initRepository(repo: HabitRepository) {
        habitRepository = repo
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun insertOrUpdate(updateHabitList: List<UpdateHabit>) {
        if (!::habitRepository.isInitialized) {
            throw IllegalStateException("SDK not initialized. Call ConsistencySDK.init() first.")
        }

        scope.launch {
            updateHabitList.forEach { updateHabit ->
                habitRepository.insertOrUpdateHabit(updateHabit)
            }
        }
    }


    fun init(sdk: ConsistencySDK) {
        this.config = sdk
    }

}