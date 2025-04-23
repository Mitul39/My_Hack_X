package com.mtl.My_Hack_X.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtl.My_Hack_X.MyHackXApp
import com.mtl.My_Hack_X.data.models.Notification
import com.mtl.My_Hack_X.data.models.NotificationType
import com.mtl.My_Hack_X.data.services.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val error: String? = null
)

class NotificationsViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val _uiState = MutableStateFlow(NotificationsUiState(isLoading = true))
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    init {
        loadNotifications()
    }
    
    fun loadNotifications() {
        if (MyHackXApp.isTestMode) {
            val testNotifications = generateTestNotifications()
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    notifications = testNotifications,
                    error = null
                )
            }
            _unreadCount.value = testNotifications.count { !it.isRead }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val notifications = firebaseService.getUserNotifications(currentUser.email ?: "")
                    _unreadCount.value = notifications.count { !it.isRead }
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            notifications = notifications,
                            error = null
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            notifications = emptyList(),
                            error = "User not signed in"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load notifications"
                    )
                }
            }
        }
    }
    
    fun markAsRead(notificationId: String) {
        if (MyHackXApp.isTestMode) {
            // Update local state for test mode
            _uiState.update { currentState ->
                val updatedNotifications = currentState.notifications.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true)
                    } else {
                        notification
                    }
                }
                _unreadCount.value = updatedNotifications.count { !it.isRead }
                currentState.copy(notifications = updatedNotifications)
            }
            return
        }
        
        viewModelScope.launch {
            try {
                firebaseService.markNotificationAsRead(notificationId)
                
                // Update local state
                _uiState.update { currentState ->
                    val updatedNotifications = currentState.notifications.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                    _unreadCount.value = updatedNotifications.count { !it.isRead }
                    currentState.copy(notifications = updatedNotifications)
                }
            } catch (e: Exception) {
                // Silently fail but don't update UI state
            }
        }
    }
    
    private fun generateTestNotifications(): List<Notification> {
        return listOf(
            Notification(
                id = UUID.randomUUID().toString(),
                recipientEmail = "test@example.com",
                title = "Welcome to My HackX",
                message = "Thank you for joining our platform. Explore events and join hackathons today!",
                type = NotificationType.GENERAL,
                timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                isRead = false
            ),
            Notification(
                id = UUID.randomUUID().toString(),
                recipientEmail = "test@example.com",
                title = "Team Invitation",
                message = "John Doe has invited you to join their team 'Code Wizards' for the upcoming hackathon.",
                type = NotificationType.TEAM_INVITATION,
                data = mapOf(
                    "teamId" to "team123",
                    "eventId" to "event456"
                ),
                timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
                isRead = false
            ),
            Notification(
                id = UUID.randomUUID().toString(),
                recipientEmail = "test@example.com",
                title = "Event Reminder",
                message = "The 'Summer Code Fest' hackathon starts in 2 days. Don't forget to prepare!",
                type = NotificationType.EVENT_REMINDER,
                data = mapOf("eventId" to "event789"),
                timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
                isRead = true
            )
        )
    }
} 