package com.om.diucampusschedule.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.om.diucampusschedule.data.model.RoutineScheduleDto
import com.om.diucampusschedule.data.model.toDomainModel
import com.om.diucampusschedule.data.model.toDto
import com.om.diucampusschedule.domain.model.RoutineSchedule
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val ROUTINES_COLLECTION = "routines"
    }

    suspend fun getLatestRoutineForDepartment(department: String): Result<RoutineSchedule> {
        return try {
            val querySnapshot = firestore.collection(ROUTINES_COLLECTION)
                .whereEqualTo("department", department)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                val document = querySnapshot.documents.first()
                val routineDto = document.toObject(RoutineScheduleDto::class.java)
                if (routineDto != null) {
                    val routine = routineDto.copy(id = document.id).toDomainModel()
                    Result.success(routine)
                } else {
                    Result.failure(Exception("Failed to parse routine data"))
                }
            } else {
                Result.failure(Exception("No routine found for department: $department"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRoutineById(routineId: String): Result<RoutineSchedule> {
        return try {
            val document = firestore.collection(ROUTINES_COLLECTION)
                .document(routineId)
                .get()
                .await()

            if (document.exists()) {
                val routineDto = document.toObject(RoutineScheduleDto::class.java)
                if (routineDto != null) {
                    val routine = routineDto.copy(id = document.id).toDomainModel()
                    Result.success(routine)
                } else {
                    Result.failure(Exception("Failed to parse routine data"))
                }
            } else {
                Result.failure(Exception("Routine not found with ID: $routineId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkForUpdates(department: String, currentVersion: Long): Result<Boolean> {
        return try {
            val querySnapshot = firestore.collection(ROUTINES_COLLECTION)
                .whereEqualTo("department", department)
                .whereGreaterThan("version", currentVersion)
                .limit(1)
                .get()
                .await()

            Result.success(querySnapshot.documents.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeRoutineUpdates(department: String): Flow<RoutineSchedule?> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            listenerRegistration = firestore.collection(ROUTINES_COLLECTION)
                .whereEqualTo("department", department)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val document = snapshot.documents.first()
                        val routineDto = document.toObject(RoutineScheduleDto::class.java)
                        if (routineDto != null) {
                            val routine = routineDto.copy(id = document.id).toDomainModel()
                            trySend(routine)
                        } else {
                            trySend(null)
                        }
                    } else {
                        trySend(null)
                    }
                }
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    suspend fun uploadRoutine(routine: RoutineSchedule): Result<String> {
        return try {
            val routineDto = routine.toDto()
            val documentRef = if (routine.id.isNotEmpty()) {
                firestore.collection(ROUTINES_COLLECTION).document(routine.id)
            } else {
                firestore.collection(ROUTINES_COLLECTION).document()
            }

            documentRef.set(routineDto).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRoutine(routineId: String): Result<Unit> {
        return try {
            firestore.collection(ROUTINES_COLLECTION)
                .document(routineId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllRoutinesForDepartment(department: String): Result<List<RoutineSchedule>> {
        return try {
            val querySnapshot = firestore.collection(ROUTINES_COLLECTION)
                .whereEqualTo("department", department)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val routines = querySnapshot.documents.mapNotNull { document ->
                val routineDto = document.toObject(RoutineScheduleDto::class.java)
                routineDto?.copy(id = document.id)?.toDomainModel()
            }

            Result.success(routines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
