package com.om.diucampusschedule.data.remote

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.om.diucampusschedule.data.model.UserDto
import com.om.diucampusschedule.data.model.toUserDto
import com.om.diucampusschedule.domain.model.SignInRequest
import com.om.diucampusschedule.domain.model.SignUpRequest
import com.om.diucampusschedule.domain.model.UserRegistrationForm
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun signIn(request: SignInRequest): Result<UserDto> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(request.email, request.password).await()
            val firebaseUser = result.user ?: throw Exception("Authentication failed")
            
            // Get user data from Firestore
            val userDoc = usersCollection.document(firebaseUser.uid).get().await()
            val userDto = userDoc.toUserDto()
                ?: throw Exception("User profile not found")
            
            Result.success(userDto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(request: SignUpRequest): Result<UserDto> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(request.email, request.password).await()
            val firebaseUser = result.user ?: throw Exception("User creation failed")
            
            // Create user document in Firestore
            val userDto = UserDto(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: request.email,
                isProfileComplete = false
            )
            
            usersCollection.document(firebaseUser.uid).set(userDto).await()
            
            Result.success(userDto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<UserDto> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: throw Exception("Google sign-in failed")
            
            // Check if user exists in Firestore
            val userDoc = usersCollection.document(firebaseUser.uid).get().await()
            
            val userDto = if (userDoc.exists()) {
                // Existing user
                userDoc.toUserDto() ?: throw Exception("Failed to parse user data")
            } else {
                // New user, create profile
                val newUserDto = UserDto(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "",
                    profilePictureUrl = firebaseUser.photoUrl?.toString() ?: "",
                    isProfileComplete = false
                )
                
                usersCollection.document(firebaseUser.uid).set(newUserDto).await()
                newUserDto
            }
            
            Result.success(userDto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<UserDto?> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                Result.success(null)
            } else {
                val userDoc = usersCollection.document(firebaseUser.uid).get().await()
                val userDto = userDoc.toUserDto()
                Result.success(userDto)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeAuthState(): Flow<UserDto?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                // Launch a coroutine to get user data from Firestore
                usersCollection.document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }
                        
                        val userDto = snapshot?.toUserDto()
                        trySend(userDto)
                    }
            }
        }
        
        firebaseAuth.addAuthStateListener(listener)
        
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    suspend fun updateUserProfile(userId: String, form: UserRegistrationForm): Result<UserDto> {
        return try {
            val updates = hashMapOf<String, Any>(
                "name" to form.name,
                "profilePictureUrl" to form.profilePictureUrl,
                "department" to form.department,
                "role" to form.role.name,
                "batch" to form.batch,
                "section" to form.section,
                "labSection" to form.labSection,
                "initial" to form.initial,
                "isProfileComplete" to true,
                "updatedAt" to System.currentTimeMillis()
            )
            
            usersCollection.document(userId).update(updates).await()
            
            // Get updated user data
            val userDoc = usersCollection.document(userId).get().await()
            val userDto = userDoc.toUserDto() ?: throw Exception("Failed to get updated user data")
            
            Result.success(userDto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isUserProfileComplete(userId: String): Result<Boolean> {
        return try {
            val userDoc = usersCollection.document(userId).get().await()
            val isComplete = userDoc.getBoolean("isProfileComplete") ?: false
            Result.success(isComplete)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
