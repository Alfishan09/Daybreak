package com.example.daybreak

import android.os.Build
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Background = Color(0xFFF7F5F0)
private val PrimaryText = Color(0xFF25232B)
private val SecondaryText = Color(0xFF706B78)
private val Accent = Color(0xFF263A8B)
private val AccentLight = Color(0xFFEDEFFB)
private val CardColor = Color.White
private val Danger = Color(0xFFB54A4A)

data class AlarmItem(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true,
    val toneUri: String = ""
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DaybreakApp()
        }
    }
    private val preferencesName = "daybreak_preferences"
    private val alarmsKey = "alarms"

    private var pendingHour = 0
    private var pendingMinute = 0

    private val ringtonePicker =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {

                val uri =
                    result.data?.getParcelableExtra<Uri>(
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI
                    )

                addAlarm(
                    pendingHour,
                    pendingMinute,
                    uri?.toString() ?: ""
                )
            }
        }

    @Composable
    private fun DaybreakApp() {

        var currentTime by remember {
            mutableStateOf(
                SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date())
            )
        }

        val alarms = remember {
            mutableStateListOf<AlarmItem>().apply {
                addAll(loadAlarms())
            }
        }

        LaunchedEffect(Unit) {

            while (true) {

                currentTime =
                    SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(Date())

                delay(1000)
            }
        }

        val currentDate =
            SimpleDateFormat(
                "EEEE, MMMM d, yyyy",
                Locale.getDefault()
            ).format(Date())

        DaybreakHomeScreen(
            currentTime = currentTime,
            currentDate = currentDate,
            alarms = alarms,

            onAddAlarm = {
                showTimePicker()
            },

            onToggleAlarm = { alarm ->

                val index =
                    alarms.indexOfFirst {
                        it.id == alarm.id
                    }

                if (index != -1) {

                    val updated =
                        alarm.copy(
                            enabled = !alarm.enabled
                        )

                    alarms[index] = updated

                    if (updated.enabled) {
                        scheduleAlarm(
                            this@MainActivity,
                            updated.id,
                            updated.hour,
                            updated.minute,
                            updated.toneUri
                        )
                    } else {
                        cancelAlarm(updated)
                    }

                    saveAlarms(alarms)
                }
            },

            onDeleteAlarm = { alarm ->

                cancelAlarm(alarm)

                alarms.removeAll {
                    it.id == alarm.id
                }

                saveAlarms(alarms)
            }
        )
    }

    private fun showTimePicker() {

        val calendar = Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hour, minute ->

                pendingHour = hour
                pendingMinute = minute

                showTonePicker()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun showTonePicker() {

        val intent =
            Intent(
                RingtoneManager.ACTION_RINGTONE_PICKER
            ).apply {

                putExtra(
                    RingtoneManager.EXTRA_RINGTONE_TYPE,
                    RingtoneManager.TYPE_ALARM
                )

                putExtra(
                    RingtoneManager.EXTRA_RINGTONE_TITLE,
                    "Choose Alarm Tone"
                )

                putExtra(
                    RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
                    false
                )

                putExtra(
                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                    RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_ALARM
                    )
                )
            }

        ringtonePicker.launch(intent)
    }

    private fun addAlarm(
        hour: Int,
        minute: Int,
        toneUri: String
    ) {

        val alarm = AlarmItem(
            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            hour = hour,
            minute = minute,
            enabled = true,
            toneUri = toneUri
        )

        val alarms = loadAlarms().toMutableList()

        alarms.add(alarm)

        saveAlarms(alarms)

        scheduleAlarm(
            this@MainActivity,
            alarm.id,
            alarm.hour,
            alarm.minute,
            alarm.toneUri
        )

        recreate()
    }

    private fun scheduleAlarm(
        context: Context,
        alarmId: Int,
        hour: Int,
        minute: Int,
        toneUri: String
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the selected time has already passed today,
            // schedule it for tomorrow.
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("tone_uri", toneUri)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            calendar.timeInMillis,
            pendingIntent
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            return
        }

        alarmManager.setAlarmClock(
            alarmClockInfo,
            pendingIntent
        )
    }
    private fun cancelAlarm(
        alarm: AlarmItem
    ) {

        val intent =
            Intent(
                this,
                AlarmReceiver::class.java
            )

        val pendingIntent =
            PendingIntent.getBroadcast(
                this,
                alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val alarmManager =
            getSystemService(Context.ALARM_SERVICE)
                    as AlarmManager

        alarmManager.cancel(pendingIntent)
    }

    private fun saveAlarms(
        alarms: List<AlarmItem>
    ) {

        val data =
            alarms.joinToString(";") {

                "${it.id},${it.hour},${it.minute},${it.enabled},${Uri.encode(it.toneUri)}"
            }

        getSharedPreferences(
            preferencesName,
            MODE_PRIVATE
        )
            .edit()
            .putString(
                alarmsKey,
                data
            )
            .apply()
    }

    private fun loadAlarms(): List<AlarmItem> {

        val data =
            getSharedPreferences(
                preferencesName,
                MODE_PRIVATE
            )
                .getString(
                    alarmsKey,
                    ""
                )
                ?: ""

        if (data.isBlank()) {
            return emptyList()
        }

        return data
            .split(";")
            .mapNotNull { item ->

                val parts =
                    item.split(",")

                if (parts.size >= 5) {

                    AlarmItem(
                        id =
                            parts[0].toIntOrNull()
                                ?: return@mapNotNull null,

                        hour =
                            parts[1].toIntOrNull()
                                ?: return@mapNotNull null,

                        minute =
                            parts[2].toIntOrNull()
                                ?: return@mapNotNull null,

                        enabled =
                            parts[3].toBoolean(),

                        toneUri =
                            Uri.decode(
                                parts.drop(4)
                                    .joinToString(",")
                            )
                    )

                } else {
                    null
                }
            }
    }
}

@Composable
private fun DaybreakHomeScreen(
    currentTime: String,
    currentDate: String,
    alarms: List<AlarmItem>,
    onAddAlarm: () -> Unit,
    onToggleAlarm: (AlarmItem) -> Unit,
    onDeleteAlarm: (AlarmItem) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 22.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier =
                    Modifier.height(48.dp)
            )

            Text(
                text = "Daybreak",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )

            Text(
                text =
                    "A better way to manage your time.",
                fontSize = 14.sp,
                color = SecondaryText
            )

            Spacer(
                modifier =
                    Modifier.height(38.dp)
            )

            Text(
                text = currentTime,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = Accent
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text = currentDate,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SecondaryText
            )

            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "Alarms",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                Text(
                    text = "${alarms.size} set",
                    fontSize = 13.sp,
                    color = SecondaryText
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            if (alarms.isEmpty()) {

                EmptyAlarmCard()

            } else {

                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),

                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = alarms,
                        key = { it.id }
                    ) { alarm ->

                        AlarmCard(
                            alarm = alarm,

                            onToggle = {
                                onToggleAlarm(alarm)
                            },

                            onDelete = {
                                onDeleteAlarm(alarm)
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

            Button(
                onClick = onAddAlarm,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                shape =
                    RoundedCornerShape(18.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Accent
                    )
            ) {

                Text(
                    text = "+  Add Alarm",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

            Text(
                text =
                    "Stay on time. Stay in control.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = SecondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(22.dp)
            )
        }
    }
}

@Composable
private fun EmptyAlarmCard() {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp),

        colors =
            CardDefaults.cardColors(
                containerColor = CardColor
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(28.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Box(
                modifier =
                    Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(AccentLight),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "⏰",
                    fontSize = 28.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            Text(
                text = "No alarms yet",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )

            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )

            Text(
                text = "Add an alarm to get started.",
                fontSize = 14.sp,
                color = SecondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: AlarmItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(22.dp),

        colors =
            CardDefaults.cardColors(
                containerColor = CardColor
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 18.dp,
                        vertical = 16.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentLight),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "⏰",
                    fontSize = 23.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.size(14.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        formatAlarmTime(
                            alarm.hour,
                            alarm.minute
                        ),

                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )

                Text(
                    text =
                        if (alarm.enabled)
                            "Alarm is on"
                        else
                            "Alarm is off",

                    fontSize = 13.sp,
                    color = SecondaryText
                )
            }

            Switch(
                checked = alarm.enabled,
                onCheckedChange = {
                    onToggle()
                }
            )

            IconButton(
                onClick = onDelete
            ) {

                Text(
                    text = "×",
                    fontSize = 24.sp,
                    color = Danger
                )
            }
        }
    }
}

private fun formatAlarmTime(
    hour: Int,
    minute: Int
): String {

    val calendar =
        Calendar.getInstance()

    calendar.set(
        Calendar.HOUR_OF_DAY,
        hour
    )

    calendar.set(
        Calendar.MINUTE,
        minute
    )

    return SimpleDateFormat(
        "hh:mm a",
        Locale.getDefault()
    ).format(calendar.time)
}