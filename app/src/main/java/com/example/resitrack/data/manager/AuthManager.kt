package com.example.resitrack.data.manager

import com.example.resitrack.data.model.User
import com.example.resitrack.data.source.AuthSource

class AuthManager(private val source: AuthSource) {
    suspend fun loginUser(email: String, password: String) = source.signIn(email, password)
    suspend fun registerUser(email: String, password: String, user: User) = source.signUp(email, password, user)
    fun getCurrentUserUid() = source.getCurrentUserUid()
    suspend fun getUserRole(uid: String) = source.getUserRole(uid)

    fun getCurrentUserId(): String? = source.getCurrentUserId()
    fun signOut() = source.signOut()
}