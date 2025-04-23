package com.mtl.My_Hack_X.data.models

import java.util.Date

data class EventFilter(
    val status: EventStatus? = null,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val location: String? = null,
    val tags: List<String> = emptyList(),
    val maxTeamSize: Int? = null
) 