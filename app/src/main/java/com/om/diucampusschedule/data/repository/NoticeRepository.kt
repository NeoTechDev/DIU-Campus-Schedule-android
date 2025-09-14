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
                val dateElement = entry.selectFirst(".event-date-wrap p")
                val monthYearElement = entry.selectFirst(".event-date-wrap span")
                val date = if (dateElement != null && monthYearElement != null) {
                    dateElement.text() + " " + monthYearElement.text()
                } else ""
                notices.add(Notice(title = title, link = link, date = date))
            }
        } catch (e: Exception) {
            // Handle error, optionally log
        }
        notices
    }
}