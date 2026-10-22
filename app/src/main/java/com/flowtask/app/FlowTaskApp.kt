package com.flowtask.app

import android.app.Application
import com.flowtask.app.reminder.TaskReminderScheduler

class FlowTaskApp : Application() {

    override fun onCreate() {
        super.onCreate()
        TaskReminderScheduler.initNotificationChannel(this)
    }
}
