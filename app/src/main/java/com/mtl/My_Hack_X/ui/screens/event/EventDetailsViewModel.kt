package com.mtl.My_Hack_X.ui.screens.event

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mtl.My_Hack_X.data.models.HackathonEvent
import com.mtl.My_Hack_X.data.models.Team
import com.mtl.My_Hack_X.data.models.EventStatus
import com.mtl.My_Hack_X.data.services.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

sealed class EventDetailsState {
    object Loading : EventDetailsState()
    data class Success(
        val event: HackathonEvent,
        val teams: List<Team> = emptyList(),
        val isRegistered: Boolean = false,
        val currentUserTeamId: String? = null,
        val canJoinTeam: Boolean = false,
        val canRegister: Boolean = false,
        val eventImage: String? = null
    ) : EventDetailsState()
    data class Error(val message: String) : EventDetailsState()
}

class EventDetailsViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val eventId: String = checkNotNull(savedStateHandle["eventId"])
    private val firebaseService = FirebaseService()
    private val _state = MutableStateFlow<EventDetailsState>(EventDetailsState.Loading)
    val state: StateFlow<EventDetailsState> = _state.asStateFlow()

    init {
        loadEventDetails()
    }

    fun loadEventDetails() {
        viewModelScope.launch {
            _state.value = EventDetailsState.Loading
            try {
                // Get event details
                val eventResult = firebaseService.getEventById(eventId)
                
                eventResult.onSuccess { event ->
                    if (event != null) {
                        // Get current user ID
                        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        
                        // Fetch teams manually since there's no direct method
                        try {
                            // Get all team IDs for this event
                            val teamIds = event.teams
                            val fetchedTeams = mutableListOf<Team>()
                            
                            // Fetch each team
                            for (teamId in teamIds) {
                                firebaseService.getTeamById(teamId)
                                    .onSuccess { team ->
                                        if (team != null) {
                                            fetchedTeams.add(team)
                                        }
                                    }
                            }
                            
                            // Find user's team
                            val currentUserTeam = fetchedTeams.find { team ->
                                team.members.any { member -> member.uid == currentUserId }
                            }
                            
                            val isBeforeDeadline = event.registrationDeadline.after(Date())
                            val isEventNotStarted = event.status == EventStatus.UPCOMING
                            
                            _state.value = EventDetailsState.Success(
                                event = event,
                                teams = fetchedTeams,
                                isRegistered = currentUserTeam != null,
                                currentUserTeamId = currentUserTeam?.id,
                                canJoinTeam = currentUserTeam == null && isBeforeDeadline,
                                canRegister = currentUserTeam == null && isBeforeDeadline && isEventNotStarted,
                                eventImage = null // Optional custom image URL if you have one
                            )
                        } catch (e: Exception) {
                            _state.value = EventDetailsState.Error(e.message ?: "Failed to load teams")
                        }
                    } else {
                        _state.value = EventDetailsState.Error("Event not found")
                    }
                }.onFailure { e ->
                    _state.value = EventDetailsState.Error(e.message ?: "Failed to load event")
                }
            } catch (e: Exception) {
                _state.value = EventDetailsState.Error(e.message ?: "Failed to load event")
            }
        }
    }

    fun registerForEvent(teamName: String = "") {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            try {
                firebaseService.registerForEvent(eventId, currentUserId, teamName)
                    .onSuccess {
                        loadEventDetails()
                    }
                    .onFailure { e ->
                        _state.value = EventDetailsState.Error(e.message ?: "Failed to register")
                    }
            } catch (e: Exception) {
                _state.value = EventDetailsState.Error(e.message ?: "Failed to register")
            }
        }
    }

    fun joinTeam(teamId: String) {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            try {
                firebaseService.joinTeam(teamId, eventId, currentUserId)
                    .onSuccess {
                        loadEventDetails()
                    }
                    .onFailure { e ->
                        _state.value = EventDetailsState.Error(e.message ?: "Failed to join team")
                    }
            } catch (e: Exception) {
                _state.value = EventDetailsState.Error(e.message ?: "Failed to join team")
            }
        }
    }

    fun leaveEvent() {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val currentState = _state.value as? EventDetailsState.Success ?: return@launch
            val teamId = currentState.currentUserTeamId ?: return@launch

            try {
                firebaseService.leaveTeam(teamId, eventId, currentUserId)
                    .onSuccess {
                        loadEventDetails()
                    }
                    .onFailure { e ->
                        _state.value = EventDetailsState.Error(e.message ?: "Failed to leave event")
                    }
            } catch (e: Exception) {
                _state.value = EventDetailsState.Error(e.message ?: "Failed to leave event")
            }
        }
    }
} 