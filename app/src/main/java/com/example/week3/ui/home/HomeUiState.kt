package com.example.week3.ui.home

import androidx.compose.runtime.Immutable
import com.example.week3.data.SmartDevice

// UI'ın ihtiyaç duyduğu her şey hazır hesaplanmış halde gelir; Compose sadece çizer
@Immutable
data class HomeUiState(
    val devices: List<SmartDevice> = emptyList(),
    val rooms: List<String> = emptyList(),
    val selectedRoom: String? = null,
    val totalPowerWatts: Double = 0.0,
    val totalPowerText: String = "0.0 W",
    val activeDeviceCount: Int = 0
)
