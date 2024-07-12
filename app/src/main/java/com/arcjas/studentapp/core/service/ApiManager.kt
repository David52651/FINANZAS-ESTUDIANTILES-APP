package com.arcjas.studentapp.core.service

import android.content.Context
import android.content.SharedPreferences
import com.arcjas.studentapp.core.model.User
import com.google.gson.Gson


class ApiManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    private val baseUrl = "https://qrb8cju7rlao.share.zrok.io"
    private var token: String? = null
    private var currentUser: User? = null
    private val gson = Gson()
    init {
        token = sharedPreferences.getString("token", null)
    }
    companion object {
        @Volatile
        private var instance: ApiManager? = null

        fun getInstance(context: Context): ApiManager {
            return instance ?: synchronized(this) {
                instance ?: ApiManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getToken(): String? {
        return token
    }

    fun getBaseUrl(): String {
        return baseUrl
    }

    fun saveToken(token: String) {
        this.token = token
        sharedPreferences.edit().putString("token", token).apply()
    }
    fun clearToken() {
        this.token = null
        sharedPreferences.edit().remove("token").apply()
    }
    fun saveCurrentUser(user: User) {
        this.currentUser = user
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString("user", userJson).apply()
    }
    fun getCurrentUser(): User? {
        return currentUser
    }
    fun clearCurrentUser() {
        this.currentUser = null
        sharedPreferences.edit().remove("user").apply()
    }
    fun updateToken(newToken: String) {
        saveToken(newToken)
    }
}