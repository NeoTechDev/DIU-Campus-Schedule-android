package com.om.diucampusschedule.data.repository

import com.om.diucampusschedule.domain.model.Notice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepository @Inject constructor() {
    suspend fun fetchNotices(): List<Notice> = withContext(Dispatchers.IO) {
        val url = "https://daffodilvarsity.edu.bd/department/swe/notice"
        val notices = mutableListOf<Notice>()
        try {
            val doc = Jsoup.connect(url).get()
            val entries = doc.select("div.blog-entry")
            for (entry in entries) {
                val titleElement = entry.selectFirst(".date-description .heading a")
                val title = titleElement?.text() ?: continue
                val link = titleElement.absUrl("href")
                // Parse date from the col-md-2 div that contains calendar icon and date
                val dateElement = entry.selectFirst(".date-description .row .col-md-2:has(i.fas.fa-calendar-alt)")
                val date = dateElement?.text()?.replace("📅", "")?.trim() ?: ""
                notices.add(Notice(title = title, link = link, date = date))
            }
        } catch (e: Exception) {
            // Handle error, optionally log
        }
        notices
    }
}