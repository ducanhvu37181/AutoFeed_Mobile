package com.example.autofeedmobile.data

import android.content.Context
import android.content.SharedPreferences
import com.example.autofeedmobile.network.UserResponse
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("autofeed_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER = "user"
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveUser(user: UserResponse) {
        val json = gson.toJson(user)
        prefs.edit().putString(KEY_USER, json).apply()
    }

    fun fetchUser(): UserResponse? {
        val json = prefs.getString(KEY_USER, null)
        return if (json != null) {
            gson.fromJson(json, UserResponse::class.java)
        } else {
            null
        }
    }

    fun clearSession() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_USER).apply()
    }
}
