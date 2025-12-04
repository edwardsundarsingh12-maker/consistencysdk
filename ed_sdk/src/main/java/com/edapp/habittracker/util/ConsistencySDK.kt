package com.edapp.habittracker.util

import android.app.Application


class ConsistencySDK private constructor(
    val enableAddNewHabit: Boolean,
    val enableRowEditOption: Boolean,
    val canShowAllMonth: Boolean,
    val enableLineChart: Boolean,
    val enableReminderNotification: Boolean,
) {

    class Builder {
        private var enableAddNewHabit: Boolean = true
        private var enableRowEditOption: Boolean = false
        private var canShowAllMonth: Boolean = false
        private var enableLineChart: Boolean = false

        private var enableReminderNotification: Boolean = false

        private var context: Application? = null

        fun setEnableAddNewHabit(value: Boolean) = apply { this.enableAddNewHabit = value }
        fun setEnableRowEditOption(value: Boolean) = apply { this.enableRowEditOption = value }
        fun setCanShowAllMonth(value: Boolean) = apply { this.canShowAllMonth = value }
        fun setEnableLineChart(value: Boolean) = apply { this.enableLineChart = value }
        fun setEnableReminderNotification(value: Boolean) = apply { this.enableReminderNotification = value }
        fun setAppContext(context: Application) = apply {
            this.context = context
        }

        fun build(): ConsistencySDK {
            if (context == null) {
                throw IllegalStateException(
                    "ConsistencySDK requires a valid Application context. " +
                            "Please initialize using ConsistencySDK.setAppContext(context) before using the SDK."
                )
            }
            SDK.setAppContext(context!!)
            val sdk = ConsistencySDK(
                enableAddNewHabit = enableAddNewHabit,
                enableRowEditOption = enableRowEditOption,
                canShowAllMonth = canShowAllMonth,
                enableLineChart = enableLineChart,
                enableReminderNotification = enableReminderNotification
            )
            SDK.init(sdk)
            return sdk
        }
    }

}