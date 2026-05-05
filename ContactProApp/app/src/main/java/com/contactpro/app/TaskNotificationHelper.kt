package com.contactpro.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Locale

object TaskNotificationHelper {

    private const val CHANNEL_ID = "hamsaa_task_reminders"
    private const val CHANNEL_NAME = "Task Reminders"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for upcoming task reminders"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /**
     * Schedules a notification for the task at 9:00 AM on the due date.
     * @param taskId unique task ID (used as notification ID)
     * @param title task title
     * @param dueDateStr date string in "yyyy-MM-dd" format
     */
    fun scheduleNotification(context: Context, taskId: Long, title: String, dueDateStr: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dueDate = try { sdf.parse(dueDateStr) } catch (e: Exception) { return } ?: return

        // Set notification time to 9:00 AM on the due date
        val calendar = java.util.Calendar.getInstance().apply {
            time = dueDate
            set(java.util.Calendar.HOUR_OF_DAY, 9)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }

        // Don't schedule if the time is already past
        if (calendar.timeInMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("taskId", taskId.toInt())
            putExtra("taskTitle", title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, taskId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelNotification(context: Context, taskId: Long) {
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, taskId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("taskId", 0)
        val title = intent.getStringExtra("taskTitle") ?: "Task Reminder"

        TaskNotificationHelper.createChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, taskId, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "hamsaa_task_reminders")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Hamsaa Task Due Today")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(taskId, notification)
    }
}
