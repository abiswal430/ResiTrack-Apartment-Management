package com.example.resitrack.data.manager

import com.example.resitrack.data.model.Notice
import com.example.resitrack.data.source.NoticesSource

class NoticesManager(private val source: NoticesSource) {
    fun getAllNoticesFlow() = source.getAllNoticesFlow()
    fun getAllResidentNoticesFlow() = source.getAllResidentNoticesFlow()

    fun getLatestNoticesFlow(limit: Long) = source.getLatestNoticesFlow(limit)
    suspend fun getNoticeDetails(noticeId: String) = source.getNoticeDetails(noticeId)
    suspend fun addNotice(notice: Notice) = source.addNotice(notice)
    suspend fun updateNotice(notice: Notice) = source.updateNotice(notice)
    suspend fun deleteNotice(noticeId: String) = source.deleteNotice(noticeId)
}
