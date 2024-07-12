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
import org.json.JSONObject
import java.lang.reflect.Type
import java.math.BigDecimal

data class Saving(
    val id: Long?,
    val userId: Long,
    @SerializedName("source_account_id") val sourceAccountId: Long,
    @SerializedName("destination_account_id") val destinationAccountId: Long,
    val amount: BigDecimal,
    @SerializedName("saving_goal") val savingGoal: BigDecimal,
    @SerializedName("saving_interval") val savingInterval: Long,
    val description: String,
    val status: Boolean,
    @SerializedName("last_processed_at") val lastProcessedAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
) {
    companion object {
        private val gson: Gson = GsonBuilder()
            .registerTypeAdapter(Saving::class.java, SavingDeserializer())
            .create()

        fun fromJson(json: String): Saving? {
            return try {
                gson.fromJson(json, Saving::class.java)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                null
            }
        }

        fun toJson(saving: Saving): String {
            return gson.toJson(saving)
        }

        fun listFromJson(json: String): List<Saving>? {
            return try {
                val listType = object : TypeToken<List<Saving>>() {}.type
                gson.fromJson(json, listType)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                null
            }
        }

        fun listToJson(savings: List<Saving>): String {
            val listType = object : TypeToken<List<Saving>>() {}.type
            return gson.toJson(savings, listType)
        }
    }

    fun toJsonObject(): JSONObject {
        val jsonString = gson.toJson(this)
        return gson.fromJson(jsonString, JSONObject::class.java)
    }

    private class SavingDeserializer : JsonDeserializer<Saving> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Saving {
            val jsonObject = json?.asJsonObject ?: throw JsonParseException("Invalid JSON format")

            val id = jsonObject.getAsJsonPrimitive("id")?.asLong
            val userId = jsonObject.getAsJsonPrimitive("user_id").asLong
            val sourceAccountId = jsonObject.getAsJsonPrimitive("source_account_id").asLong
            val destinationAccountId =
                jsonObject.getAsJsonPrimitive("destination_account_id").asLong
            val amount = jsonObject.getAsJsonPrimitive("amount").asBigDecimal
            val savingGoal = jsonObject.getAsJsonPrimitive("saving_goal").asBigDecimal
            val savingInterval = jsonObject.getAsJsonPrimitive("saving_interval").asLong
            val description = jsonObject.getAsJsonPrimitive("description").asString
            val status = jsonObject.getAsJsonPrimitive("status").asBoolean
            val lastProcessedAt = jsonObject.getAsJsonPrimitive("last_processed_at")?.asString
            val createdAt = jsonObject.getAsJsonPrimitive("created_at")?.asString
            val updatedAt = jsonObject.getAsJsonPrimitive("updated_at")?.asString

            return Saving(
                id, userId, sourceAccountId, destinationAccountId, amount, savingGoal,
                savingInterval, description, status, lastProcessedAt, createdAt, updatedAt
            )
        }
    }
}