package com.mtl.My_Hack_X.data.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.mtl.My_Hack_X.MyHackXApp
import com.mtl.My_Hack_X.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseService {
    private val auth by lazy { 
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }
    
    private val firestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }
    
    private val storage by lazy {
        try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            null
        }
    }
    
    private val functions by lazy {
        try {
            Firebase.functions
        } catch (e: Exception) {
            null
        }
    }

    private val usersCollection by lazy { firestore?.collection("users") }
    private val eventsCollection by lazy { firestore?.collection("events") }
    private val teamsCollection by lazy { firestore?.collection("teams") }
    private val invitationsCollection by lazy { firestore?.collection("team_invitations") }
    private val notificationsCollection by lazy { firestore?.collection("notifications") }

    // Authentication
    suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        if (MyHackXApp.isTestMode || auth == null) {
            // Return a test user
            return@runCatching User(
                uid = "test_user_id",
                email = "test@example.com",
                displayName = "Test User",
                photoUrl = ""
            )
        }
        
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth!!.signInWithCredential(credential).await()
        val firebaseUser = authResult.user!!
        
        val user = User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            photoUrl = firebaseUser.photoUrl?.toString() ?: ""
        )
        
        usersCollection?.document(user.uid)?.set(user)?.await()
        user
    }
    
    suspend fun signInWithEmail(email: String, password: String): Result<User> = runCatching {
        if (MyHackXApp.isTestMode || auth == null) {
            // Return a test user
            if (email == "test@example.com" && password == "password") {
                return@runCatching User(
                    uid = "test_user_id",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = ""
                )
            } else {
                throw Exception("Invalid email or password")
            }
        }
        
        val authResult = auth!!.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user!!
        
        // Get or create user document
        val userDoc = usersCollection?.document(firebaseUser.uid)?.get()?.await()
        if (userDoc == null || !userDoc.exists()) {
            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: email.substringBefore("@"),
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )
            usersCollection?.document(user.uid)?.set(user)?.await()
            user
        } else {
            userDoc.toObject(User::class.java)!!
        }
    }
    
    suspend fun registerWithEmail(email: String, password: String): Result<User> = runCatching {
        if (MyHackXApp.isTestMode || auth == null) {
            // Return a test user
            return@runCatching User(
                uid = "test_user_id",
                email = email,
                displayName = email.substringBefore("@"),
                photoUrl = ""
            )
        }
        
        val authResult = auth!!.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user!!
        
        val user = User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = email.substringBefore("@"),
            photoUrl = ""
        )
        
        usersCollection?.document(user.uid)?.set(user)?.await()
        user
    }
    
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        if (MyHackXApp.isTestMode || auth == null) {
            // Simulate success in test mode
            return@runCatching Unit
        }
        
        auth!!.sendPasswordResetEmail(email).await()
    }

    fun signOut() {
        if (!MyHackXApp.isTestMode && auth != null) {
            auth!!.signOut()
        }
    }

    // User Operations
    suspend fun getCurrentUser(): User? {
        if (MyHackXApp.isTestMode || auth == null) {
            return null
        }
        val firebaseUser = auth!!.currentUser ?: return null
        return usersCollection?.document(firebaseUser.uid)?.get()?.await()?.toObject(User::class.java)
    }

    fun getAllUsers(): Flow<List<User>> = flow {
        if (MyHackXApp.isTestMode || firestore == null) {
            emit(emptyList())
            return@flow
        }
        val snapshot = usersCollection?.get()?.await()
        val users = snapshot?.toObjects(User::class.java) ?: emptyList()
        emit(users)
    }

    suspend fun updateUser(user: User): Result<Unit> = runCatching {
        if (MyHackXApp.isTestMode || firestore == null) {
            throw Exception("Firebase services are not initialized")
        }
        usersCollection?.document(user.uid)?.set(user)?.await()
    }

    suspend fun deleteUser(userId: String): Result<Unit> = runCatching {
        if (MyHackXApp.isTestMode || firestore == null) {
            throw Exception("Firebase services are not initialized")
        }
        usersCollection?.document(userId)?.delete()?.await()
    }

    // Event Operations
    suspend fun createEvent(event: HackathonEvent): Result<String> = runCatching {
        if (MyHackXApp.isTestMode || eventsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        val documentRef = eventsCollection?.add(event)?.await()
        documentRef?.id ?: throw Exception("Event creation failed")
    }

    suspend fun updateEvent(event: HackathonEvent): Result<Unit> = runCatching {
        if (MyHackXApp.isTestMode || eventsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        eventsCollection?.document(event.id)?.set(event)?.await()
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> = runCatching {
        if (MyHackXApp.isTestMode || eventsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        eventsCollection?.document(eventId)?.delete()?.await()
    }

    fun getEvents(filters: Map<String, Any> = emptyMap()): Flow<List<HackathonEvent>> = flow {
        if (MyHackXApp.isTestMode || eventsCollection == null) {
            emit(emptyList())
            return@flow
        }
        var query: Query = eventsCollection!!
        filters.forEach { (field, value) ->
            query = query.whereEqualTo(field, value)
        }
        val snapshot = query.get()?.await()
        val events = snapshot?.toObjects(HackathonEvent::class.java) ?: emptyList()
        
        // For each event, fetch its teams and add them to teamObjects
        val eventsWithTeams = events.map { event ->
            val teamsList = mutableListOf<Team>()
            try {
                for (teamId in event.teams) {
                    val teamDoc = teamsCollection?.document(teamId)?.get()?.await()
                    val team = teamDoc?.toObject(Team::class.java)
                    if (team != null) {
                        teamsList.add(team.copy(id = teamDoc.id))
                    }
                }
                event.copy(teamObjects = teamsList)
            } catch (e: Exception) {
                // If there's an error fetching teams, just return the event without teams
                event
            }
        }
        
        emit(eventsWithTeams)
    }

    suspend fun getEventById(eventId: String): Result<HackathonEvent?> = runCatching {
        if (MyHackXApp.isTestMode || eventsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        eventsCollection?.document(eventId)?.get()?.await()?.toObject(HackathonEvent::class.java)
    }

    // Team Operations
    suspend fun createTeam(team: Team, eventId: String): Result<String> = runCatching {
        if (MyHackXApp.isTestMode || teamsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        val documentRef = teamsCollection?.add(team)?.await()
        val teamId = documentRef?.id ?: throw Exception("Team creation failed")
        
        // Update event with new team ID
        eventsCollection?.document(eventId)
            ?.update("teams", com.google.firebase.firestore.FieldValue.arrayUnion(teamId))
            ?.await()
        
        teamId
    }

    suspend fun updateTeam(team: Team, eventId: String): Result<Unit> = runCatching {
        if (MyHackXApp.isTestMode || teamsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        teamsCollection?.document(team.id)?.set(team)?.await()
    }

    suspend fun deleteTeam(teamId: String, eventId: String): Result<Unit> = runCatching {
        if (MyHackXApp.isTestMode || teamsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        teamsCollection?.document(teamId)?.delete()?.await()
        
        // Remove team ID from event
        eventsCollection?.document(eventId)
            ?.update("teams", com.google.firebase.firestore.FieldValue.arrayRemove(teamId))
            ?.await()
    }

    // Registration Operations
    suspend fun registerForEvent(eventId: String, userId: String, teamName: String = ""): Result<Unit> = runCatching {
        if (MyHackXApp.isTestMode || eventsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        val event = eventsCollection?.document(eventId)?.get()?.await()?.toObject(HackathonEvent::class.java)
            ?: throw Exception("Event not found")
        
        val team = Team(
            name = teamName.ifBlank { "Individual-$userId" },
            eventId = eventId,
            leaderId = userId,
            members = listOf(TeamMember(
                uid = userId,
                email = auth?.currentUser?.email ?: "",
                role = TeamRole.LEADER
            )),
            status = TeamStatus.FORMING
        )
        createTeam(team, eventId)
    }

    suspend fun joinTeam(teamId: String, eventId: String, userId: String): Result<Unit> = runCatching {
        if (MyHackXApp.isTestMode || teamsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        val team = teamsCollection?.document(teamId)?.get()?.await()?.toObject(Team::class.java)
            ?: throw Exception("Team not found")
        
        val event = eventsCollection?.document(eventId)?.get()?.await()?.toObject(HackathonEvent::class.java)
            ?: throw Exception("Event not found")
        
        if (team.members.size >= event.maxTeamSize) {
            throw Exception("Team is full")
        }
        
        val updatedTeam = team.copy(
            members = team.members + TeamMember(
                uid = userId,
                email = auth?.currentUser?.email ?: "",
                role = TeamRole.MEMBER
            )
        )
        
        updateTeam(updatedTeam, eventId)
    }

    suspend fun leaveTeam(teamId: String, eventId: String, userId: String): Result<Unit> = runCatching {
        if (MyHackXApp.isTestMode || teamsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        val team = teamsCollection?.document(teamId)?.get()?.await()?.toObject(Team::class.java)
            ?: throw Exception("Team not found")
        
        val updatedTeam = team.copy(
            members = team.members.filter { it.uid != userId }
        )
        
        if (updatedTeam.members.isEmpty()) {
            deleteTeam(teamId, eventId)
        } else {
            updateTeam(updatedTeam, eventId)
        }
    }

    suspend fun registerTeamForEvent(
        eventId: String,
        teamName: String,
        memberEmails: List<String>,
        leaderId: String
    ) {
        if (MyHackXApp.isTestMode || teamsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        // Create team document
        val teamRef = teamsCollection?.document()
        val team = Team(
            id = teamRef?.id ?: throw Exception("Team ID not generated"),
            name = teamName,
            eventId = eventId,
            leaderId = leaderId,
            members = listOf(TeamMember(
                uid = leaderId,
                email = auth?.currentUser?.email ?: "",
                role = TeamRole.LEADER
            )),
            status = TeamStatus.FORMING
        )
        
        // Save team
        teamRef?.set(team)?.await()

        // Send invitations to team members
        memberEmails.filter { it != auth?.currentUser?.email }.forEach { email ->
            val invitation = TeamInvitation(
                teamId = teamRef?.id ?: throw Exception("Team ID not available"),
                eventId = eventId,
                teamName = teamName,
                inviterEmail = auth?.currentUser?.email ?: "",
                inviteeEmail = email,
                status = InvitationStatus.PENDING
            )
            invitationsCollection?.add(invitation)?.await()

            // Create in-app notification for the invitation
            createNotification(
                recipientEmail = email,
                title = "Team Invitation",
                message = "${auth?.currentUser?.email ?: "Someone"} has invited you to join team $teamName",
                type = NotificationType.TEAM_INVITATION,
                data = mapOf(
                    "teamId" to (teamRef?.id ?: ""),
                    "eventId" to eventId
                )
            )
        }

        // Update event with new team ID
        eventsCollection?.document(eventId)
            ?.update("teams", com.google.firebase.firestore.FieldValue.arrayUnion(teamRef?.id))
            ?.await()
    }

    suspend fun acceptTeamInvitation(invitationId: String) {
        if (MyHackXApp.isTestMode || invitationsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        val invitation = invitationsCollection?.document(invitationId)?.get()?.await()?.toObject(TeamInvitation::class.java)
            ?: throw Exception("Invitation not found")

        if (invitation.status != InvitationStatus.PENDING) {
            throw Exception("Invitation is no longer pending")
        }

        if (MyHackXApp.isTestMode || auth == null) {
            throw Exception("User not authenticated")
        }
        
        val currentUser = auth!!.currentUser ?: throw Exception("User not authenticated")
        
        // Add member to team
        val teamMember = TeamMember(
            uid = currentUser.uid,
            email = currentUser.email ?: "",
            role = TeamRole.MEMBER
        )

        teamsCollection?.document(invitation.teamId)?.update("members", com.google.firebase.firestore.FieldValue.arrayUnion(teamMember))?.await()

        // Update invitation status
        invitationsCollection?.document(invitationId)?.update("status", InvitationStatus.ACCEPTED)?.await()

        // Create notification for team leader
        val team = teamsCollection?.document(invitation.teamId)?.get()?.await()?.toObject(Team::class.java)
            ?: throw Exception("Team not found")

        createNotification(
            recipientEmail = team.members.first { it.role == TeamRole.LEADER }.email,
            title = "New Team Member",
            message = "${currentUser.email} has joined your team ${team.name}",
            type = NotificationType.MEMBER_JOINED,
            data = mapOf(
                "teamId" to team.id,
                "eventId" to team.eventId
            )
        )
    }

    suspend fun declineTeamInvitation(invitationId: String) {
        if (MyHackXApp.isTestMode || invitationsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        val invitation = invitationsCollection?.document(invitationId)?.get()?.await()?.toObject(TeamInvitation::class.java)
            ?: throw Exception("Invitation not found")

        invitationsCollection?.document(invitationId)?.update("status", InvitationStatus.DECLINED)?.await()

        // Create notification for team leader
        createNotification(
            recipientEmail = invitation.inviterEmail,
            title = "Invitation Declined",
            message = "${invitation.inviteeEmail} has declined to join team ${invitation.teamName}",
            type = NotificationType.INVITATION_DECLINED,
            data = mapOf(
                "teamId" to invitation.teamId,
                "eventId" to invitation.eventId
            )
        )
    }

    suspend fun removeTeamMember(teamId: String, memberUid: String) {
        if (MyHackXApp.isTestMode || teamsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        val team = teamsCollection?.document(teamId)?.get()?.await()?.toObject(Team::class.java)
            ?: throw Exception("Team not found")

        if (team.leaderId != auth?.currentUser?.uid) {
            throw Exception("Only team leader can remove members")
        }

        val memberToRemove = team.members.find { it.uid == memberUid }
            ?: throw Exception("Member not found")

        teamsCollection?.document(teamId)?.update("members", com.google.firebase.firestore.FieldValue.arrayRemove(memberToRemove))?.await()

        // Create notification for removed member
        createNotification(
            recipientEmail = memberToRemove.email,
            title = "Team Membership Update",
            message = "You have been removed from team ${team.name}",
            type = NotificationType.MEMBER_REMOVED,
            data = mapOf(
                "teamId" to team.id,
                "eventId" to team.eventId
            )
        )
    }

    private suspend fun createNotification(
        recipientEmail: String,
        title: String,
        message: String,
        type: NotificationType,
        data: Map<String, String>
    ) {
        if (MyHackXApp.isTestMode || notificationsCollection == null) {
            return // Just return in test mode instead of throwing an exception
        }
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            recipientEmail = recipientEmail,
            title = title,
            message = message,
            type = type,
            data = data,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationsCollection?.document(notification.id)?.set(notification)?.await()
    }

    suspend fun getPendingInvitations(userEmail: String): List<TeamInvitation> {
        if (MyHackXApp.isTestMode || invitationsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        return invitationsCollection?.whereEqualTo("inviteeEmail", userEmail)
            ?.whereEqualTo("status", InvitationStatus.PENDING)
            ?.get()
            ?.await()
            ?.toObjects(TeamInvitation::class.java)
            ?: throw Exception("Invitations not found")
    }

    suspend fun getTeamDetails(teamId: String): Team {
        if (MyHackXApp.isTestMode || teamsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        return teamsCollection?.document(teamId)?.get()?.await()?.toObject(Team::class.java)
            ?: throw Exception("Team not found")
    }

    suspend fun getUserNotifications(userEmail: String): List<Notification> {
        if (MyHackXApp.isTestMode || notificationsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        return notificationsCollection?.whereEqualTo("recipientEmail", userEmail)
            ?.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            ?.get()
            ?.await()
            ?.toObjects(Notification::class.java)
            ?: throw Exception("Notifications not found")
    }

    suspend fun markNotificationAsRead(notificationId: String) {
        if (MyHackXApp.isTestMode || notificationsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        notificationsCollection?.document(notificationId)?.update("isRead", true)?.await()
    }

    suspend fun createNotification(notification: Notification) {
        if (MyHackXApp.isTestMode || notificationsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        notificationsCollection?.document(notification.id)?.set(notification)?.await()
    }

    /**
     * Gets a team by its ID
     */
    suspend fun getTeamById(teamId: String): Result<Team?> = runCatching {
        if (MyHackXApp.isTestMode || teamsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        val teamDoc = teamsCollection?.document(teamId)?.get()?.await()
        teamDoc?.toObject(Team::class.java)?.copy(id = teamDoc.id)
    }

    /**
     * Unregisters a user from an event by finding and removing their team
     */
    suspend fun unregisterFromEvent(eventId: String, userId: String): Result<Unit> = runCatching {
        if (MyHackXApp.isTestMode || teamsCollection == null) {
            throw Exception("Firebase services are not initialized")
        }
        // Find all teams for this event
        val teamSnapshots = teamsCollection?.whereEqualTo("eventId", eventId)?.get()?.await()
        
        // Find the team that contains this user
        val userTeam = teamSnapshots?.documents
            ?.mapNotNull { doc -> 
                doc.toObject(Team::class.java)?.copy(id = doc.id)
            }
            ?.find { team -> 
                team.members.any { member -> member.uid == userId }
            }
        
        // If team found, handle the unregistration
        if (userTeam != null) {
            if (userTeam.leaderId == userId && userTeam.members.size > 1) {
                // If leader is leaving but there are other members, reassign leadership
                val newLeader = userTeam.members.first { it.uid != userId }
                val updatedMembers = userTeam.members
                    .filter { it.uid != userId }
                    .map { 
                        if (it.uid == newLeader.uid) 
                            it.copy(role = TeamRole.LEADER) 
                        else 
                            it 
                    }
                
                teamsCollection?.document(userTeam.id)?.update(
                    "members", updatedMembers,
                    "leaderId", newLeader.uid
                )?.await()
            } else if (userTeam.members.size <= 1) {
                // If last member is leaving, delete the team
                deleteTeam(userTeam.id, eventId)
            } else {
                // Just remove the member
                leaveTeam(userTeam.id, eventId, userId)
            }
        }
    }
} 