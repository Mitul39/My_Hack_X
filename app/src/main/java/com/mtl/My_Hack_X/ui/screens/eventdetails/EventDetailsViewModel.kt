package com.mtl.My_Hack_X.ui.screens.eventdetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtl.My_Hack_X.MyHackXApp
import com.mtl.My_Hack_X.data.models.HackathonEvent
import com.mtl.My_Hack_X.data.models.Team
import com.mtl.My_Hack_X.data.services.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class EventDetailsUiState(
    val isLoading: Boolean = false,
    val event: HackathonEvent? = null,
    val error: String? = null,
    val isRegistered: Boolean = false
)

class EventDetailsViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val _uiState = MutableStateFlow(EventDetailsUiState(isLoading = true))
    val uiState: StateFlow<EventDetailsUiState> = _uiState.asStateFlow()
    
    private var currentEventId: String? = null
    
    fun loadEventDetails(eventId: String) {
        currentEventId = eventId
        
        if (MyHackXApp.isTestMode) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    event = generateTestEvent(eventId),
                    error = null
                )
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val eventResult = firebaseService.getEventById(eventId)
                eventResult.onSuccess { event ->
                    if (event != null) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                event = event,
                                error = null
                            )
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Event not found"
                            )
                        }
                    }
                }.onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load event details"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }
    
    fun registerForEvent() {
        // This would normally handle the registration logic
        // For now, we'll just update the UI state to show registered
        _uiState.update { it.copy(isRegistered = true) }
    }
    
    private fun generateTestEvent(eventId: String): HackathonEvent {
        return HackathonEvent(
            id = eventId,
            name = "Test Hackathon Event",
            description = "This is a test event for demonstration purposes. It showcases the event details screen.",
            location = "Virtual / Online",
            startDate = Date(),
            endDate = Date(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000), // 2 days from now
            registrationDeadline = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000), // 1 day from now
            maxTeamSize = 4,
            minTeamSize = 1,
            status = "UPCOMING",
            tags = listOf("AI", "Machine Learning", "Web Development"),
            organizerId = "admin_user_id",
            maxParticipants = 100,
            currentParticipants = 42,
            prizes = listOf("$5000 for 1st Place", "$2000 for 2nd Place", "$1000 for 3rd Place"),
            teams = listOf(),
            imageUrl = ""
        )
    }
} 