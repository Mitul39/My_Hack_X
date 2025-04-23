package com.mtl.My_Hack_X.ui.screens.teamdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtl.My_Hack_X.MyHackXApp
import com.mtl.My_Hack_X.data.models.Team
import com.mtl.My_Hack_X.data.models.TeamMember
import com.mtl.My_Hack_X.data.models.TeamRole
import com.mtl.My_Hack_X.data.models.TeamStatus
import com.mtl.My_Hack_X.data.services.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeamDetailsUiState(
    val isLoading: Boolean = false,
    val team: Team? = null,
    val error: String? = null
)

class TeamDetailsViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val _uiState = MutableStateFlow(TeamDetailsUiState(isLoading = true))
    val uiState: StateFlow<TeamDetailsUiState> = _uiState.asStateFlow()
    
    private var currentTeamId: String? = null
    private var currentEventId: String? = null
    
    fun loadTeamDetails(teamId: String) {
        currentTeamId = teamId
        
        if (MyHackXApp.isTestMode) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    team = generateTestTeam(teamId),
                    error = null
                )
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val teamResult = firebaseService.getTeamById(teamId)
                teamResult.onSuccess { team ->
                    if (team != null) {
                        currentEventId = team.eventId
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                team = team,
                                error = null
                            )
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Team not found"
                            )
                        }
                    }
                }.onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load team details"
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
    
    fun leaveTeam() {
        // This would normally handle the team leaving logic
        // For now, we'll just show a success message
        _uiState.update { 
            it.copy(
                team = it.team?.copy(
                    members = it.team.members.dropLast(1)
                )
            )
        }
    }
    
    private fun generateTestTeam(teamId: String): Team {
        return Team(
            id = teamId,
            name = "Test Team",
            eventId = "event_1",
            leaderId = "user_1",
            members = listOf(
                TeamMember(
                    uid = "user_1",
                    email = "team_leader@example.com",
                    role = TeamRole.LEADER
                ),
                TeamMember(
                    uid = "user_2",
                    email = "member1@example.com",
                    role = TeamRole.MEMBER
                ),
                TeamMember(
                    uid = "user_3",
                    email = "member2@example.com",
                    role = TeamRole.MEMBER
                )
            ),
            status = TeamStatus.FORMING
        )
    }
}