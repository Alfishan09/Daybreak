package com.example.daybreak
import android.app.ActivityOptions
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

class AlarmService : Service() {

    companion object {

        const val CHANNEL_ID = "DAYBREAK_ALARM_V3"

        const val NOTIFICATION_ID = 1001

        const val EXTRA_TONE_URI = "tone_uri"
    }

    private var mediaPlayer: MediaPlayer? = null

    private var alarmId: Int = 0
    private var alarmHour: Int = 0
    private var alarmMinute: Int = 0
    private var toneUri: String? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        alarmId = intent?.getIntExtra(
            AlarmReceiver.EXTRA_ALARM_ID,
            0
        ) ?: 0

        alarmHour = intent?.getIntExtra(
            AlarmReceiver.EXTRA_HOUR,
            0
        ) ?: 0

        alarmMinute = intent?.getIntExtra(
            AlarmReceiver.EXTRA_MINUTE,
            0
        ) ?: 0

        toneUri = intent?.getStringExtra(
            AlarmReceiver.EXTRA_TONE_URI
        )

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            android.content.pm.ServiceInfo
                .FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )

        startAlarmSound(toneUri)

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daybreak Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {

                description =
                    "Notifications for Daybreak alarms"

                enableVibration(true)
            }

            val manager =
                getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    private fun startAlarmSound(
        toneString: String?
    ) {

        stopAlarmSound()

        val defaultAlarmUri =
            RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_ALARM
            )

        val alarmUri: Uri =
            if (!toneString.isNullOrEmpty()) {

                try {
                    Uri.parse(toneString)
                } catch (_: Exception) {
                    defaultAlarmUri
                }

            } else {
                defaultAlarmUri
            }

        mediaPlayer = try {
            MediaPlayer.create(
                this,
                alarmUri
            )
        } catch (_: Exception) {
            null
        }

        if (mediaPlayer == null) {
            mediaPlayer =
                MediaPlayer.create(
                    this,
                    defaultAlarmUri
                )
        }

        mediaPlayer?.apply {
            isLooping = true
            start()
        }
    }

    private fun stopAlarmSound() {

        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }

        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun buildNotification(): Notification {

        val fullScreenIntent =
            Intent(
                this,
                AlarmRingingActivity::class.java
            ).apply {

                putExtra(
                    AlarmReceiver.EXTRA_ALARM_ID,
                    alarmId
                )

                putExtra(
                    AlarmReceiver.EXTRA_HOUR,
                    alarmHour
                )

                putExtra(
                    AlarmReceiver.EXTRA_MINUTE,
                    alarmMinute
                )

                putExtra(
                    AlarmReceiver.EXTRA_TONE_URI,
                    toneUri
                )
            }

        val fullScreenOptions = ActivityOptions.makeBasic().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setPendingIntentCreatorBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                )
            }
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId + 30000,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            fullScreenOptions.toBundle()
        )
        val snoozeIntent =
            Intent(
                this,
                AlarmReceiver::class.java
            ).apply {

                action =
                    AlarmReceiver.ACTION_SNOOZE

                putExtra(
                    AlarmReceiver.EXTRA_ALARM_ID,
                    alarmId
                )

                putExtra(
                    AlarmReceiver.EXTRA_HOUR,
                    alarmHour
                )

                putExtra(
                    AlarmReceiver.EXTRA_MINUTE,
                    alarmMinute
                )

                putExtra(
                    AlarmReceiver.EXTRA_TONE_URI,
                    toneUri
                )
            }

        val snoozePendingIntent =
            PendingIntent.getBroadcast(
                this,
                alarmId + 40000,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val dismissIntent =
            Intent(
                this,
                AlarmReceiver::class.java
            ).apply {

                action =
                    AlarmReceiver.ACTION_DISMISS

                putExtra(
                    AlarmReceiver.EXTRA_ALARM_ID,
                    alarmId
                )

                putExtra(
                    AlarmReceiver.EXTRA_HOUR,
                    alarmHour
                )

                putExtra(
                    AlarmReceiver.EXTRA_MINUTE,
                    alarmMinute
                )

                putExtra(
                    AlarmReceiver.EXTRA_TONE_URI,
                    toneUri
                )
            }

        val dismissPendingIntent =
            PendingIntent.getBroadcast(
                this,
                alarmId + 50000,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        return NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setSmallIcon(
                android.R.drawable.ic_lock_idle_alarm
            )
            .setContentTitle(
                "Daybreak Alarm"
            )
            .setContentText(
                "Your alarm is ringing."
            )
            .setPriority(
                NotificationCompat.PRIORITY_MAX
            )
            .setCategory(
                NotificationCompat.CATEGORY_ALARM
            )
            .setVisibility(
                NotificationCompat.VISIBILITY_PUBLIC
            )
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(false)
            .setForegroundServiceBehavior(
                NotificationCompat
                    .FOREGROUND_SERVICE_IMMEDIATE
            )
            .setFullScreenIntent(
                fullScreenPendingIntent,
                true
            )
            .addAction(
                android.R.drawable.ic_media_pause,
                "SNOOZE",
                snoozePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "DISMISS",
                dismissPendingIntent
            )
            .build()
    }

    override fun onDestroy() {

        stopAlarmSound()

        val manager =
            getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        manager.cancel(NOTIFICATION_ID)

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null
}