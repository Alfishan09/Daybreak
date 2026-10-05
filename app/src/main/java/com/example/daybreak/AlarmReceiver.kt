package com.example.daybreak

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    companion object {

        const val ACTION_SNOOZE =
            "com.example.daybreak.ACTION_SNOOZE"

        const val ACTION_DISMISS =
            "com.example.daybreak.ACTION_DISMISS"

        const val EXTRA_ALARM_ID =
            "alarm_id"

        const val EXTRA_HOUR =
            "hour"

        const val EXTRA_MINUTE =
            "minute"

        const val EXTRA_TONE_URI =
            "tone_uri"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        when (intent.action) {

            ACTION_SNOOZE -> {

                stopAlarmService(context)

                scheduleSnooze(
                    context,
                    intent
                )
            }

            ACTION_DISMISS -> {

                stopAlarmService(context)

                val manager =
                    context.getSystemService(
                        Context.NOTIFICATION_SERVICE
                    ) as NotificationManager

                manager.cancel(
                    AlarmService.NOTIFICATION_ID
                )
            }

            else -> {

                startAlarmService(
                    context,
                    intent
                )
            }
        }
    }

    private fun startAlarmService(
        context: Context,
        alarmIntent: Intent
    ) {

        val serviceIntent =
            Intent(
                context,
                AlarmService::class.java
            ).apply {

                putExtra(
                    EXTRA_ALARM_ID,
                    alarmIntent.getIntExtra(
                        EXTRA_ALARM_ID,
                        0
                    )
                )

                putExtra(
                    EXTRA_HOUR,
                    alarmIntent.getIntExtra(
                        EXTRA_HOUR,
                        0
                    )
                )

                putExtra(
                    EXTRA_MINUTE,
                    alarmIntent.getIntExtra(
                        EXTRA_MINUTE,
                        0
                    )
                )

                putExtra(
                    EXTRA_TONE_URI,
                    alarmIntent.getStringExtra(
                        EXTRA_TONE_URI
                    )
                )
            }

        ContextCompat.startForegroundService(
            context,
            serviceIntent
        )
    }

    private fun stopAlarmService(
        context: Context
    ) {

        val serviceIntent =
            Intent(
                context,
                AlarmService::class.java
            )

        context.stopService(
            serviceIntent
        )
    }

    private fun scheduleSnooze(
        context: Context,
        originalIntent: Intent
    ) {

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val snoozeTime =
            System.currentTimeMillis() +
                    (5 * 60 * 1000L)

        val snoozeIntent =
            Intent(
                context,
                AlarmReceiver::class.java
            ).apply {

                putExtra(
                    EXTRA_ALARM_ID,
                    originalIntent.getIntExtra(
                        EXTRA_ALARM_ID,
                        0
                    )
                )

                putExtra(
                    EXTRA_HOUR,
                    originalIntent.getIntExtra(
                        EXTRA_HOUR,
                        0
                    )
                )

                putExtra(
                    EXTRA_MINUTE,
                    originalIntent.getIntExtra(
                        EXTRA_MINUTE,
                        0
                    )
                )

                putExtra(
                    EXTRA_TONE_URI,
                    originalIntent.getStringExtra(
                        EXTRA_TONE_URI
                    )
                )
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                originalIntent.getIntExtra(
                    EXTRA_ALARM_ID,
                    0
                ) + 50000,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            snoozeTime,
            pendingIntent
        )
    }
}