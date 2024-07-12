package com.arcjas.studentapp.core.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.math.BigDecimal

data class Account(
    val id: Long?,
    @SerializedName("user_id") val userId: Long?,
    val name: String?,
    val balance: BigDecimal?,
    @SerializedName("is_main") val isMain: Boolean?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
) {
    companion object {
        private val gson: Gson = GsonBuilder()
            .registerTypeAdapter(Account::class.java, AccountDeserializer())
            .create()

        fun fromJson(json: String): Account? {
            return try {
                gson.fromJson(json, Account::class.java)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                null
            }
        }
        fun toJson(account: Account): String {
            return gson.toJson(account)
        }
        fun listFromJson(json: String): List<Account>? {
            return try {
                val listType = object : TypeToken<List<Account>>() {}.type
                gson.fromJson(json, listType)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                null
            }
        }
        fun listToJson(accounts: List<Account>): String {
            val listType = object : TypeToken<List<Account>>() {}.type
            return gson.toJson(accounts, listType)
        }
    }

    private class AccountDeserializer : JsonDeserializer<Account> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Account {
            val jsonObject = json?.asJsonObject ?: throw JsonParseException("Invalid JSON format")
            val id = jsonObject.getAsJsonPrimitive("id")?.asLong
            val userId = jsonObject.getAsJsonPrimitive("user_id")?.asLong
            val name = jsonObject.getAsJsonPrimitive("name")?.asString
            val balance = jsonObject.getAsJsonPrimitive("balance")?.asBigDecimal
            val isMain = jsonObject.getAsJsonPrimitive("is_main")?.asBoolean
            val createdAt = jsonObject.getAsJsonPrimitive("created_at")?.asString
            val updatedAt = jsonObject.getAsJsonPrimitive("updated_at")?.asString

            return Account(id, userId, name, balance, isMain, createdAt, updatedAt)
        }
    }
}