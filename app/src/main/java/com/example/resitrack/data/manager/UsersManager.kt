package com.example.resitrack.data.manager

import com.example.resitrack.data.model.User
import com.example.resitrack.data.source.UsersSource

class UsersManager(private val source: UsersSource) {
    suspend fun getUserRole(uid: String) = source.getUserRole(uid)

    fun getResidentsCountFlow() = source.getResidentsCountFlow()
    fun getAllResidentsFlow() = source.getAllResidentsFlow()

    suspend fun getResidentDetails(uid: String) = source.getResidentDetails(uid)
    suspend fun updateResident(user: User) = source.updateResident(user)
}

