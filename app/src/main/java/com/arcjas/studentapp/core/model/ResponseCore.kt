package com.arcjas.studentapp.core.model

data class ResponseCore<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)