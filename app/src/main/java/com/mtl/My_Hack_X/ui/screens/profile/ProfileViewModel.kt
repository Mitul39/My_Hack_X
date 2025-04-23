package com.mtl.My_Hack_X.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mtl.My_Hack_X.MyHackXApp
import com.mtl.My_Hack_X.data.models.User
import com.mtl.My_Hack_X.data.services.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        if (MyHackXApp.isTestMode) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    user = User(
                        uid = "test_user_id",
                        email = "test@example.com",
                        displayName = "Test User",
                        photoUrl = "",
                        isAdmin = true
                    ),
                    error = null
                )
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Check if user is authenticated
                val firebaseUser = auth.currentUser
                if (firebaseUser == null) {
                    Log.d("ProfileViewModel", "No Firebase auth user found")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Please sign in to view your profile"
                        )
                    }
                    return@launch
                }
                
                // Try to get user from Firestore
                var user = firebaseService.getCurrentUser()
                
                // If user doesn't exist in Firestore, create it
                if (user == null) {
                    Log.d("ProfileViewModel", "Creating new user profile")
                    user = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "User",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                    )
                    
                    // Save the user to Firestore
                    try {
                        firebaseService.updateUser(user)
                        Log.d("ProfileViewModel", "User profile created successfully")
                    } catch (e: Exception) {
                        Log.e("ProfileViewModel", "Error creating user profile", e)
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "Failed to create user profile: ${e.message}"
                            )
                        }
                        return@launch
                    }
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        user = user,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Failed to load profile: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun signOut() {
        firebaseService.signOut()
        _uiState.update { it.copy(user = null) }
    }
} 