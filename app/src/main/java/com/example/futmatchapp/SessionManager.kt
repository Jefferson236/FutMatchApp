package com.example.futmatchapp

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("FutMatchPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val USER_ID = "user_id"
        private const val PERFIL_ID = "perfil_id"
    }

    fun saveUserId(id: Int) {
        prefs.edit().putInt(USER_ID, id).apply()
    }

    fun getUserId(): Int {
        return prefs.getInt(USER_ID, -1)
    }

    fun savePerfilId(id: Int) {
        prefs.edit().putInt(PERFIL_ID, id).apply()
    }

    fun getPerfilId(): Int {
        return prefs.getInt(PERFIL_ID, -1)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
