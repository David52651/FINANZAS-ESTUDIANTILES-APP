package com.arcjas.studentapp.ui.reminder

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.arcjas.studentapp.R
import com.arcjas.studentapp.core.config.ReminderBroadcastReceiver
import java.util.*

class ReminderFragment : Fragment() {

    private var selectedDate: Calendar? = null
    private var selectedTime: Calendar? = null

    private val CHANNEL_ID = "reminder_channel"
    private val CHANNEL_NAME = "Reminder Channel"
    private val NOTIFICATION_ID = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_reminder, container, false)

        // Define views
        val etTitle: EditText = root.findViewById(R.id.et_title)
        val etDescription: EditText = root.findViewById(R.id.et_description)
        val btnSelectDate: Button = root.findViewById(R.id.btn_select_date)
        val btnSelectTime: Button = root.findViewById(R.id.btn_select_time)
        val btnSaveReminder: Button = root.findViewById(R.id.btn_save_reminder)

        // Set click listeners
        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                btnSelectDate.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            }, year, month, day).show()
        }

        btnSelectTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }
                btnSelectTime.text = "$selectedHour:$selectedMinute"
            }, hour, minute, true).show()
        }

        btnSaveReminder.setOnClickListener {
            val title = etTitle.text.toString()
            val description = etDescription.text.toString()

            if (title.isBlank() || description.isBlank() || selectedDate == null || selectedTime == null) {
                Toast.makeText(
                    requireContext(),
                    "Por favor completa todos los campos y selecciona fecha y hora",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            createNotificationChannel()

            scheduleNotification(title, description)

            Toast.makeText(
                requireContext(),
                "Recordatorio programado para ${selectedDate?.time}",
                Toast.LENGTH_SHORT
            ).show()
        }

        return root
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_NAME
            val descriptionText = "Channel for reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleNotification(title: String, description: String) {
        val notificationIntent = Intent(requireContext(), ReminderBroadcastReceiver::class.java)
        notificationIntent.putExtra("title", title)
        notificationIntent.putExtra("description", description)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            NOTIFICATION_ID,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            set(
                selectedDate!!.get(Calendar.YEAR),
                selectedDate!!.get(Calendar.MONTH),
                selectedDate!!.get(Calendar.DAY_OF_MONTH),
                selectedTime!!.get(Calendar.HOUR_OF_DAY),
                selectedTime!!.get(Calendar.MINUTE),
                0
            )
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            Toast.makeText(requireContext(), "Selecciona una fecha y hora futura", Toast.LENGTH_SHORT).show()
            return
        }

        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

}
