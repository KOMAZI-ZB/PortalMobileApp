package com.example.portalapp.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull // <-- added to support token()
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val USERNAME = stringPreferencesKey("userName")
        val ROLES = stringPreferencesKey("roles_csv")
        val JOINDATE = stringPreferencesKey("joinDate")
    }

    fun tokenFlow(): Flow<String?> =
        context.dataStore.data.map { it[Keys.TOKEN] }

    suspend fun token(): String? =
        context.dataStore.data.map { it[Keys.TOKEN] }.firstOrNull()

    fun userNameFlow(): Flow<String?> =
        context.dataStore.data.map { it[Keys.USERNAME] }

    fun rolesFlow(): Flow<List<String>> =
        context.dataStore.data.map { (it[Keys.ROLES] ?: "").split(',').filter { s -> s.isNotBlank() } }

    suspend fun setToken(token: String) {
        context.dataStore.edit { it[Keys.TOKEN] = token }
    }

    suspend fun setUserBasics(userName: String, roles: List<String>, joinDate: String?) {
        context.dataStore.edit {
            it[Keys.USERNAME] = userName
            it[Keys.ROLES] = roles.joinToString(",")
            if (joinDate != null) it[Keys.JOINDATE] = joinDate else it.remove(Keys.JOINDATE)
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
