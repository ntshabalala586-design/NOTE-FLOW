package com.notflow.app

import android.app.Application
import com.notflow.app.data.repository.AppRepository
import com.notflow.app.notifications.NotificationHelper

class NotFlowApplication : Application() {
    lateinit var repository: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = AppRepository(this)
        NotificationHelper.createNotificationChannel(this)
    }
}
