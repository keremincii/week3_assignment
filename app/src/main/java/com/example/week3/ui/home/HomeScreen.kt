package com.example.week3.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.week3.data.SmartDevice
import com.example.week3.data.sampleDevices
import com.example.week3.ui.theme.Week3_assignmentTheme

// Route: ViewModel'e bağlanan tek yer. State'i toplar, olayları ViewModel'e yönlendirir.
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onRoomSelected = viewModel::onRoomSelected,
        onToggleDevice = viewModel::onToggleDevice,
        modifier = modifier
    )
}

// Buradan aşağısı tamamen stateless: remember yok, filtre yok, hesaplama yok.
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRoomSelected: (String?) -> Unit,
    onToggleDevice: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        EnergyCard(
            totalPowerText = uiState.totalPowerText,
            activeDeviceCount = uiState.activeDeviceCount,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        DeviceSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)
        )
        RoomFilterRow(
            rooms = uiState.rooms,
            selectedRoom = uiState.selectedRoom,
            onRoomSelected = onRoomSelected
        )
        if (uiState.devices.isEmpty()) {
            Text(
                text = "Eşleşen cihaz bulunamadı",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // key: sadece değişen kart yeniden çizilir
                items(items = uiState.devices, key = { it.id }) { device ->
                    DeviceCard(device = device, onToggle = { onToggleDevice(device.id) })
                }
            }
        }
    }
}

@Composable
fun EnergyCard(
    totalPowerText: String,
    activeDeviceCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "⚡ Anlık Enerji Tüketimi", style = MaterialTheme.typography.titleMedium)
            Text(text = totalPowerText, style = MaterialTheme.typography.displaySmall)
            Text(
                text = "$activeDeviceCount aktif ve çevrimiçi cihaz",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DeviceSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(text = "Cihaz ara") },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun RoomFilterRow(
    rooms: List<String>,
    selectedRoom: String?,
    onRoomSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedRoom == null,
                onClick = { onRoomSelected(null) },
                label = { Text(text = "Tümü") }
            )
        }
        items(items = rooms, key = { it }) { room ->
            FilterChip(
                selected = room == selectedRoom,
                onClick = { onRoomSelected(room) },
                label = { Text(text = room) }
            )
        }
    }
}

@Composable
fun DeviceCard(
    device: SmartDevice,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = device.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${device.category.emoji} ${device.category.label} • ${device.room}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(text = "${device.powerWatts} W", style = MaterialTheme.typography.bodySmall)
                DeviceStatus(device = device)
            }
            if (device.isActive) {
                OutlinedButton(onClick = onToggle, enabled = device.isOnline) {
                    Text(text = "Kapat")
                }
            } else {
                Button(onClick = onToggle, enabled = device.isOnline) {
                    Text(text = "Aç")
                }
            }
        }
    }
}

@Composable
private fun DeviceStatus(device: SmartDevice) {
    when {
        !device.isOnline -> Text(
            text = "● Çevrimdışı",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelLarge
        )
        device.isActive -> Text(
            text = "● Açık",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )
        else -> Text(
            text = "● Kapalı",
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun HomeScreenPreview() {
    Week3_assignmentTheme {
        HomeScreen(
            uiState = HomeUiState(
                devices = sampleDevices,
                rooms = listOf("Salon", "Bahçe", "Antre", "Yatak Odası", "Mutfak", "Koridor", "Balkon", "Ofis"),
                totalPowerText = "106.0 W",
                activeDeviceCount = 6
            ),
            searchQuery = "",
            onSearchQueryChange = { },
            onRoomSelected = { },
            onToggleDevice = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceCardOfflinePreview() {
    Week3_assignmentTheme {
        DeviceCard(device = sampleDevices[8], onToggle = { })
    }
}
