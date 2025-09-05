package com.om.diucampusschedule.ui.utils

import android.content.Context
import com.om.diucampusschedule.domain.model.TaskGroup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object TaskGroupUtils {
    private const val FILE_NAME = "task_groups.json"

    fun saveTaskGroups(context: Context, groups: List<TaskGroup>) {
        val jsonString = Json.encodeToString(groups)
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(jsonString)
    }

    fun loadTaskGroups(context: Context): List<TaskGroup> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            // Create default "All Tasks" group
            val defaultGroup = TaskGroup(id = 0, name = "All Tasks")
            saveTaskGroups(context, listOf(defaultGroup))
            return listOf(defaultGroup)
        }
        val jsonString = file.readText()
        return Json.decodeFromString(jsonString)
    }
}