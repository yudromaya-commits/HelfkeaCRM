package com.helfkea.crm.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.helfkea.crm.data.AuthCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    suspend fun login(credentials: AuthCredentials): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Здесь должна быть реальная проверка с сервером
                // Пока просто сохраняем и считаем успешным
                if (credentials.rememberMe) {
                    saveCredentials(credentials)
                }
                setLoggedIn(true)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    fun logout() {
        sharedPreferences.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .remove(KEY_REMEMBER_ME)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }
    
    fun getSavedCredentials(): AuthCredentials? {
        val username = sharedPreferences.getString(KEY_USERNAME, null)
        val password = sharedPreferences.getString(KEY_PASSWORD, null)
        val rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
        
        return if (username != null && password != null) {
            AuthCredentials(username, password, rememberMe)
        } else {
            null
        }
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun saveCredentials(credentials: AuthCredentials) {
        sharedPreferences.edit()
            .putString(KEY_USERNAME, credentials.username)
            .putString(KEY_PASSWORD, credentials.password)
            .putBoolean(KEY_REMEMBER_ME, credentials.rememberMe)
            .apply()
    }
    
    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            .apply()
    }
    
    fun clearCredentials() {
        sharedPreferences.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .remove(KEY_REMEMBER_ME)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }
    
    fun getCredentials(): AuthCredentials? {
        return getSavedCredentials()
    }
    
    fun getCurrentCredentials(): AuthCredentials? {
        return if (isLoggedIn()) {
            getSavedCredentials()
        } else {
            null
        }
    }
}