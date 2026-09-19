package com.example.data

enum class DeviceType {
    CLASSIC,
    BLE,
    DUAL,
    UNKNOWN
}

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED
}

data class BluetoothDeviceItem(
    val name: String,
    val address: String,
    val rssi: Int = -60,
    val type: DeviceType = DeviceType.BLE,
    val isBonded: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val servicesCount: Int = 0
)

data class ConsoleLog(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String,
    val message: String,
    val type: LogType
)

enum class LogType {
    INFO,
    SUCCESS,
    SEND,
    RECEIVE,
    WARNING,
    ERROR
}
