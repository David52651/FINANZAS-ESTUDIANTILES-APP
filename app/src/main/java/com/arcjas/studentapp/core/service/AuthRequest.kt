package com.arcjas.studentapp.core.service


import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arcjas.studentapp.core.config.VolleyListener
import com.arcjas.studentapp.core.model.ResponseCore
import com.arcjas.studentapp.core.model.User
import org.json.JSONObject

class AuthRequest(private val context: Context) {
    private val queue: RequestQueue = Volley.newRequestQueue(context)
    private val apiManager = ApiManager.getInstance(context)

    fun loginAuth(
        username: String,
        password: String,
        listener: VolleyListener<User>
    ) {
        val url = "${apiManager.getBaseUrl()}/api/login"

        val jsonBody = JSONObject()
        jsonBody.put("code", username)
        jsonBody.put("password", password)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                handleResponse(response, listener)
            },
            { error ->
                listener.onError(error.toString())
            }
        )

        queue.add(jsonObjectRequest)
    }

    fun registerUser(
        username: String,
        password: String,
        email: String,
        code: String,
        name: String,
        listener: VolleyListener<User>
    ) {
        val url = "${apiManager.getBaseUrl()}/api/register"
        println("Request URL: $url")

        val jsonBody = JSONObject()
        jsonBody.put("username", username)
        jsonBody.put("password", password)
        jsonBody.put("email", email)
        jsonBody.put("code", code)
        jsonBody.put("name", name)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                println("Response: $response")
                handleResponse(response, listener)
            },
            { error ->
                val statusCode = error.networkResponse?.statusCode ?: -1
                val errorMessage = error.message ?: "Unknown error"
                println("Error: $statusCode $errorMessage")
                listener.onError("$statusCode - $errorMessage")
                error.networkResponse?.data?.let { errorData ->
                    val errorResponse = String(errorData)
                    println("Error Response: $errorResponse")
                }
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun handleResponse(response: JSONObject, listener: VolleyListener<User>) {
        println(response);
        val success = response.optBoolean("success", false)
        val message = response.optString("message", "")
        val dataJson = response.optJSONObject("data")
        if (success && dataJson != null) {
            val dataObject = User.fromJson(dataJson.toString())
            val responseCore = ResponseCore(success, message, dataObject)
            listener.onSuccess(responseCore)
        } else {
            listener.onError(message)
        }
    }


}