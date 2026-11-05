package com.example.week3.data

import androidx.compose.runtime.Immutable

enum class DeviceCategory(val label: String, val emoji: String) {
    LIGHT("Aydınlatma", "💡"),
    CAMERA("Kamera", "📷"),
    THERMOSTAT("Termostat", "🌡️"),
    VACUUM("Süpürge", "🧹")
}

// Tüm alanlar val: durum değişimi sadece copy() ile yeni nesne üretilerek yapılır
@Immutable
data class SmartDevice(
    val id: Int,
    val name: String,
    val category: DeviceCategory,
    val room: String,
    val isOnline: Boolean,
    val isActive: Boolean,
    val powerWatts: Double
)
