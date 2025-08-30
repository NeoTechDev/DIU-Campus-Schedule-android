package com.om.diucampusschedule.core.service

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.om.diucampusschedule.domain.model.CourseInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to handle course name mapping from courseNames.json asset file
 */
@Singleton
class CourseNameService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private var courseMap: Map<String, CourseInfo>? = null
    
    companion object {
        private const val COURSE_NAMES_FILE = "courseNames.json"
        private const val TAG = "CourseNameService"
    }
    
    /**
     * Load course names from assets file
     */
    suspend fun loadCourseNames(): Result<Map<String, CourseInfo>> = withContext(Dispatchers.IO) {
        try {
            if (courseMap != null) {
                return@withContext Result.success(courseMap!!)
            }
            
            android.util.Log.d(TAG, "Loading course names from assets...")
            
            val inputStream = context.assets.open(COURSE_NAMES_FILE)
            val reader = InputStreamReader(inputStream)
            
            val courseListType = object : TypeToken<List<CourseInfo>>() {}.type
            val courseList: List<CourseInfo> = gson.fromJson(reader, courseListType)
            
            // Create a map for quick lookup by course code
            courseMap = courseList.associateBy { it.courseCode.uppercase() }
            
            android.util.Log.d(TAG, "Loaded ${courseMap!!.size} course names successfully")
            
            reader.close()
            inputStream.close()
            
            Result.success(courseMap!!)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error loading course names", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get course name for a given course code
     */
    suspend fun getCourseName(courseCode: String): String? {
        val courses = loadCourseNames().getOrNull() ?: return null
        return courses[courseCode.uppercase()]?.courseName
    }
    
    /**
     * Get course info for a given course code
     */
    suspend fun getCourseInfo(courseCode: String): CourseInfo? {
        val courses = loadCourseNames().getOrNull() ?: return null
        return courses[courseCode.uppercase()]
    }
    
    /**
     * Clear the cache to force reload on next access
     */
    fun clearCache() {
        courseMap = null
    }
}