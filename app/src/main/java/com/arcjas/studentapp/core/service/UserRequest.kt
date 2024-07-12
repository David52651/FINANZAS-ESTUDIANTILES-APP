package com.arcjas.studentapp.core.service


import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arcjas.studentapp.core.model.ResponseCore
import com.arcjas.studentapp.core.model.User

class UserRequest(private val context: Context, private val apiManager: ApiManager) {

    private val queue: RequestQueue = Volley.newRequestQueue(context)

    interface UserInfoListener {
        fun onUserInfoSuccess(response: ResponseCore<User>)
        fun onUserInfoError(error: String)
    }

    fun getUserInfo(listener: UserInfoListener) {
        val url = "${apiManager.getBaseUrl()}/api/userinfo"

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener { response ->
                val success = response.optBoolean("success", false)
                val message = response.optString("message", "")
                val dataJson = response.optJSONObject("data")

                if (success && dataJson != null) {
                    val user = User.fromJson(dataJson.toString())
                    val responseCore = ResponseCore(success, message, user)
                    listener.onUserInfoSuccess(responseCore)
                } else {
                    listener.onUserInfoError("Error retrieving user info")
                }
            },
            Response.ErrorListener { error ->
                listener.onUserInfoError(error.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                apiManager.getToken()?.let {
                    headers["Authorization"] = "Bearer $it"
                }
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }
}