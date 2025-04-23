package com.mtl.My_Hack_X.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mtl.My_Hack_X.MyHackXApp
import com.mtl.My_Hack_X.data.services.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    data class Error(val message: String) : LoginState()
    object Success : LoginState()
}

class LoginViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    init {
        // Check if user is already signed in
        try {
            FirebaseAuth.getInstance().currentUser?.let {
                _loginState.value = LoginState.Success
            }
        } catch (e: Exception) {
            // If in test mode, we can bypass the error
            if (MyHackXApp.isTestMode) {
                _loginState.value = LoginState.Initial
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        if (MyHackXApp.isTestMode) {
            // In test mode, just simulate a successful login
            _loginState.value = LoginState.Loading
            _loginState.value = LoginState.Success
            return
        }
        
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                firebaseService.signInWithGoogle(idToken)
                    .onSuccess {
                        _loginState.value = LoginState.Success
                    }
                    .onFailure { exception ->
                        _loginState.value = LoginState.Error(exception.message ?: "Sign in failed")
                    }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Sign in failed")
            }
        }
    }
    
    fun signInWithEmail(email: String, password: String) {
        if (MyHackXApp.isTestMode) {
            // In test mode, just simulate a successful login with test@example.com/password
            _loginState.value = LoginState.Loading
            if (email == "test@example.com" && password == "password") {
                _loginState.value = LoginState.Success
            } else {
                _loginState.value = LoginState.Error("Invalid email or password")
            }
            return
        }
        
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                firebaseService.signInWithEmail(email, password)
                    .onSuccess {
                        _loginState.value = LoginState.Success
                    }
                    .onFailure { exception ->
                        _loginState.value = LoginState.Error(exception.message ?: "Sign in failed")
                    }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Sign in failed")
            }
        }
    }
    
    fun registerWithEmail(email: String, password: String) {
        if (MyHackXApp.isTestMode) {
            // In test mode, just simulate a successful registration
            _loginState.value = LoginState.Loading
            _loginState.value = LoginState.Success
            return
        }
        
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                firebaseService.registerWithEmail(email, password)
                    .onSuccess {
                        _loginState.value = LoginState.Success
                    }
                    .onFailure { exception ->
                        _loginState.value = LoginState.Error(exception.message ?: "Registration failed")
                    }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Registration failed")
            }
        }
    }
    
    fun resetPassword(email: String, onComplete: (Boolean, String?) -> Unit) {
        if (MyHackXApp.isTestMode) {
            // In test mode, just simulate a successful password reset
            onComplete(true, null)
            return
        }
        
        viewModelScope.launch {
            try {
                firebaseService.sendPasswordResetEmail(email)
                    .onSuccess {
                        onComplete(true, null)
                    }
                    .onFailure { exception ->
                        onComplete(false, exception.message ?: "Password reset failed")
                    }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Password reset failed")
            }
        }
    }

    fun signOut() {
        try {
            firebaseService.signOut()
        } catch (e: Exception) {
            // Ignore errors in test mode
        }
        _loginState.value = LoginState.Initial
    }
} 