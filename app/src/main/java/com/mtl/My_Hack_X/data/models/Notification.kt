package com.mtl.My_Hack_X.data.models

data class Notification(
    val id: String = "",
    val recipientEmail: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val data: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

enum class NotificationType {
    GENERAL,
    TEAM_INVITATION,
    MEMBER_JOINED,
    MEMBER_REMOVED,
    INVITATION_DECLINED,
    EVENT_REMINDER
} 