package com.example.week3.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

// Ham veri seti (ödev PDF'i)
val sampleDevices = listOf(
    SmartDevice(1, "Köşe Lambası", DeviceCategory.LIGHT, "Salon", isOnline = true, isActive = true, powerWatts = 15.0),
    SmartDevice(2, "Tavan Avizesi", DeviceCategory.LIGHT, "Salon", isOnline = true, isActive = false, powerWatts = 60.0),
    SmartDevice(3, "Bahçe Kamerası", DeviceCategory.CAMERA, "Bahçe", isOnline = true, isActive = true, powerWatts = 8.5),
    SmartDevice(4, "Giriş Kamerası", DeviceCategory.CAMERA, "Antre", isOnline = true, isActive = true, powerWatts = 8.5),
    SmartDevice(5, "Merkezi Termostat", DeviceCategory.THERMOSTAT, "Salon", isOnline = true, isActive = true, powerWatts = 12.0),
    SmartDevice(6, "Yatak Odası Kliması", DeviceCategory.THERMOSTAT, "Yatak Odası", isOnline = true, isActive = false, powerWatts = 1200.0),
    SmartDevice(7, "Mutfak Spotları", DeviceCategory.LIGHT, "Mutfak", isOnline = true, isActive = false, powerWatts = 45.0),
    SmartDevice(8, "Roborock S7", DeviceCategory.VACUUM, "Koridor", isOnline = true, isActive = true, powerWatts = 50.0),
    SmartDevice(9, "Balkon Aydınlatması", DeviceCategory.LIGHT, "Balkon", isOnline = false, isActive = false, powerWatts = 25.0),
    SmartDevice(10, "Çalışma Odası Lambası", DeviceCategory.LIGHT, "Ofis", isOnline = true, isActive = true, powerWatts = 12.0)
)

// Tek doğruluk kaynağı (single source of truth). Main-safe: ağır iş verilen dispatcher'da yapılır.
class DeviceRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val _devices = MutableStateFlow(sampleDevices)
    val devices: StateFlow<List<SmartDevice>> = _devices.asStateFlow()

    suspend fun toggleDevice(id: Int) = withContext(dispatcher) {
        // update {} atomik çalışır: aynı anda gelen tıklamalar birbirini ezmez
        _devices.update { devices ->
            devices.map { device ->
                if (device.id == id && device.isOnline) device.copy(isActive = !device.isActive) else device
            }
        }
    }
}
