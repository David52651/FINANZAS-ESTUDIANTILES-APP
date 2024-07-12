package com.arcjas.studentapp.core.service

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arcjas.studentapp.core.config.VolleyListener
import com.arcjas.studentapp.core.model.Account
import com.arcjas.studentapp.core.model.ResponseCore
import com.arcjas.studentapp.core.model.Saving
import org.json.JSONException
import org.json.JSONObject

class SavingRequest(private val context: Context, private val apiManager: ApiManager) {
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    fun updateSaving(saving: Saving, listener: VolleyListener<Saving>) {
        val url = "${apiManager.getBaseUrl()}/api/saving"

        val jsonBody = JSONObject()
        jsonBody.put("source_account_id", saving.sourceAccountId)
        jsonBody.put("destination_account_id", saving.destinationAccountId)
        jsonBody.put("amount", saving.amount)
        jsonBody.put("saving_goal", saving.savingGoal)
        jsonBody.put("saving_interval", saving.savingInterval)
        jsonBody.put("description", saving.description)
        println(jsonBody)
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, jsonBody,
            Response.Listener { response ->
                val responseAccount = Saving.fromJson(response.getJSONObject("data").toString())
                val responseCore = ResponseCore(
                    response.getBoolean("success"),
                    response.getString("message"),
                    responseAccount
                )
                listener.onSuccess(responseCore)
            },
            Response.ErrorListener { error ->
                val errorMessage = "$error"
                listener.onError(errorMessage)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                apiManager.getToken()?.let {
                    headers["Authorization"] = "Bearer $it"
                }
                return headers
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    fun updateStatusSaving(status: Boolean, listener: VolleyListener<Saving>) {
        val url = "${apiManager.getBaseUrl()}/api/saving/updateStatusSaving"
        val jsonBody = JSONObject()
        jsonBody.put("status", status)
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, jsonBody,
            Response.Listener { response ->
                val responseAccount = Saving.fromJson(response.getJSONObject("data").toString())
                val responseCore = ResponseCore(
                    response.getBoolean("success"),
                    response.getString("message"),
                    responseAccount
                )
                listener.onSuccess(responseCore)
            },
            Response.ErrorListener { error ->
                val errorMessage = "$error"
                listener.onError(errorMessage)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                apiManager.getToken()?.let {
                    headers["Authorization"] = "Bearer $it"
                }
                return headers
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    fun loadSaving(listener: VolleyListener<Saving>) {
        val url = "${apiManager.getBaseUrl()}/api/saving"
        val jsonArrayRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener { response ->
                if (response.getBoolean("success")) {
                    val responseAccount = Saving.fromJson(response.getJSONObject("data").toString())
                    val responseCore = ResponseCore(
                        response.getBoolean("success"),
                        response.getString("message"),
                        responseAccount
                    )
                    listener.onSuccess(responseCore)
                } else {
                    listener.onError(response.getString("message"))
                }
            },
            Response.ErrorListener { error ->
                val errorMessage = "$error"
                println(error)
                listener.onError(errorMessage)
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                apiManager.getToken()?.let {
                    headers["Authorization"] = "Bearer $it"
                }
                return headers
            }
        }
        requestQueue.add(jsonArrayRequest)
    }
}
