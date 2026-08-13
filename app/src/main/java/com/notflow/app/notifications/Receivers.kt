package com.notflow.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.notflow.app.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        val title = intent.getStringExtra("title") ?: "Reminder"
        val description = intent.getStringExtra("description") ?: "You have a scheduled item on Note Flow"

        NotificationHelper.showNotification(
            context = context,
            notificationId = if (reminderId != -1L) reminderId.toInt() else System.currentTimeMillis().toInt(),
            title = title,
            message = description
        )

        if (reminderId != -1L) {
            val database = AppDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                database.reminderDao().markReminderTriggered(reminderId)
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val database = AppDatabase.getDatabase(context)
            val alarmScheduler = AlarmScheduler(context)

            CoroutineScope(Dispatchers.IO).launch {
                val activeReminders = database.reminderDao().getUpcomingRemindersList(System.currentTimeMillis())
                activeReminders.forEach { reminder ->
                    alarmScheduler.scheduleAlarm(
                        reminderId = reminder.id,
                        title = reminder.title,
                        description = reminder.description,
                        triggerAtMillis = reminder.scheduledTime
                    )
                }
            }
        }
    }
}
