package com.example.week3.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.week3.data.DeviceRepository
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: DeviceRepository = DeviceRepository(),
    computeDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    // TextField'a gecikmesiz gitmesi için arama metni ayrı ve senkron tutulur
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val selectedRoom = MutableStateFlow<String?>(null)

    // Üç kaynaktan biri değişince filtre + toplam yeniden hesaplanır.
    // flowOn: hesaplama Main thread'i bloklamadan Default havuzunda yapılır.
    val uiState: StateFlow<HomeUiState> =
        combine(repository.devices, _searchQuery, selectedRoom) { devices, query, room ->
            val needle = query.trim()
            val poweredDevices = devices.filter { it.isOnline && it.isActive }
            val totalPower = poweredDevices.sumOf { it.powerWatts }

            HomeUiState(
                devices = devices.filter { device ->
                    (room == null || device.room == room) &&
                        device.name.contains(needle, ignoreCase = true)
                },
                rooms = devices.map { it.room }.distinct(),
                selectedRoom = room,
                totalPowerWatts = totalPower,
                totalPowerText = String.format(Locale.US, "%.1f W", totalPower),
                activeDeviceCount = poweredDevices.size
            )
        }
            .flowOn(computeDispatcher)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState()
            )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    // Seçili odaya tekrar basılırsa filtre kaldırılır
    fun onRoomSelected(room: String?) {
        selectedRoom.value = if (room == selectedRoom.value) null else room
    }

    fun onToggleDevice(id: Int) {
        viewModelScope.launch { repository.toggleDevice(id) }
    }
}
