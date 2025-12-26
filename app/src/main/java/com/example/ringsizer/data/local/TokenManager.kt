package com.example.ringsizer.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_TOKEN = stringPreferencesKey("auth_token")
    private val KEY_USER_ID = longPreferencesKey("user_id")
    private val KEY_USER_ROLE = stringPreferencesKey("user_role")
    private val KEY_USER_NAME = stringPreferencesKey("user_name")

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOKEN]
    }

    val userIdFlow: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID]
    }

    val userRoleFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ROLE]
    }

    val userNameFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME]
    }

    suspend fun setToken(token: String) {
        context.dataStore.edit { prefs -> prefs[KEY_TOKEN] = token }
    }

    suspend fun setUser(id: Long, role: String, name: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = id
            prefs[KEY_USER_ROLE] = role
            prefs[KEY_USER_NAME] = name
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_ROLE)
            prefs.remove(KEY_USER_NAME)
        }
    }

    suspend fun getToken(): String? = context.dataStore.data.map { it[KEY_TOKEN] }.firstOrNull()
}
