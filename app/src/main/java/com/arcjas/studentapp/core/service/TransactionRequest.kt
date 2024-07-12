package com.arcjas.studentapp.core.service

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arcjas.studentapp.core.config.VolleyListener
import com.arcjas.studentapp.core.model.ResponseCore
import com.arcjas.studentapp.core.model.Transaction
import org.json.JSONException
import org.json.JSONObject

class TransactionRequest(private val context: Context, private val apiManager: ApiManager) {
    private val baseUrl = "${apiManager.getBaseUrl()}/api/transactions"
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    fun createTransaction(transaction: JSONObject, listener: VolleyListener<ResponseCore<String>>) {
        val url = "${apiManager.getBaseUrl()}/api/transactions/payProduct"

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, transaction,
            Response.Listener { response ->
                val message = "Transaction created successfully"
                val innerResponseCore =
                    ResponseCore(true, message, "Transaction created successfully")
                val responseCore = ResponseCore(true, "ResponseCore of String", innerResponseCore)
                listener.onSuccess(responseCore)
            },
            Response.ErrorListener { error ->
                val errorMessage = "Error creating transaction: ${error.toString()}"
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

    fun createTransaction(name: String, amount: String, listener: VolleyListener<Transaction>) {
        val url = "${apiManager.getBaseUrl()}/api/transactions/paymentService"

        val jsonBody = JSONObject().apply {
            put("name", name)
            put("amount", amount)
        }
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, jsonBody,
            Response.Listener { response ->
                if (response.getBoolean("success")) {
                    val responseAccount =
                        Transaction.fromJson(response.getJSONObject("data").toString())
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
                val errorMessage = "Error creating transaction: ${error.toString()}"
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

    fun listTransactions(listener: VolleyListener<ResponseCore<List<Transaction>>>) {
        val url = "${apiManager.getBaseUrl()}/api/transactions"

        val jsonArrayRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener { response ->
                try {
                    val success = response.optBoolean("success", false)
                    val message = response.optString("message", "")
                    val transactions = mutableListOf<Transaction>()

                    val dataJsonArray = response.getJSONArray("data")
                    for (i in 0 until dataJsonArray.length()) {
                        val transactionJson = dataJsonArray.getJSONObject(i)
                        val transaction = Transaction.fromJson(transactionJson.toString())
                        if (transaction != null) {
                            transactions.add(transaction)
                        }
                    }

                    val innerResponseCore = ResponseCore(success, message, transactions.toList())
                    val responseCore = ResponseCore(
                        success,
                        "ResponseCore of List<Transaction>",
                        innerResponseCore
                    )
                    listener.onSuccess(responseCore)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    listener.onError("Error parsing JSON: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                val errorMessage = "Error retrieving transactions: ${error.toString()}"
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

    fun listTransactionsByFilter(
        type: String,
        date: String,
        listener: VolleyListener<ResponseCore<List<Transaction>>>
    ) {
        val url = "${apiManager.getBaseUrl()}/api/transactions/all/ByFilter"

        val jsonArrayRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            JSONObject().apply {
                put("type", type)
                put("date", date)
            },
            Response.Listener { response ->
                try {
                    val success = response.optBoolean("success", false)
                    val message = response.optString("message", "")
                    val transactions = mutableListOf<Transaction>()
                    val dataJsonArray = response.getJSONArray("data")
                    for (i in 0 until dataJsonArray.length()) {
                        val transactionJson = dataJsonArray.getJSONObject(i)
                        val transaction = Transaction.fromJson(transactionJson.toString())
                        if (transaction != null) {
                            transactions.add(transaction)
                        }
                    }
                    val innerResponseCore = ResponseCore(success, message, transactions.toList())
                    val responseCore = ResponseCore(
                        success,
                        "ResponseCore of List<Transaction>",
                        innerResponseCore
                    )
                    listener.onSuccess(responseCore)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    listener.onError("Error parsing JSON: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                val errorMessage = "Error retrieving transactions: ${error.toString()}"
                listener.onError(errorMessage)
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // Agregar el token de autorización en los encabezados
                apiManager.getToken()?.let {
                    headers["Authorization"] = "Bearer $it"
                }
                return headers
            }
        }

        requestQueue.add(jsonArrayRequest)
    }

}
