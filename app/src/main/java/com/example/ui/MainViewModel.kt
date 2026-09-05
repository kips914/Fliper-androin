package com.example.ui

import android.app.Application
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BleScannerManager
import com.example.data.DuckyExecutionStep
import com.example.data.DuckyScriptManager
import com.example.data.FirmwareManager
import com.example.data.NfcStorageManager
import com.example.data.SubGhzLabEngine
import com.example.model.AppTab
import com.example.model.BleDeviceModel
import com.example.model.DuckyScriptItem
import com.example.model.FirmwarePack
import com.example.model.NfcCardModel
import com.example.model.SubGhzSignal
import com.example.nfc.ApduExchange
import com.example.nfc.FlipperHostApduService
import com.example.nfc.NfcReaderHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MascotMood {
    IDLE, HAPPY, SCANNING, TRANSMIT, SLEEP
}

data class MascotState(
    val mood: MascotMood = MascotMood.IDLE,
    val xp: Int = 850,
    val level: Int = 3,
    val message: String = "Flipper Droid готов к работе!"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val firmwareManager = FirmwareManager(application)
    val nfcManager = NfcStorageManager(application)
    val bleManager = BleScannerManager(application)
    val duckyManager = DuckyScriptManager()

    private val nfcAdapter: NfcAdapter? = try {
        NfcAdapter.getDefaultAdapter(application)
    } catch (_: Exception) {
        null
    }

    val currentFirmware: StateFlow<FirmwarePack> = firmwareManager.currentFirmware

    private val _activeTab = MutableStateFlow(AppTab.NFC)
    val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(
        listOf(
            "[00:00:01] FLIPPER DROID OS v0.98.3 BOOTED",
            "[00:00:02] HARDWARE VIRTUALIZATION ENGINE ONLINE",
            "[00:00:02] NFC HF (13.56 MHz): READY",
            "[00:00:03] SUB-GHZ LAB SYNTHESIZER: LOADED",
            "[00:00:03] BADUSB DUCKY ENGINE: INITIALIZED",
            "[00:00:04] BLE 2.4 GHz SUBSYSTEM: STANDBY"
        )
    )
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    private val _mascotState = MutableStateFlow(MascotState())
    val mascotState: StateFlow<MascotState> = _mascotState.asStateFlow()

    // --- NFC State ---
    private val _lastScannedCard = MutableStateFlow<NfcCardModel?>(null)
    val lastScannedCard: StateFlow<NfcCardModel?> = _lastScannedCard.asStateFlow()

    private val _isReaderActive = MutableStateFlow(false)
    val isReaderActive: StateFlow<Boolean> = _isReaderActive.asStateFlow()

    private val _apduLogs = MutableStateFlow<List<ApduExchange>>(emptyList())
    val apduLogs: StateFlow<List<ApduExchange>> = _apduLogs.asStateFlow()

    val isNfcAvailable: Boolean = nfcAdapter != null
    val isNfcEnabled: Boolean get() = nfcAdapter?.isEnabled == true

    // --- Sub-GHz State ---
    private val _subGhzFrequency = MutableStateFlow(433.92)
    val subGhzFrequency: StateFlow<Double> = _subGhzFrequency.asStateFlow()

    private val _subGhzProtocol = MutableStateFlow("KeeLoq 64bit")
    val subGhzProtocol: StateFlow<String> = _subGhzProtocol.asStateFlow()

    private val _subGhzModulation = MutableStateFlow("AM650")
    val subGhzModulation: StateFlow<String> = _subGhzModulation.asStateFlow()

    private val _subGhzHexKey = MutableStateFlow("1A2B3C4D5E")
    val subGhzHexKey: StateFlow<String> = _subGhzHexKey.asStateFlow()

    private val _currentSignal = MutableStateFlow<SubGhzSignal?>(
        SubGhzLabEngine.synthesizeSignal(433.92, "KeeLoq 64bit", "AM650", "1A2B3C4D5E")
    )
    val currentSignal: StateFlow<SubGhzSignal?> = _currentSignal.asStateFlow()

    private val _spectrumPoints = MutableStateFlow(
        SubGhzLabEngine.generateSimulatedSpectrum(433.92)
    )
    val spectrumPoints: StateFlow<List<Float>> = _spectrumPoints.asStateFlow()

    private val _isTransmitting = MutableStateFlow(false)
    val isTransmitting: StateFlow<Boolean> = _isTransmitting.asStateFlow()

    private val _rawDecoderInput = MutableStateFlow("320 -960 640 -640 320 -960 640 -640 320 -960")
    val rawDecoderInput: StateFlow<String> = _rawDecoderInput.asStateFlow()

    private val _rawDecoderOutput = MutableStateFlow(Pair("01010", "0x0A"))
    val rawDecoderOutput: StateFlow<Pair<String, String>> = _rawDecoderOutput.asStateFlow()

    // --- BadUSB State ---
    private val _selectedScript = MutableStateFlow<DuckyScriptItem>(duckyManager.defaultTemplates[0])
    val selectedScript: StateFlow<DuckyScriptItem> = _selectedScript.asStateFlow()

    private val _scriptEditorContent = MutableStateFlow(duckyManager.defaultTemplates[0].scriptContent)
    val scriptEditorContent: StateFlow<String> = _scriptEditorContent.asStateFlow()

    val duckyExecutionLogs = duckyManager.executionLogs
    val isDuckyExecuting = duckyManager.isExecuting
    val simulatedScreenText = duckyManager.simulatedScreenText

    // --- BLE State ---
    val isBleScanning = bleManager.isScanning
    val discoveredBleDevices = bleManager.discoveredDevices
    val savedBleDevices = bleManager.savedDevices

    private val _selectedBleDevice = MutableStateFlow<BleDeviceModel?>(null)
    val selectedBleDevice: StateFlow<BleDeviceModel?> = _selectedBleDevice.asStateFlow()

    init {
        // Collect HCE APDUs
        viewModelScope.launch {
            FlipperHostApduService.apduEvents.collect { event ->
                _apduLogs.value = listOf(event) + _apduLogs.value.take(20)
                addLog("[APDU] CMD: ${event.commandHex.take(16)}... RESP: ${event.responseHex.take(16)}")
                triggerMascot(MascotMood.HAPPY, "NFC эмуляция ответила считывателю!")
            }
        }

        // Sync SubGHz protocol with firmware defaults
        viewModelScope.launch {
            currentFirmware.collect { fw ->
                if (!fw.subGhzProtocols.contains(_subGhzProtocol.value) && fw.subGhzProtocols.isNotEmpty()) {
                    _subGhzProtocol.value = fw.subGhzProtocols[0]
                    synthesizeSubGhzSignal()
                }
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _activeTab.value = tab
        addLog("[NAV] Switched to ${tab.title}")
    }

    fun addLog(msg: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val formatted = "[$time] $msg"
        val list = _terminalLogs.value.toMutableList()
        list.add(formatted)
        if (list.size > 80) list.removeAt(0)
        _terminalLogs.value = list
    }

    fun clearLogs() {
        _terminalLogs.value = emptyList()
    }

    fun triggerMascot(mood: MascotMood, message: String, durationMs: Long = 3000) {
        viewModelScope.launch {
            _mascotState.value = _mascotState.value.copy(
                mood = mood,
                message = message,
                xp = _mascotState.value.xp + 15
            )
            delay(durationMs)
            _mascotState.value = _mascotState.value.copy(
                mood = MascotMood.IDLE,
                message = "Flipper Droid в режиме ожидания"
            )
        }
    }

    // --- NFC Methods ---
    fun onTagScanned(tag: Tag) {
        val card = NfcReaderHelper.parseTag(tag)
        _lastScannedCard.value = card
        nfcManager.saveCard(card)
        addLog("[NFC-READ] Tag: ${card.name} UID: ${card.uidHex} Tech: ${card.techList.joinToString()}")
        triggerMascot(MascotMood.HAPPY, "Считана карта 13.56 МГц: ${card.name}!")
    }

    fun loadSimulatedTag(sampleCard: NfcCardModel) {
        _lastScannedCard.value = sampleCard
        nfcManager.saveCard(sampleCard)
        addLog("[NFC-SIM] Loaded dump: ${sampleCard.name} (${sampleCard.uidHex})")
        triggerMascot(MascotMood.HAPPY, "Загружен тестовый дамп: ${sampleCard.name}")
    }

    fun toggleCardEmulation(card: NfcCardModel?) {
        if (nfcManager.isEmulating.value) {
            nfcManager.stopEmulation()
            addLog("[NFC-HCE] Emulation stopped")
            triggerMascot(MascotMood.IDLE, "Эмуляция NFC остановлена")
        } else if (card != null) {
            nfcManager.startEmulation(card)
            addLog("[NFC-HCE] Emulation started for '${card.name}' UID: ${card.uidHex}")
            triggerMascot(MascotMood.TRANSMIT, "Эмулируется карта: ${card.name}")
        }
    }

    fun setReaderActive(active: Boolean) {
        _isReaderActive.value = active
        if (active) {
            addLog("[NFC] Reader mode activated. Поднесите карту к задней панели смартфона.")
            triggerMascot(MascotMood.SCANNING, "Сканирование NFC 13.56 МГц...")
        } else {
            addLog("[NFC] Reader mode suspended.")
        }
    }

    // --- Sub-GHz Methods ---
    fun updateSubGhzParams(freq: Double? = null, proto: String? = null, mod: String? = null, key: String? = null) {
        freq?.let { _subGhzFrequency.value = it }
        proto?.let { _subGhzProtocol.value = it }
        mod?.let { _subGhzModulation.value = it }
        key?.let { _subGhzHexKey.value = it }
        synthesizeSubGhzSignal()
    }

    fun synthesizeSubGhzSignal() {
        val signal = SubGhzLabEngine.synthesizeSignal(
            _subGhzFrequency.value,
            _subGhzProtocol.value,
            _subGhzModulation.value,
            _subGhzHexKey.value
        )
        _currentSignal.value = signal
        _spectrumPoints.value = SubGhzLabEngine.generateSimulatedSpectrum(_subGhzFrequency.value)
        addLog("[SUB-GHZ] Synthesized: ${_subGhzProtocol.value} at ${_subGhzFrequency.value} MHz (${signal.pulses.size} pulses)")
    }

    fun startSimulatedTransmission() {
        if (_isTransmitting.value) return
        viewModelScope.launch {
            _isTransmitting.value = true
            addLog("[SUB-GHZ-TX] Эмуляция радиопередачи: ${_subGhzFrequency.value} MHz, Протокол: ${_subGhzProtocol.value}")
            triggerMascot(MascotMood.TRANSMIT, "Эмуляция передачи Sub-GHz...")
            repeat(4) { idx ->
                delay(350)
                addLog("[SUB-GHZ-TX] Frame ${idx + 1}/4: Packet [${_subGhzHexKey.value}] Sent (Virtual RF)")
            }
            delay(200)
            _isTransmitting.value = false
            addLog("[SUB-GHZ-TX] Передача завершена (виртуальный синтез без аппаратного радиоизлучения)")
            triggerMascot(MascotMood.HAPPY, "Пакет успешно эмулирован!")
        }
    }

    fun decodeRawInput(raw: String) {
        _rawDecoderInput.value = raw
        val decoded = SubGhzLabEngine.decodeRawTimings(raw)
        _rawDecoderOutput.value = decoded
        addLog("[SUB-GHZ-RAW] Decoded: ${decoded.second} (Bits: ${decoded.first})")
    }

    // --- BadUSB Methods ---
    fun selectDuckyTemplate(template: DuckyScriptItem) {
        _selectedScript.value = template
        _scriptEditorContent.value = template.scriptContent
        addLog("[BADUSB] Loaded script template: ${template.title}")
    }

    fun updateScriptContent(content: String) {
        _scriptEditorContent.value = content
    }

    fun insertDuckySnippet(snippet: String) {
        val current = _scriptEditorContent.value
        _scriptEditorContent.value = if (current.isEmpty()) snippet else "$current\n$snippet"
    }

    fun runDuckySimulation() {
        viewModelScope.launch {
            addLog("[BADUSB] Запуск виртуального выполнения DuckyScript...")
            triggerMascot(MascotMood.TRANSMIT, "Имитация нажатий BadUSB HID...")
            duckyManager.runSimulation(_scriptEditorContent.value) { step ->
                addLog("[HID] Line ${step.lineNumber}: ${step.command} ${step.argument}")
            }
            addLog("[BADUSB] Симуляция завершена!")
            triggerMascot(MascotMood.HAPPY, "DuckyScript выполнен в терминале!")
        }
    }

    fun stopDuckySimulation() {
        duckyManager.stopSimulation()
        addLog("[BADUSB] Симуляция прервана пользователем")
    }

    // --- BLE Methods ---
    fun startBleScan() {
        val success = bleManager.startScan()
        addLog("[BLE] Сканирование BLE радиоэфира запущено... ${if (!success) "(Используется симуляция окружения)" else ""}")
        triggerMascot(MascotMood.SCANNING, "Поиск BLE маяков вокруг...")
    }

    fun stopBleScan() {
        bleManager.stopScan()
        addLog("[BLE] Сканирование остановлено. Найдено устройств: ${bleManager.discoveredDevices.value.size}")
        triggerMascot(MascotMood.IDLE, "Сканирование BLE завершено")
    }

    fun selectBleDevice(device: BleDeviceModel?) {
        _selectedBleDevice.value = device
    }

    fun saveBleDevice(device: BleDeviceModel) {
        bleManager.saveDevice(device)
        addLog("[BLE] MAC-адрес ${device.address} сохранен в базу Flipper Droid")
    }

    fun removeBleDevice(address: String) {
        bleManager.removeSavedDevice(address)
        addLog("[BLE] MAC-адрес $address удален из сохраненных")
    }

    // --- Firmware Methods ---
    fun applyFirmwarePack(pack: FirmwarePack) {
        firmwareManager.applyFirmware(pack)
        addLog("[FIRMWARE] Применен пак '${pack.name}' v${pack.version} (Цвета и модули обновлены)")
        triggerMascot(MascotMood.HAPPY, "Прошивка '${pack.name}' активирована!")
    }

    fun importZipFirmware(inputStream: InputStream, onComplete: (Result<FirmwarePack>) -> Unit) {
        val res = firmwareManager.parseZipFile(inputStream)
        res.onSuccess { pack ->
            addLog("[FIRMWARE-IMPORT] ZIP успешно распакован! Установлен '${pack.name}'")
            triggerMascot(MascotMood.HAPPY, "Новая прошивка импортирована!")
        }.onFailure { err ->
            addLog("[FIRMWARE-ERR] Ошибка импорта: ${err.message}")
        }
        onComplete(res)
    }

    fun importJsonFirmware(json: String, onComplete: (Result<FirmwarePack>) -> Unit) {
        val res = firmwareManager.parseJsonString(json)
        res.onSuccess { pack ->
            addLog("[FIRMWARE-IMPORT] JSON применен! Установлен '${pack.name}'")
            triggerMascot(MascotMood.HAPPY, "Конфиг прошивки применен!")
        }.onFailure { err ->
            addLog("[FIRMWARE-ERR] Ошибка JSON: ${err.message}")
        }
        onComplete(res)
    }

    fun toggleModule(moduleId: String, enabled: Boolean) {
        firmwareManager.updateModuleStatus(moduleId, enabled)
        addLog("[FIRMWARE-CONFIG] Модуль '$moduleId' ${if (enabled) "включен" else "отключен"}")
    }

    fun updateFirmwareProtocols(protocols: List<String>) {
        firmwareManager.updateSubGhzProtocols(protocols)
        addLog("[FIRMWARE-CONFIG] Обновлен список протоколов Sub-GHz (${protocols.size} шт.)")
    }
}
