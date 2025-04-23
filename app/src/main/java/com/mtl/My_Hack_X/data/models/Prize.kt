package com.mtl.My_Hack_X.data.models

data class Prize(
    val title: String = "",
    val description: String = "",
    val value: Double = 0.0,
    val position: Int = 1 // Added for backward compatibility
) 