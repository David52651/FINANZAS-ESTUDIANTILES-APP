package com.arcjas.studentapp.core.config

import com.arcjas.studentapp.core.model.ResponseCore

interface VolleyListener<T> {
    fun onSuccess(response: ResponseCore<T>)
    fun onError(error: String)
}