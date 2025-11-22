package com.example.resitrack.data.manager

import com.example.resitrack.data.source.NotificationsSource

class NotificationsManager(private val source: NotificationsSource) {
    suspend fun saveUserToken(userId: String, token: String) = source.saveToken(userId, token)
}
