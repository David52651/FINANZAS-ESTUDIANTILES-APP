package com.arcjas.studentapp.core.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.util.Date

data class Reminder(
    val id: Long? = null,
    val userId: Long,
    val title: String,
    val description: String?,
    val reminderDate: Date,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
) {
    companion object {
        private val gson: Gson = GsonBuilder().create()

        fun fromJson(json: String): Reminder? {
            return try {
                gson.fromJson(json, Reminder::class.java)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                null
            }
        }

        fun toJson(reminder: Reminder): String {
            return gson.toJson(reminder)
        }

        fun listFromJson(json: String): List<Reminder>? {
            return try {
                val listType = object : TypeToken<List<Reminder>>() {}.type
                gson.fromJson(json, listType)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                null
            }
        }

        fun listToJson(reminders: List<Reminder>): String {
            val listType = object : TypeToken<List<Reminder>>() {}.type
            return gson.toJson(reminders, listType)
        }
    }
}
