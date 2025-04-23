package com.mtl.My_Hack_X.data.models

sealed class RegistrationData {
    data class IndividualRegistrationData(
        val eventId: String,
        val userId: String
    ) : RegistrationData()

    data class TeamRegistrationData(
        val eventId: String,
        val teamName: String,
        val memberEmails: List<String>,
        val leaderId: String
    ) : RegistrationData()
}