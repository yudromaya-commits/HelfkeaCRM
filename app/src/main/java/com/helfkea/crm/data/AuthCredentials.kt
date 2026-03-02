package com.helfkea.crm.data

data class AuthCredentials(
    val username: String,
    val password: String,
    val rememberMe: Boolean = false
) {
    fun isValid(): Boolean {
        return username.isNotBlank() && password.isNotBlank()
    }
    
    fun toBasicAuthHeader(): String {
        val credentials = "$username:$password"
        return android.util.Base64.encodeToString(
            credentials.toByteArray(),
            android.util.Base64.NO_WRAP
        )
    }
}