package com.example.week3

import com.example.week3.data.DeviceRepository
import com.example.week3.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.createViewModel(): HomeViewModel {
        val viewModel = HomeViewModel(DeviceRepository(dispatcher), dispatcher)
        backgroundScope.launch { viewModel.uiState.collect() }
        return viewModel
    }

    @Test
    fun `toplam sadece aktif ve online cihazlari sayar`() = runTest(dispatcher) {
        val state = createViewModel().uiState.value

        // 15 + 8.5 + 8.5 + 12 + 50 + 12
        assertEquals(106.0, state.totalPowerWatts, 0.0)
        assertEquals("106.0 W", state.totalPowerText)
        assertEquals(6, state.activeDeviceCount)
    }

    @Test
    fun `cihaz acilinca toplam aninda guncellenir`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onToggleDevice(2) // Tavan Avizesi, 60 W
        advanceUntilIdle()
        assertEquals(166.0, viewModel.uiState.value.totalPowerWatts, 0.0)

        viewModel.onToggleDevice(2)
        advanceUntilIdle()
        assertEquals(106.0, viewModel.uiState.value.totalPowerWatts, 0.0)
    }

    @Test
    fun `offline cihazin durumu degistirilemez`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onToggleDevice(9) // Balkon Aydınlatması, offline
        advanceUntilIdle()

        assertEquals(106.0, viewModel.uiState.value.totalPowerWatts, 0.0)
    }

    @Test
    fun `arama ve oda filtresi birlikte calisir`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onRoomSelected("Salon")
        assertEquals(3, viewModel.uiState.value.devices.size)

        viewModel.onSearchQueryChange("lamba")
        assertEquals(listOf("Köşe Lambası"), viewModel.uiState.value.devices.map { it.name })

        viewModel.onRoomSelected("Salon") // tekrar basınca filtre kalkar
        assertEquals(
            listOf("Köşe Lambası", "Çalışma Odası Lambası"),
            viewModel.uiState.value.devices.map { it.name }
        )
    }
}
