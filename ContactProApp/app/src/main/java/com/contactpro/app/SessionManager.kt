package com.contactpro.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        val KEY_USER_ID = longPreferencesKey("user_id")
        val KEY_TOKEN   = stringPreferencesKey("token")
        val KEY_NAME    = stringPreferencesKey("name")
        val KEY_EMAIL   = stringPreferencesKey("email")
        val KEY_PHONE   = stringPreferencesKey("phone")
        val KEY_COMPANY = stringPreferencesKey("company")
        val KEY_LOGGED_IN = booleanPreferencesKey("logged_in")
        val KEY_THEME   = stringPreferencesKey("theme")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: "System"
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOGGED_IN] ?: false
    }

    val userId: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID] ?: -1L
    }

    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOKEN]
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_NAME] ?: ""
    }

    val userEmail: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMAIL] ?: ""
    }

    val userPhone: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PHONE] ?: ""
    }

    val userCompany: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_COMPANY] ?: ""
    }

    suspend fun saveSession(
        userId: Long,
        token: String,
        name: String,
        email: String,
        phone: String? = null,
        company: String? = null
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOGGED_IN] = true
            prefs[KEY_USER_ID]   = userId
            prefs[KEY_TOKEN]     = token
            prefs[KEY_NAME]      = name
            prefs[KEY_EMAIL]     = email
            prefs[KEY_PHONE]     = phone ?: ""
            prefs[KEY_COMPANY]   = company ?: ""
        }
    }

    suspend fun updateProfile(name: String, phone: String?, company: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NAME]    = name
            prefs[KEY_PHONE]   = phone ?: ""
            prefs[KEY_COMPANY] = company ?: ""
        }
    }

    suspend fun setTheme(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = mode
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
