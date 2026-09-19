package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BluetoothDeviceItem
import com.example.data.ConnectionStatus
import com.example.data.ConsoleLog
import com.example.data.DeviceType
import com.example.data.LogType
import com.example.viewmodel.BluetoothControllerViewModel
import com.example.viewmodel.BluetoothUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothAppScreen(
    viewModel: BluetoothControllerViewModel,
    uiState: BluetoothUiState,
    onRequestPermissions: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = "Bluetooth Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Bluetooth Test Kontrolcü",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.isSimulationMode) "Simülasyon / Test Modu" else "Cihaz Donanım Modu",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.isSimulationMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Test Modu",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Switch(
                            checked = uiState.isSimulationMode,
                            onCheckedChange = { viewModel.toggleSimulationMode(it) },
                            modifier = Modifier.testTag("simulation_switch")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                NavigationBarItem(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    icon = { Icon(Icons.Default.BluetoothSearching, contentDescription = "Cihaz Tara") },
                    label = { Text("Tara") },
                    modifier = Modifier.testTag("tab_scan")
                )
                NavigationBarItem(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    icon = { Icon(Icons.Default.Devices, contentDescription = "Eşleşmiş Cihazlar") },
                    label = { Text("Eşleşenler") },
                    modifier = Modifier.testTag("tab_paired")
                )
                NavigationBarItem(
                    selected = uiState.activeTab == 2,
                    onClick = { viewModel.setActiveTab(2) },
                    icon = { Icon(Icons.Default.SettingsRemote, contentDescription = "Uzaktan Kontrol") },
                    label = { Text("Kumanda") },
                    modifier = Modifier.testTag("tab_remote")
                )
                NavigationBarItem(
                    selected = uiState.activeTab == 3,
                    onClick = { viewModel.setActiveTab(3) },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Güvenlik & Şifre Bilgisi") },
                    label = { Text("Güvenlik") },
                    modifier = Modifier.testTag("tab_security")
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Permission & Bluetooth Status Banner
            if (!uiState.hasPermissions && !uiState.isSimulationMode) {
                PermissionNoticeCard(onRequestPermissions)
            } else if (!uiState.isBluetoothEnabled && !uiState.isSimulationMode) {
                BluetoothDisabledCard()
            }

            // Tab Content
            when (uiState.activeTab) {
                0 -> ScanDevicesView(
                    uiState = uiState,
                    onStartScan = { viewModel.startScan() },
                    onStopScan = { viewModel.stopScan() },
                    onSelectDevice = { viewModel.selectDevice(it) },
                    onSearchQueryChange = { viewModel.setSearchQuery(it) }
                )
                1 -> PairedDevicesView(
                    uiState = uiState,
                    onRefresh = { viewModel.refreshPairedDevices() },
                    onSelectDevice = { viewModel.selectDevice(it) }
                )
                2 -> RemoteControlView(
                    uiState = uiState,
                    onConnect = { viewModel.connectToDevice(it) },
                    onDisconnect = { viewModel.disconnectDevice(it) },
                    onSendCommand = { cmd, hex -> viewModel.sendTestCommand(cmd, hex) },
                    onClearLogs = { viewModel.clearLogs() },
                    onGoToScan = { viewModel.setActiveTab(0) }
                )
                3 -> SecurityInfoView()
            }
        }
    }
}

@Composable
fun PermissionNoticeCard(onRequestPermissions: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bluetooth İzni Gerekli",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Cihazları taramak ve bağlanmak için izin verilmelidir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier.testTag("grant_permission_button")
            ) {
                Text("İzin Ver")
            }
        }
    }
}

@Composable
fun BluetoothDisabledCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Bluetooth, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Telefonun Bluetooth özelliği kapalı. Lütfen ayarlardan açın veya Test Modunu kullanın.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
fun ScanDevicesView(
    uiState: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onSelectDevice: (BluetoothDeviceItem) -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar & Scan Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Cihaz veya MAC ara...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Temizle")
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("device_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (uiState.isScanning) {
                Button(
                    onClick = onStopScan,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("stop_scan_button")
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Durdur")
                }
            } else {
                Button(
                    onClick = onStartScan,
                    modifier = Modifier.testTag("start_scan_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tara")
                }
            }
        }

        if (uiState.isScanning) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filtered = uiState.discoveredDevices.filter {
            it.name.contains(uiState.searchQuery, ignoreCase = true) ||
                    it.address.contains(uiState.searchQuery, ignoreCase = true)
        }

        Text(
            text = "Bulunan Cihazlar (${filtered.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.BluetoothSearching,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.isScanning) "Cihazlar taranıyor..." else "Henüz cihaz bulunamadı",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Yakındaki Bluetooth veya BLE donanımlarını aramak için 'Tara'ya basın.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filtered, key = { it.address }) { dev ->
                    DeviceCard(device = dev, onSelect = { onSelectDevice(dev) })
                }
            }
        }
    }
}

@Composable
fun PairedDevicesView(
    uiState: BluetoothUiState,
    onRefresh: () -> Unit,
    onSelectDevice: (BluetoothDeviceItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Eşleşmiş Cihazlar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Telefonunuza önceden kaydedilmiş cihazlar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onRefresh, modifier = Modifier.testTag("refresh_paired_button")) {
                Icon(Icons.Default.Refresh, contentDescription = "Yenile")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.pairedDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Kayıtlı eşleşmiş cihaz bulunamadı.",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.pairedDevices, key = { it.address }) { dev ->
                    DeviceCard(device = dev, onSelect = { onSelectDevice(dev) })
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: BluetoothDeviceItem,
    onSelect: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("device_card_${device.address.replace(":", "")}"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (device.type == DeviceType.BLE) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (device.connectionStatus == ConnectionStatus.CONNECTED) Icons.Default.BluetoothConnected
                    else Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = if (device.connectionStatus == ConnectionStatus.CONNECTED) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = device.type.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }
                    if (device.isBonded) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Eşleşmiş",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${device.rssi} dBm",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSelect,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("select_device_btn")
            ) {
                Text(
                    text = if (device.connectionStatus == ConnectionStatus.CONNECTED) "Bağlı" else "Kontrol",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun RemoteControlView(
    uiState: BluetoothUiState,
    onConnect: (BluetoothDeviceItem) -> Unit,
    onDisconnect: (BluetoothDeviceItem) -> Unit,
    onSendCommand: (String, String?) -> Unit,
    onClearLogs: () -> Unit,
    onGoToScan: () -> Unit
) {
    val selectedDevice = uiState.selectedDevice
    var customCommandText by remember { mutableStateOf("") }

    if (selectedDevice == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.SettingsRemote,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Kontrol edilecek cihaz seçilmedi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Test komutları göndermek veya uzaktan kontrol etmek için cihazlar sekmesinden bir cihaz seçin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onGoToScan, modifier = Modifier.testTag("select_from_scan_button")) {
                    Text("Cihaz Listesine Git")
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Active Target Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedDevice.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "MAC: ${selectedDevice.address} | Sinyal: ${selectedDevice.rssi} dBm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusColor = when (selectedDevice.connectionStatus) {
                            ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.primary
                            ConnectionStatus.CONNECTING -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.outline
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (selectedDevice.connectionStatus) {
                                ConnectionStatus.CONNECTED -> "Bağlandı (Aktif)"
                                ConnectionStatus.CONNECTING -> "Bağlanıyor..."
                                else -> "Bağlantı Yok"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (selectedDevice.connectionStatus == ConnectionStatus.CONNECTED) {
                    OutlinedButton(
                        onClick = { onDisconnect(selectedDevice) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("disconnect_button")
                    ) {
                        Text("Kes")
                    }
                } else {
                    Button(
                        onClick = { onConnect(selectedDevice) },
                        modifier = Modifier.testTag("connect_button")
                    ) {
                        Text("Bağlan")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Controls
        Text(
            text = "Hızlı Test Komutları",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Power & Switch Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onSendCommand("ON", "0x01") },
                modifier = Modifier.weight(1f).testTag("cmd_on"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("AÇ (1)")
            }
            Button(
                onClick = { onSendCommand("OFF", "0x00") },
                modifier = Modifier.weight(1f).testTag("cmd_off"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("KAPAT (0)")
            }
            Button(
                onClick = { onSendCommand("TOGGLE", "0x02") },
                modifier = Modifier.weight(1f).testTag("cmd_toggle"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("DURUM")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Media & Ping Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onSendCommand("PLAY/PAUSE", "0x44") },
                modifier = Modifier.weight(1f).testTag("cmd_media_play")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Oynat")
            }
            OutlinedButton(
                onClick = { onSendCommand("VOL+", "0x41") },
                modifier = Modifier.weight(1f).testTag("cmd_vol_up")
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Ses+")
            }
            OutlinedButton(
                onClick = { onSendCommand("VOL-", "0x42") },
                modifier = Modifier.weight(1f).testTag("cmd_vol_down")
            ) {
                Icon(Icons.Default.VolumeDown, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Ses-")
            }
            OutlinedButton(
                onClick = { onSendCommand("PING", "0x50") },
                modifier = Modifier.weight(1f).testTag("cmd_ping")
            ) {
                Text("PING")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Custom Command Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customCommandText,
                onValueChange = { customCommandText = it },
                placeholder = { Text("Örn: AT+STATUS, TEST, LED_ON...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("custom_command_input"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (customCommandText.isNotBlank()) {
                        onSendCommand(customCommandText, null)
                        customCommandText = ""
                    }
                },
                modifier = Modifier.testTag("send_custom_cmd_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Terminal / Console Logs Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "İletişim & Test Günlüğü (Konsol)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onClearLogs, modifier = Modifier.testTag("clear_logs_button")) {
                Icon(Icons.Default.Delete, contentDescription = "Günlüğü Temizle", tint = MaterialTheme.colorScheme.outline)
            }
        }

        // Terminal Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E1E))
                .padding(10.dp)
        ) {
            if (uiState.logs.isEmpty()) {
                Text(
                    text = "Henüz günlük kaydı yok. Bir cihaza bağlanın veya komut gönderin.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true
                ) {
                    items(uiState.logs, key = { it.id }) { log ->
                        ConsoleLogItem(log)
                    }
                }
            }
        }
    }
}

@Composable
fun ConsoleLogItem(log: ConsoleLog) {
    val textColor = when (log.type) {
        LogType.SEND -> Color(0xFF64B5F6) // Light Blue
        LogType.RECEIVE -> Color(0xFF81C784) // Light Green
        LogType.SUCCESS -> Color(0xFFA5D6A7)
        LogType.WARNING -> Color(0xFFFFB74D) // Amber
        LogType.ERROR -> Color(0xFFEF5350) // Red
        LogType.INFO -> Color(0xFFE0E0E0) // Light Gray
    }

    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "[${log.timestamp}] ",
            color = Color.DarkGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = log.message,
            color = textColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun SecurityInfoView() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Bluetooth ve Şifre Güvenlik Mimarisi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cihazlara şifresiz bağlanma hakkında teknik ve güvenlik bilgileri",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            ElevatedCard(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1. Şifresiz Kullanılabilen Donanımlar (BLE)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Bluetooth Low Energy (BLE) ile çalışan ESP32, Arduino, akıllı LED şeritleri, oda sensörleri ve beacon'lar genellikle şifresiz (Just Works) profilde çalışır. Bu cihazlara Android OS şifre sormadan doğrudan GATT servisi üzerinden komut gönderebilirsiniz.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            ElevatedCard(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. Şifreli / Eşleşme Zorunlu Cihazlar",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Telefonlar, bilgisayarlar ve güvenli kulaklıklar Bluetooth SSP (Secure Simple Pairing) ve PIN kimlik doğrulaması gerektirir. Android işletim sistemi çekirdeği ve Bluetooth güvenlik protokolleri, yetkisiz erişimi engellemek için şifre veya PIN onayını zorunlu tutar.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            ElevatedCard(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3. Bu Uygulama ile Neler Test Edebilirsiniz?",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Yakındaki tüm BLE ve Klasik Bluetooth sinyallerini tarama\n" +
                                "• Cihazların RSSI (sinyal gücü) ve donanım adreslerini izleme\n" +
                                "• Önceden eşleşmiş cihazlara doğrudan kumanda komutları iletme\n" +
                                "• Açık BLE test cihazlarına röle, LED ve durum komutları gönderme\n" +
                                "• Konsol günlüğü ile gönderilen ve alınan paketleri anlık izleme",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
