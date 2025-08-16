package com.example.portalapp.util

import retrofit2.HttpException
import java.io.IOException

object Http {
    fun friendlyMessage(t: Throwable): String = when (t) {
        is IOException -> "Network error. Check your connection and try again."
        is HttpException -> {
            val code = t.code()
            when (code) {
                400 -> "Invalid request. Please check your details."
                401 -> "Invalid username or password."
                403 -> "You don't have access."
                404 -> "Service not found."
                500 -> "Server error. Please try again later."
                else -> "Unexpected error (HTTP $code)."
            }
        }
        else -> "Something went wrong. Please try again."
    }
}
