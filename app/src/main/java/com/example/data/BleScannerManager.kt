package com.example.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.SharedPreferences
import com.example.model.BleDeviceModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class BleScannerManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("flipper_droid_ble", Context.MODE_PRIVATE)

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<Map<String, BleDeviceModel>>(emptyMap())
    val discoveredDevices: StateFlow<Map<String, BleDeviceModel>> = _discoveredDevices.asStateFlow()

    private val _savedDevices = MutableStateFlow<List<BleDeviceModel>>(loadSavedDevices())
    val savedDevices: StateFlow<List<BleDeviceModel>> = _savedDevices.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result ?: return
            val device = result.device ?: return
            val address = device.address ?: "00:00:00:00:00:00"
            val name = try { device.name ?: result.scanRecord?.deviceName } catch (_: SecurityException) { null }
            val rssi = result.rssi
            val uuids = result.scanRecord?.serviceUuids?.map { it.toString() } ?: emptyList()

            val isSaved = _savedDevices.value.any { it.address == address }

            val model = BleDeviceModel(
                address = address,
                name = name,
                rssi = rssi,
                serviceUuids = uuids,
                lastSeen = System.currentTimeMillis(),
                isSaved = isSaved
            )

            val map = _discoveredDevices.value.toMutableMap()
            map[address] = model
            _discoveredDevices.value = map
        }

        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
        }
    }

    fun isBluetoothAvailable(): Boolean = bluetoothAdapter != null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun startScan(): Boolean {
        if (!isBluetoothAvailable() || !isBluetoothEnabled()) {
            // Seed with sample simulated devices for instant terminal feedback if BLE hardware is restricted
            seedSimulatedDevices()
            _isScanning.value = true
            return false
        }

        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner != null) {
            try {
                scanner.startScan(scanCallback)
                _isScanning.value = true
                return true
            } catch (_: SecurityException) {
                seedSimulatedDevices()
                _isScanning.value = true
                return false
            }
        }
        return false
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (_isScanning.value) {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            } catch (_: Exception) {}
            _isScanning.value = false
        }
    }

    fun saveDevice(device: BleDeviceModel) {
        val updated = device.copy(isSaved = true)
        val list = _savedDevices.value.filter { it.address != device.address } + updated
        _savedDevices.value = list
        persistSavedDevices(list)

        // Update in discovered list
        val map = _discoveredDevices.value.toMutableMap()
        if (map.containsKey(device.address)) {
            map[device.address] = updated
            _discoveredDevices.value = map
        }
    }

    fun removeSavedDevice(address: String) {
        val list = _savedDevices.value.filter { it.address != address }
        _savedDevices.value = list
        persistSavedDevices(list)

        val map = _discoveredDevices.value.toMutableMap()
        if (map.containsKey(address)) {
            map[address] = map[address]!!.copy(isSaved = false)
            _discoveredDevices.value = map
        }
    }

    private fun seedSimulatedDevices() {
        val sample = listOf(
            BleDeviceModel(
                address = "FC:0F:E6:12:44:9A",
                name = "Flipper_Anub1s",
                rssi = -54,
                serviceUuids = listOf("00003082-0000-1000-8000-00805f9b34fb")
            ),
            BleDeviceModel(
                address = "4C:76:25:A8:10:BC",
                name = "Apple AirTag (Proximity)",
                rssi = -72,
                serviceUuids = listOf("0000fd6f-0000-1000-8000-00805f9b34fb")
            ),
            BleDeviceModel(
                address = "D4:36:39:CA:71:02",
                name = "Nordic_Thingy52",
                rssi = -65,
                serviceUuids = listOf("ef680100-9b35-4933-9b10-52ffa9740042")
            ),
            BleDeviceModel(
                address = "80:EA:CA:9F:33:55",
                name = "ESP32_BLE_Beacon",
                rssi = -81,
                serviceUuids = listOf("0000feaa-0000-1000-8000-00805f9b34fb")
            )
        )
        val map = _discoveredDevices.value.toMutableMap()
        for (d in sample) {
            val isSaved = _savedDevices.value.any { it.address == d.address }
            map[d.address] = d.copy(isSaved = isSaved)
        }
        _discoveredDevices.value = map
    }

    private fun loadSavedDevices(): List<BleDeviceModel> {
        val raw = prefs.getString("saved_ble_devices", "[]") ?: "[]"
        val list = mutableListOf<BleDeviceModel>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val uuids = mutableListOf<String>()
                val uArr = obj.optJSONArray("uuids")
                if (uArr != null) {
                    for (j in 0 until uArr.length()) uuids.add(uArr.getString(j))
                }
                list.add(
                    BleDeviceModel(
                        address = obj.getString("address"),
                        name = if (obj.has("name")) obj.getString("name") else null,
                        rssi = obj.optInt("rssi", -70),
                        serviceUuids = uuids,
                        lastSeen = obj.optLong("lastSeen", System.currentTimeMillis()),
                        isSaved = true
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun persistSavedDevices(list: List<BleDeviceModel>) {
        val arr = JSONArray()
        for (d in list) {
            val obj = JSONObject()
            obj.put("address", d.address)
            if (d.name != null) obj.put("name", d.name)
            obj.put("rssi", d.rssi)
            obj.put("lastSeen", d.lastSeen)
            obj.put("uuids", JSONArray(d.serviceUuids))
            arr.put(obj)
        }
        prefs.edit().putString("saved_ble_devices", arr.toString()).apply()
    }
}
