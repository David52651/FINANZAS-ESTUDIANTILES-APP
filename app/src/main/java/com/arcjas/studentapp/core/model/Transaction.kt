package com.arcjas.studentapp.core.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

data class Transaction(
    val id: Int,
    @SerializedName("account_id") val accountId: Int?,  // Cambio para recibir account_id
    val userId: Int?,
    val type: String?,
    val description: String?,
    val amount: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("created_at_formatted") val createdAtFormatted: String?
) {
    companion object {
        private val gson: Gson = GsonBuilder()
            .registerTypeAdapter(Transaction::class.java, TransactionDeserializer())
            .create()

        fun fromJson(json: String): Transaction? {
            return try {
                gson.fromJson(json, Transaction::class.java)
            } catch (e: JsonParseException) {
                e.printStackTrace()
                null
            }
        }

        fun toJson(transaction: Transaction): String {
            return gson.toJson(transaction)
        }

        fun listFromJson(json: String): List<Transaction>? {
            return try {
                val listType = object : TypeToken<List<Transaction>>() {}.type
                gson.fromJson(json, listType)
            } catch (e: JsonParseException) {
                e.printStackTrace()
                null
            }
        }

        fun listToJson(transactions: List<Transaction>): String {
            val listType = object : TypeToken<List<Transaction>>() {}.type
            return gson.toJson(transactions, listType)
        }
    }

    private class TransactionDeserializer : JsonDeserializer<Transaction> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Transaction {
            val jsonObject = json?.asJsonObject ?: throw JsonParseException("Invalid JSON format")

            val id = jsonObject.getAsJsonPrimitive("id").asInt
            val accountId = jsonObject.getAsJsonPrimitive("account_id")?.asInt
            val userId = jsonObject.getAsJsonPrimitive("user_id")?.asInt
            val type = jsonObject.getAsJsonPrimitive("type")?.asString
            val description = jsonObject.getAsJsonPrimitive("description")?.asString
            val amount = jsonObject.getAsJsonPrimitive("amount")?.asString
            val createdAt = jsonObject.getAsJsonPrimitive("created_at")?.asString
            val createdAtFormatted = jsonObject.getAsJsonPrimitive("created_at_formatted")?.asString


            return Transaction(id, accountId, userId, type, description, amount, createdAt,createdAtFormatted)
        }
    }

}
