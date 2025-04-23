package com.mtl.My_Hack_X.data.models

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val isAdmin: Boolean = false,
    val skills: List<String> = emptyList(),
    val bio: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
)
