package com.arcjas.studentapp.core.service

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arcjas.studentapp.core.config.VolleyListener
import com.arcjas.studentapp.core.model.Account
import com.arcjas.studentapp.core.model.ResponseCore
import org.json.JSONException
import org.json.JSONObject

class AccountRequest(private val context: Context, private val apiManager: ApiManager) {
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    fun createAccount(name: String, listener: VolleyListener<Account>) {
        val url = "${apiManager.getBaseUrl()}/api/account"
        val jsonBody = JSONObject()
        jsonBody.put("name", name)
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, jsonBody,
            Response.Listener { response ->
                val responseAccount = Account.fromJson(response.getJSONObject("data").toString())
                val responseCore = ResponseCore(
                    response.getBoolean("success"),
                    response.getString("message"),
                    responseAccount
                )
                listener.onSuccess(responseCore)
            },
            Response.ErrorListener { error ->
                val errorMessage = " $error"
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

    fun deleteAccount(account: Account, listener: VolleyListener<String>) {
        val url = "${apiManager.getBaseUrl()}/api/account/" + account.id
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.DELETE, url, null,
            Response.Listener { response ->
                val responseCore = ResponseCore(
                    response.getBoolean("success"),
                    response.getString("message"),
                    ""
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

    fun listAccount(listener: VolleyListener<ResponseCore<List<Account>>>) {
        val url = "${apiManager.getBaseUrl()}/api/account"

        val jsonArrayRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener { response ->
                try {
                    val success = response.optBoolean("success", false)
                    val message = response.optString("message", "")
                    val account = mutableListOf<Account>()

                    val dataJsonArray = response.getJSONArray("data")
                    for (i in 0 until dataJsonArray.length()) {
                        val transactionJson = dataJsonArray.getJSONObject(i)
                        val transaction = Account.fromJson(transactionJson.toString())
                        if (transaction != null) {
                            account.add(transaction)
                        }
                    }

                    val innerResponseCore = ResponseCore(success, message, account.toList())
                    val responseCore =
                        ResponseCore(success, "ResponseCore of List<Account>", innerResponseCore)
                    listener.onSuccess(responseCore)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    listener.onError("Error parsing JSON: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                val errorMessage = "Error retrieving account: ${error.toString()}"
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
