package com.om.diucampusschedule.ui.utils

import android.content.Context
import com.om.diucampusschedule.domain.model.Faculty
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.json.JSONArray
import java.io.IOException

object FacultyUtils {

    fun loadFacultyData(context: Context): List<Faculty> {
        return try {
            val jsonString = context.assets.open("facultyinfo.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonArray = JSONArray(jsonString)
            val facultyDataList = mutableListOf<Faculty>()

            for (i in 0 until jsonArray.length()) {
                val facultyJsonObject = jsonArray.getJSONObject(i)
                val faculty = Json.Default.decodeFromJsonElement<Faculty>(Json.Default.parseToJsonElement(facultyJsonObject.toString()))
                facultyDataList.add(faculty)
            }
            facultyDataList
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList() // Return empty list in case of error
        }
    }
}