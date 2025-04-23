package com.mtl.My_Hack_X.ui.screens.events

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtl.My_Hack_X.MyHackXApp
import com.mtl.My_Hack_X.data.models.HackathonEvent
import com.mtl.My_Hack_X.data.models.EventStatus
import com.mtl.My_Hack_X.data.models.RegistrationData
import com.mtl.My_Hack_X.data.models.RegistrationData.IndividualRegistrationData
import com.mtl.My_Hack_X.data.models.RegistrationData.TeamRegistrationData
import com.mtl.My_Hack_X.data.services.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class EventsUiState(
    val isLoading: Boolean = false,
    val events: List<HackathonEvent> = emptyList(),
    val error: String? = null
)

sealed class EventsState {
    object Loading : EventsState()
    data class Success(val events: List<HackathonEvent>) : EventsState()
    data class Error(val message: String) : EventsState()
}

enum class SortOption {
    DATE_ASC, DATE_DESC, POPULARITY, REGISTRATION_CLOSING
}

class EventsViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val _uiState = MutableStateFlow(EventsUiState(isLoading = true))
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()
    
    init {
        loadEvents()
    }
    
    fun loadEvents() {
        if (MyHackXApp.isTestMode) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    events = generateTestEvents(),
                    error = null
                )
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                firebaseService.getEvents().collect { events ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            events = events,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load events"
                    )
                }
            }
        }
    }
    
    private fun generateTestEvents(): List<HackathonEvent> {
        return listOf(
            HackathonEvent(
                id = "1",
                name = "Summer Hackathon 2023",
                description = "Join us for a weekend of coding and innovation!",
                location = "Online",
                startDate = Date(),
                endDate = Date(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000), // 2 days from now
                registrationDeadline = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000), // 1 day from now
                maxTeamSize = 4,
                minTeamSize = 1,
                status = "UPCOMING",
                tags = listOf("AI", "Mobile", "Web"),
                organizerId = "admin_user_id",
                maxParticipants = 100,
                currentParticipants = 42,
                prizes = listOf("$5000 for 1st Place", "$2000 for 2nd Place", "$1000 for 3rd Place"),
                teams = listOf(),
                imageUrl = ""
            ),
            HackathonEvent(
                id = "2",
                name = "AI Workshop Series",
                description = "Learn the latest in artificial intelligence and machine learning",
                location = "San Francisco",
                startDate = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000), // 7 days from now
                endDate = Date(System.currentTimeMillis() + 9 * 24 * 60 * 60 * 1000), // 9 days from now
                registrationDeadline = Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000), // 5 days from now
                maxTeamSize = 2,
                minTeamSize = 1,
                status = "UPCOMING",
                tags = listOf("AI", "ML", "Data Science"),
                organizerId = "admin_user_id",
                maxParticipants = 50,
                currentParticipants = 12,
                prizes = listOf("Certificates for all participants"),
                teams = listOf(),
                imageUrl = ""
            )
        )
    }

    private var currentEvents = listOf<HackathonEvent>()
    private var currentSearchQuery = ""
    private var currentSortOption = SortOption.DATE_DESC
    private var currentEventCategory: String? = null

    fun loadEvents(category: String? = null) {
        currentEventCategory = category
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                Log.d("EventsViewModel", "Loading events for category: $category")
                
                // Check if Firebase is initialized correctly
                if (firebaseService == null) {
                    Log.e("EventsViewModel", "Firebase service not initialized")
                    _uiState.update { it.copy(error = "Firebase service not initialized") }
                    return@launch
                }
                
                try {
                    // Use collect to handle the Flow from getEvents
                    firebaseService.getEvents().collect { events ->
                        Log.d("EventsViewModel", "Received ${events?.size ?: 0} events")
                        
                        if (events.isNullOrEmpty()) {
                            Log.d("EventsViewModel", "No events returned or null result")
                            currentEvents = emptyList()
                        } else {
                            try {
                                events.forEach { event ->
                                    Log.d("EventsViewModel", "Event: ${event.name}, teams: ${event.teams.size}, teamObjects: ${event.teamObjects?.size ?: 0}")
                                    event.teamObjects?.forEach { team ->
                                        Log.d("EventsViewModel", "   Team: ${team.name}, members: ${team.members?.size ?: 0}, leader: ${team.leaderId}")
                                    }
                                }
                                currentEvents = events
                            } catch (e: Exception) {
                                Log.e("EventsViewModel", "Error processing event details", e)
                                currentEvents = events.map { event ->
                                    // Create a safe version of the event with null checks
                                    try {
                                        event
                                    } catch (e: Exception) {
                                        // If there's an error with this event, return a minimal valid event
                                        Log.e("EventsViewModel", "Error with event ${event.id}", e)
                                        event.copy(teamObjects = emptyList())
                                    }
                                }
                            }
                        }
                        
                        try {
                            applyFiltersAndSort()
                        } catch (e: Exception) {
                            Log.e("EventsViewModel", "Error in applyFiltersAndSort", e)
                            _uiState.update { it.copy(events = currentEvents) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("EventsViewModel", "Error collecting events", e)
                    _uiState.update { it.copy(error = "Could not load events: ${e.message ?: "Unknown error"}") }
                }
            } catch (e: Exception) {
                Log.e("EventsViewModel", "Failed to initialize events", e)
                _uiState.update { it.copy(error = "Failed to initialize events: ${e.message ?: "Unknown error"}") }
            }
        }
    }

    fun setSearchQuery(query: String) {
        currentSearchQuery = query
        applyFiltersAndSort()
    }

    fun setSortOption(sortOption: SortOption) {
        currentSortOption = sortOption
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        try {
            var filteredEvents = currentEvents

            // Apply category filter if provided
            if (!currentEventCategory.isNullOrEmpty()) {
                filteredEvents = filteredEvents.filter { event ->
                    try {
                        event.tags.any { it.equals(currentEventCategory, ignoreCase = true) }
                    } catch (e: Exception) {
                        Log.e("EventsViewModel", "Error filtering by category for event ${event.id}", e)
                        false
                    }
                }
            }

            // Apply search filter
            if (currentSearchQuery.isNotBlank()) {
                filteredEvents = filteredEvents.filter { event ->
                    try {
                        event.name.contains(currentSearchQuery, ignoreCase = true) ||
                        event.description.contains(currentSearchQuery, ignoreCase = true) ||
                        event.location.contains(currentSearchQuery, ignoreCase = true) ||
                        event.tags.any { it.contains(currentSearchQuery, ignoreCase = true) }
                    } catch (e: Exception) {
                        Log.e("EventsViewModel", "Error applying search filter for event ${event.id}", e)
                        false
                    }
                }
            }

            // Apply sorting
            filteredEvents = try {
                when (currentSortOption) {
                    SortOption.DATE_ASC -> filteredEvents.sortedBy { it.startDate }
                    SortOption.DATE_DESC -> filteredEvents.sortedByDescending { it.startDate }
                    SortOption.POPULARITY -> filteredEvents.sortedByDescending { it.teams.size }
                    SortOption.REGISTRATION_CLOSING -> filteredEvents.sortedBy { it.registrationDeadline }
                }
            } catch (e: Exception) {
                Log.e("EventsViewModel", "Error sorting events", e)
                filteredEvents
            }

            _uiState.update { it.copy(events = filteredEvents) }
        } catch (e: Exception) {
            Log.e("EventsViewModel", "Error in applyFiltersAndSort", e)
            _uiState.update { it.copy(events = currentEvents) }
        }
    }

    fun handleRegistration(registrationData: RegistrationData) {
        viewModelScope.launch {
            try {
                when (registrationData) {
                    is IndividualRegistrationData -> {
                        firebaseService.registerForEvent(
                            registrationData.eventId,
                            registrationData.userId
                        )
                    }
                    is TeamRegistrationData -> {
                        firebaseService.registerTeamForEvent(
                            eventId = registrationData.eventId,
                            teamName = registrationData.teamName,
                            memberEmails = registrationData.memberEmails,
                            leaderId = registrationData.leaderId
                        )
                    }
                }
                // Refresh events after registration
                loadEvents(currentEventCategory)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to register for event") }
            }
        }
    }

    fun unregisterFromEvent(eventId: String, userId: String) {
        viewModelScope.launch {
            try {
                firebaseService.unregisterFromEvent(eventId, userId)
                // Refresh events after unregistration
                loadEvents(currentEventCategory)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to unregister from event") }
            }
        }
    }
} 