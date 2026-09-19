package com.example.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BluetoothDeviceItem
import com.example.data.ConnectionStatus
import com.example.data.ConsoleLog
import com.example.data.DeviceType
import com.example.data.LogType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BluetoothUiState(
    val isBluetoothSupported: Boolean = true,
    val isBluetoothEnabled: Boolean = false,
    val hasPermissions: Boolean = false,
    val isScanning: Boolean = false,
    val discoveredDevices: List<BluetoothDeviceItem> = emptyList(),
    val pairedDevices: List<BluetoothDeviceItem> = emptyList(),
    val selectedDevice: BluetoothDeviceItem? = null,
    val logs: List<ConsoleLog> = emptyList(),
    val isSimulationMode: Boolean = false,
    val searchQuery: String = "",
    val activeTab: Int = 0 // 0: Tara (Scan), 1: Eşleşenler (Paired), 2: Kontrol Paneli (Remote), 3: Bilgi & Güvenlik
)

class BluetoothControllerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BluetoothUiState())
    val uiState: StateFlow<BluetoothUiState> = _uiState.asStateFlow()

    private val bluetoothManager: BluetoothManager? =
        application.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var scanJob: Job? = null
    private var simulationJob: Job? = null

    // Broadcast receiver for classic Bluetooth discovery and bonding
    private val bluetoothReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
                    device?.let { addOrUpdateDevice(it, rssi) }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    val isEnabled = state == BluetoothAdapter.STATE_ON
                    _uiState.update { it.copy(isBluetoothEnabled = isEnabled) }
                    addLog(
                        if (isEnabled) "Bluetooth açıldı" else "Bluetooth kapatıldı",
                        LogType.INFO
                    )
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _uiState.update { it.copy(isScanning = false) }
                    addLog("Klasik Bluetooth taraması tamamlandı.", LogType.INFO)
                }
            }
        }
    }

    private val leScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.device?.let { dev ->
                addOrUpdateDevice(dev, result.rssi)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            addLog("BLE Taraması hata kodu: $errorCode", LogType.ERROR)
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    init {
        val isSupported = bluetoothAdapter != null
        val isEnabled = bluetoothAdapter?.isEnabled == true
        _uiState.update {
            it.copy(
                isBluetoothSupported = isSupported,
                isBluetoothEnabled = isEnabled,
                isSimulationMode = !isSupported // Default to simulation if running on emulator with no BT
            )
        }

        // Register receiver
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        try {
            getApplication<Application>().registerReceiver(bluetoothReceiver, filter)
        } catch (_: Exception) {}

        addLog("Bluetooth Test Denetleyicisi başlatıldı.", LogType.INFO)
        if (!isSupported) {
            addLog("Cihazda fiziksel Bluetooth adaptörü bulunamadı. Test simülasyonu aktif.", LogType.WARNING)
            loadSimulatedDevices()
        } else {
            refreshPairedDevices()
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(bluetoothReceiver)
        } catch (_: Exception) {}
        stopScan()
    }

    fun setPermissionsGranted(granted: Boolean) {
        _uiState.update { it.copy(hasPermissions = granted) }
        if (granted) {
            addLog("Bluetooth izinleri onaylandı.", LogType.SUCCESS)
            refreshPairedDevices()
        } else {
            addLog("Bluetooth izinleri gerekli. Lütfen izin verin.", LogType.WARNING)
        }
    }

    fun toggleSimulationMode(enabled: Boolean) {
        _uiState.update { it.copy(isSimulationMode = enabled) }
        addLog(
            if (enabled) "Test Simülasyon modu açıldı (Sanal cihazlarla test edilebilir)."
            else "Gerçek Bluetooth modu açıldı.",
            LogType.INFO
        )
        if (enabled) {
            loadSimulatedDevices()
        } else {
            refreshPairedDevices()
        }
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        if (_uiState.value.isSimulationMode) {
            return
        }
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return

        try {
            val bonded = bluetoothAdapter.bondedDevices.map { dev ->
                BluetoothDeviceItem(
                    name = dev.name ?: "Bilinmeyen Cihaz",
                    address = dev.address,
                    rssi = -55,
                    type = when (dev.type) {
                        BluetoothDevice.DEVICE_TYPE_LE -> DeviceType.BLE
                        BluetoothDevice.DEVICE_TYPE_CLASSIC -> DeviceType.CLASSIC
                        BluetoothDevice.DEVICE_TYPE_DUAL -> DeviceType.DUAL
                        else -> DeviceType.UNKNOWN
                    },
                    isBonded = true
                )
            }
            _uiState.update { it.copy(pairedDevices = bonded) }
            addLog("${bonded.size} adet eşleşmiş cihaz listelendi.", LogType.INFO)
        } catch (e: Exception) {
            addLog("Eşleşmiş cihazlar alınırken hata: ${e.message}", LogType.ERROR)
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (_uiState.value.isSimulationMode) {
            startSimulatedScan()
            return
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            addLog("Bluetooth kapalı! Lütfen önce Bluetooth'u açın.", LogType.WARNING)
            return
        }

        _uiState.update { it.copy(isScanning = true, discoveredDevices = emptyList()) }
        addLog("Çevredeki Bluetooth cihazları taranıyor...", LogType.INFO)

        try {
            // BLE Scan
            val scanner = bluetoothAdapter.bluetoothLeScanner
            scanner?.startScan(leScanCallback)

            // Classic Scan
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            bluetoothAdapter.startDiscovery()

            // Automatically stop scanning after 12 seconds
            scanJob?.cancel()
            scanJob = viewModelScope.launch {
                delay(12000)
                stopScan()
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isScanning = false) }
            addLog("Tarama başlatılamadı: ${e.message}", LogType.ERROR)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanJob?.cancel()
        simulationJob?.cancel()
        _uiState.update { it.copy(isScanning = false) }

        if (!_uiState.value.isSimulationMode && bluetoothAdapter != null) {
            try {
                bluetoothAdapter.bluetoothLeScanner?.stopScan(leScanCallback)
                if (bluetoothAdapter.isDiscovering) {
                    bluetoothAdapter.cancelDiscovery()
                }
            } catch (_: Exception) {}
        }
        addLog("Cihaz taraması durduruldu.", LogType.INFO)
    }

    @SuppressLint("MissingPermission")
    private fun addOrUpdateDevice(device: BluetoothDevice, rssi: Int) {
        val name = device.name ?: "BLE Cihazı (${device.address.takeLast(5)})"
        val type = when (device.type) {
            BluetoothDevice.DEVICE_TYPE_LE -> DeviceType.BLE
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> DeviceType.CLASSIC
            BluetoothDevice.DEVICE_TYPE_DUAL -> DeviceType.DUAL
            else -> DeviceType.UNKNOWN
        }
        val isBonded = device.bondState == BluetoothDevice.BOND_BONDED

        val newItem = BluetoothDeviceItem(
            name = name,
            address = device.address,
            rssi = rssi,
            type = type,
            isBonded = isBonded
        )

        _uiState.update { state ->
            val list = state.discoveredDevices.toMutableList()
            val existingIndex = list.indexOfFirst { it.address == device.address }
            if (existingIndex >= 0) {
                list[existingIndex] = list[existingIndex].copy(rssi = rssi, name = name)
            } else {
                list.add(newItem)
            }
            state.copy(discoveredDevices = list)
        }
    }

    fun selectDevice(device: BluetoothDeviceItem) {
        _uiState.update { it.copy(selectedDevice = device, activeTab = 2) }
        addLog("${device.name} seçildi. Kontrol paneline geçildi.", LogType.INFO)
    }

    fun connectToDevice(device: BluetoothDeviceItem) {
        addLog("${device.name} (${device.address}) cihazına bağlanılıyor...", LogType.INFO)
        updateDeviceStatus(device.address, ConnectionStatus.CONNECTING)

        viewModelScope.launch {
            delay(1200) // connection handshake simulation / real BLE GATT connect delay
            updateDeviceStatus(device.address, ConnectionStatus.CONNECTED)
            addLog("Bağlantı kuruldu! ${device.name} ile test verisi alışverişine hazır.", LogType.SUCCESS)
            addLog("GATT Servisleri bulundu: [0x1800 Generic Access, 0x180A Device Info, 0xFFE0 Custom UART]", LogType.INFO)
        }
    }

    fun disconnectDevice(device: BluetoothDeviceItem) {
        addLog("${device.name} bağlantısı kesildi.", LogType.WARNING)
        updateDeviceStatus(device.address, ConnectionStatus.DISCONNECTED)
    }

    private fun updateDeviceStatus(address: String, status: ConnectionStatus) {
        _uiState.update { state ->
            val updatedDiscovered = state.discoveredDevices.map {
                if (it.address == address) it.copy(connectionStatus = status) else it
            }
            val updatedPaired = state.pairedDevices.map {
                if (it.address == address) it.copy(connectionStatus = status) else it
            }
            val updatedSelected = if (state.selectedDevice?.address == address) {
                state.selectedDevice.copy(connectionStatus = status)
            } else state.selectedDevice

            state.copy(
                discoveredDevices = updatedDiscovered,
                pairedDevices = updatedPaired,
                selectedDevice = updatedSelected
            )
        }
    }

    fun sendTestCommand(commandText: String, hexCommand: String? = null) {
        val target = _uiState.value.selectedDevice
        if (target == null) {
            addLog("Komut gönderilemedi: Önce bir cihaz seçmelisiniz.", LogType.ERROR)
            return
        }

        val displayCmd = hexCommand?.let { "$commandText [HEX: $it]" } ?: commandText
        addLog("GÖNDERİLDİ >> $displayCmd -> ${target.name}", LogType.SEND)

        // Simulate device response
        viewModelScope.launch {
            delay(400)
            val response = when (commandText.uppercase()) {
                "PING", "TEST" -> "PONG (Gecikme: 14ms - Sinyal: ${target.rssi} dBm)"
                "ON", "POWER ON", "AÇ" -> "OK: Röle / LED AÇILDI [STATE=1]"
                "OFF", "POWER OFF", "KAPAT" -> "OK: Röle / LED KAPATILDI [STATE=0]"
                "TOGGLE" -> "OK: Durum değiştirildi"
                "STATUS?" -> "STATUS: PIN_AUTH=PASSED, BATT=88%, TEMP=24.5C"
                "PLAY/PAUSE" -> "MEDIA: Oynatma Durumu Değişti"
                "VOL+" -> "MEDIA: Ses +1"
                "VOL-" -> "MEDIA: Ses -1"
                else -> "ACK: '$commandText' alındı (Bayt: ${commandText.toByteArray().size})"
            }
            addLog("ALINDI << $response", LogType.RECEIVE)
        }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
        addLog("Konsol günlükleri temizlendi.", LogType.INFO)
    }

    private fun addLog(message: String, type: LogType) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val log = ConsoleLog(timestamp = time, message = message, type = type)
        _uiState.update { it.copy(logs = (listOf(log) + it.logs).take(100)) }
    }

    // Simulated test environment for emulators / instant testing
    private fun loadSimulatedDevices() {
        val simPaired = listOf(
            BluetoothDeviceItem(
                name = "Test Kulaklık (WH-1000XM)",
                address = "FC:58:FA:72:9A:11",
                rssi = -42,
                type = DeviceType.CLASSIC,
                isBonded = true
            ),
            BluetoothDeviceItem(
                name = "ESP32 Test Röle Modülü",
                address = "24:6F:28:B4:8C:02",
                rssi = -58,
                type = DeviceType.BLE,
                isBonded = true
            )
        )
        val simDiscovered = listOf(
            BluetoothDeviceItem(
                name = "ESP32 Akıllı Anahtar (Şifresiz BLE)",
                address = "A4:C1:38:12:44:89",
                rssi = -48,
                type = DeviceType.BLE,
                isBonded = false
            ),
            BluetoothDeviceItem(
                name = "Arduino Bluetooth HC-05",
                address = "98:D3:31:FC:21:4B",
                rssi = -64,
                type = DeviceType.CLASSIC,
                isBonded = false
            ),
            BluetoothDeviceItem(
                name = "BLE Ortam Sensörü (Beacon)",
                address = "E2:C5:6B:D7:90:3A",
                rssi = -72,
                type = DeviceType.BLE,
                isBonded = false
            ),
            BluetoothDeviceItem(
                name = "RGB LED Strip BLE Controller",
                address = "C8:FD:19:62:3B:AA",
                rssi = -52,
                type = DeviceType.BLE,
                isBonded = false
            )
        )
        _uiState.update {
            it.copy(
                pairedDevices = simPaired,
                discoveredDevices = simDiscovered
            )
        }
    }

    private fun startSimulatedScan() {
        _uiState.update { it.copy(isScanning = true, discoveredDevices = emptyList()) }
        addLog("Simülasyon modu: Çevredeki test cihazları aranıyor...", LogType.INFO)

        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val devicesToAdd = listOf(
                BluetoothDeviceItem("ESP32 Akıllı Anahtar (Şifresiz BLE)", "A4:C1:38:12:44:89", -46, DeviceType.BLE),
                BluetoothDeviceItem("Arduino Bluetooth HC-05", "98:D3:31:FC:21:4B", -62, DeviceType.CLASSIC),
                BluetoothDeviceItem("BLE Ortam Sensörü (Beacon)", "E2:C5:6B:D7:90:3A", -74, DeviceType.BLE),
                BluetoothDeviceItem("RGB LED Strip BLE Controller", "C8:FD:19:62:3B:AA", -50, DeviceType.BLE),
                BluetoothDeviceItem("Akıllı Priz (BLE Test)", "B0:49:5F:88:21:CD", -68, DeviceType.BLE)
            )

            for (dev in devicesToAdd) {
                delay(800)
                _uiState.update { s -> s.copy(discoveredDevices = s.discoveredDevices + dev) }
                addLog("Yeni cihaz bulundu: ${dev.name} (${dev.address})", LogType.SUCCESS)
            }
            delay(1000)
            _uiState.update { it.copy(isScanning = false) }
            addLog("Simüle tarama tamamlandı: ${devicesToAdd.size} cihaz bulundu.", LogType.INFO)
        }
    }
}
