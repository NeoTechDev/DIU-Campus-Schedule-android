package com.om.diucampusschedule.domain.repository

import com.om.diucampusschedule.domain.model.Notice

interface NoticeRepository {
    suspend fun fetchNotices(): List<Notice>
}