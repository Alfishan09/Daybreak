package com.example.daybreak

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AlarmRingingActivity : ComponentActivity() {

    private var alarmId: Int = 0
    private var hour: Int = 0
    private var minute: Int = 0
    private var toneUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alarmId = intent.getIntExtra(
            AlarmReceiver.EXTRA_ALARM_ID,
            0
        )

        hour = intent.getIntExtra(
            AlarmReceiver.EXTRA_HOUR,
            0
        )

        minute = intent.getIntExtra(
            AlarmReceiver.EXTRA_MINUTE,
            0
        )

        toneUri = intent.getStringExtra(
            AlarmReceiver.EXTRA_TONE_URI
        )

        setShowWhenLocked(true)
        setTurnScreenOn(true)

        setContent {
            AlarmScreen(
                onSnooze = {
                    sendAlarmAction(
                        AlarmReceiver.ACTION_SNOOZE
                    )
                },
                onDismiss = {
                    sendAlarmAction(
                        AlarmReceiver.ACTION_DISMISS
                    )
                }
            )
        }
    }

    private fun sendAlarmAction(action: String) {

        val intent = Intent(
            this,
            AlarmReceiver::class.java
        ).apply {

            this.action = action

            putExtra(
                AlarmReceiver.EXTRA_ALARM_ID,
                alarmId
            )

            putExtra(
                AlarmReceiver.EXTRA_HOUR,
                hour
            )

            putExtra(
                AlarmReceiver.EXTRA_MINUTE,
                minute
            )

            putExtra(
                AlarmReceiver.EXTRA_TONE_URI,
                toneUri
            )
        }

        sendBroadcast(intent)
        finish()
    }
}

@Composable
private fun AlarmScreen(
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Daybreak Alarm",
            fontSize = 30.sp
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Your alarm is ringing",
            fontSize = 18.sp
        )

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        Button(
            onClick = onSnooze,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SNOOZE 5 MIN")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("DISMISS")
        }
    }
}