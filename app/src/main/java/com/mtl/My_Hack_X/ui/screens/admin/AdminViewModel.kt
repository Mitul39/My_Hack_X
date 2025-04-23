package com.mtl.My_Hack_X.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtl.My_Hack_X.data.models.HackathonEvent
import com.mtl.My_Hack_X.data.models.User
import com.mtl.My_Hack_X.data.services.FirebaseService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AdminState {
    object Loading : AdminState()
    data class Success(
        val users: List<User> = emptyList(),
        val events: List<HackathonEvent> = emptyList()
    ) : AdminState()
    data class Error(val message: String) : AdminState()
}

class AdminViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val _adminState = MutableStateFlow<AdminState>(AdminState.Loading)
    val adminState: StateFlow<AdminState> = _adminState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading
            try {
                combine(
                    firebaseService.getAllUsers(),
                    firebaseService.getEvents()
                ) { users, events ->
                    AdminState.Success(users, events)
                }.catch { e ->
                    _adminState.value = AdminState.Error(e.message ?: "Failed to load data")
                }.collect { state ->
                    _adminState.value = state
                }
            } catch (e: Exception) {
                _adminState.value = AdminState.Error(e.message ?: "Failed to load data")
            }
        }
    }

    fun createEvent(event: HackathonEvent) {
        viewModelScope.launch {
            try {
                firebaseService.createEvent(event)
                loadData()
            } catch (e: Exception) {
                _adminState.value = AdminState.Error(e.message ?: "Failed to create event")
            }
        }
    }

    fun updateEvent(event: HackathonEvent) {
        viewModelScope.launch {
            try {
                firebaseService.updateEvent(event)
                loadData()
            } catch (e: Exception) {
                _adminState.value = AdminState.Error(e.message ?: "Failed to update event")
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                firebaseService.deleteEvent(eventId)
                loadData()
            } catch (e: Exception) {
                _adminState.value = AdminState.Error(e.message ?: "Failed to delete event")
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                firebaseService.updateUser(user)
                loadData()
            } catch (e: Exception) {
                _adminState.value = AdminState.Error(e.message ?: "Failed to update user")
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                firebaseService.deleteUser(userId)
                loadData()
            } catch (e: Exception) {
                _adminState.value = AdminState.Error(e.message ?: "Failed to delete user")
            }
        }
    }
} 