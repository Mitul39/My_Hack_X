package com.mtl.My_Hack_X.data.models

data class Team(
    val id: String = "",
    val name: String = "",
    val eventId: String = "",
    val leaderId: String = "",
    val members: List<TeamMember> = emptyList(),
    val status: TeamStatus = TeamStatus.FORMING
) {
    val memberIds: List<String>
        get() = members.map { it.uid }
}

data class TeamMember(
    val uid: String = "",
    val email: String = "",
    val role: TeamRole = TeamRole.MEMBER
)

enum class TeamRole {
    LEADER, MEMBER
}

enum class TeamStatus {
    FORMING, COMPLETE, DISBANDED
}

data class TeamInvitation(
    val id: String = "",
    val teamId: String = "",
    val eventId: String = "",
    val teamName: String = "",
    val inviterEmail: String = "",
    val inviteeEmail: String = "",
    val status: InvitationStatus = InvitationStatus.PENDING
)

enum class InvitationStatus {
    PENDING, ACCEPTED, DECLINED
} 