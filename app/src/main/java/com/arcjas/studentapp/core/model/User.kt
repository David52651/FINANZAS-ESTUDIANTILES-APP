package com.arcjas.studentapp.core.model

import java.util.*

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
data class User(
    val id: Int? = null,
    val code: String? = null,
    val username: String? = null,
    val name: String? = null,
    val email: String? = null,
    val emailVerifiedAt: Date? = null,
    val token: String? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
) {
    companion object {
        private val gson: Gson = GsonBuilder().create()
        fun fromJson(json: String): User? {
            return try {
                gson.fromJson(json, User::class.java)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                null
            }
        }
        fun toJson(user: User): String {
            return gson.toJson(user)
        }
        fun listFromJson(json: String): List<User>? {
            return try {
                val listType = object : TypeToken<List<User>>() {}.type
                gson.fromJson(json, listType)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                null
            }
        }
        fun listToJson(users: List<User>): String {
            val listType = object : TypeToken<List<User>>() {}.type
            return gson.toJson(users, listType)
        }
    }
}