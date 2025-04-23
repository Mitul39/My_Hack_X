package com.mtl.My_Hack_X.data.models

import com.google.firebase.Timestamp
import java.util.Date
import com.mtl.My_Hack_X.data.models.RegistrationType

enum class EventStatus {
    UPCOMING, ONGOING, COMPLETED, CANCELLED
}

data class Prize(
    val name: String = "",
    val description: String = "",
    val value: String = ""
)

data class HackathonEvent(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val registrationDeadline: Date = Date(),
    val maxTeamSize: Int = 5,
    val minTeamSize: Int = 1,
    val status: String = "UPCOMING",
    val tags: List<String> = emptyList(),
    val organizerId: String = "",
    val maxParticipants: Int = 100,
    val currentParticipants: Int = 0,
    val prizes: List<String> = emptyList(),
    val teams: List<String> = emptyList(),
    val imageUrl: String = "",
    val teamObjects: List<Team> = emptyList()
) {
    fun getTeamsSafely(): List<String> {
        return teams ?: emptyList()
    }
    
    fun getTeamObjectsSafely(): List<Team> {
        return teamObjects ?: emptyList()
    }
    
    fun getTagsSafely(): List<String> {
        return tags ?: emptyList()
    }
} 