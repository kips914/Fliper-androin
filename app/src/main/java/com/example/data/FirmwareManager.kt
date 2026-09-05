package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.FirmwarePack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

class FirmwareManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("flipper_droid_firmware", Context.MODE_PRIVATE)

    val defaultPacks = listOf(
        FirmwarePack(
            id = "stock",
            name = "Stock Flipper OS",
            version = "0.98.3-Release",
            author = "Flipper Devices",
            description = "Классическая оранжевая прошивка Flipper Zero с дефолтными протоколами и маскотом Дельфином.",
            primaryColorHex = "#FF8200",
            backgroundColorHex = "#0F1117",
            surfaceColorHex = "#1B1E28",
            accentColorHex = "#FFA233",
            textColorHex = "#FFFFFF",
            enabledModules = listOf("nfc", "rfid", "badusb", "subghz", "ble", "limits", "legal"),
            subGhzProtocols = listOf("KeeLoq 64bit", "Came 12bit", "Came 24bit", "Nice Flo", "Princeton PT2262", "Oregon Scientific", "Raw OOK PWM"),
            mascotStyle = "dolphin",
            terminalPrompt = "flipper@stock:~$ "
        ),
        FirmwarePack(
            id = "momentum",
            name = "Momentum Cyber",
            version = "1.1.4-CyberEd",
            author = "Momentum Community",
            description = "Кастомная киберпанк-прошивка с неоновым циановым терминалом, расширенной лабораторией Sub-GHz и разблокированными частотами.",
            primaryColorHex = "#00F0FF",
            backgroundColorHex = "#070B14",
            surfaceColorHex = "#0F1726",
            accentColorHex = "#00C8D7",
            textColorHex = "#E1F5FE",
            enabledModules = listOf("nfc", "rfid", "badusb", "subghz", "ble", "limits", "legal"),
            subGhzProtocols = listOf("KeeLoq Extended", "Security+ 2.0", "Sec+ 1.0", "Nice Flor-S", "Somfy RTS", "Faac SLH", "Princeton HD", "RAW Analyzer"),
            mascotStyle = "cyber_cat",
            terminalPrompt = "flipper@momentum:~$ "
        ),
        FirmwarePack(
            id = "unleashed",
            name = "Unleashed Acid",
            version = "2.4.0-OLED",
            author = "DarkFlipp Team",
            description = "Высококонтрастная ядовито-зеленая OLED-тема в стиле стелс-терминала с кастомными утилитами BadUSB.",
            primaryColorHex = "#39FF14",
            backgroundColorHex = "#000000",
            surfaceColorHex = "#0C140C",
            accentColorHex = "#22C55E",
            textColorHex = "#DCFCE7",
            enabledModules = listOf("nfc", "rfid", "badusb", "subghz", "ble", "limits", "legal"),
            subGhzProtocols = listOf("KeeLoq Unlocked", "Ansonic OOK", "BFT Mitto", "DoorHan FM", "Nice Smilo", "RAW Pulse Train"),
            mascotStyle = "skull",
            terminalPrompt = "root@unleashed:# "
        ),
        FirmwarePack(
            id = "vt100",
            name = "VT100 Amber CRT",
            version = "1982-Retro",
            author = "Phosphor Lab",
            description = "Ностальгический янтарный монохромный CRT-монитор с фосфорным свечением и аутентичным терминалом.",
            primaryColorHex = "#FFB000",
            backgroundColorHex = "#0E0902",
            surfaceColorHex = "#1C1304",
            accentColorHex = "#FF8F00",
            textColorHex = "#FEF3C7",
            enabledModules = listOf("nfc", "rfid", "badusb", "subghz", "ble", "limits", "legal"),
            subGhzProtocols = listOf("Standard 433.92", "Standard 868.35", "Standard 315.00", "Generic ASK/OOK"),
            mascotStyle = "robot",
            terminalPrompt = "amber@vt100:~$ "
        )
    )

    private val _currentFirmware = MutableStateFlow(loadInitialFirmware())
    val currentFirmware: StateFlow<FirmwarePack> = _currentFirmware.asStateFlow()

    private val _availablePacks = MutableStateFlow(loadSavedPacks())
    val availablePacks: StateFlow<List<FirmwarePack>> = _availablePacks.asStateFlow()

    private fun loadInitialFirmware(): FirmwarePack {
        val savedId = prefs.getString("active_firmware_id", "stock") ?: "stock"
        val customJson = prefs.getString("custom_pack_$savedId", null)
        if (customJson != null) {
            val parsed = parseJsonToPack(customJson)
            if (parsed != null) return parsed
        }
        return defaultPacks.find { it.id == savedId } ?: defaultPacks[0]
    }

    private fun loadSavedPacks(): List<FirmwarePack> {
        val list = defaultPacks.toMutableList()
        val customIds = prefs.getStringSet("custom_pack_ids", emptySet()) ?: emptySet()
        for (id in customIds) {
            val json = prefs.getString("custom_pack_$id", null)
            if (json != null) {
                parseJsonToPack(json)?.let { list.add(it) }
            }
        }
        return list
    }

    fun applyFirmware(pack: FirmwarePack) {
        prefs.edit().putString("active_firmware_id", pack.id).apply()
        _currentFirmware.value = pack
    }

    fun updateModuleStatus(moduleId: String, enabled: Boolean) {
        val current = _currentFirmware.value
        val updatedList = if (enabled) {
            (current.enabledModules + moduleId).distinct()
        } else {
            current.enabledModules.filter { it != moduleId }
        }
        val updated = current.copy(enabledModules = updatedList)
        savePack(updated)
        _currentFirmware.value = updated
    }

    fun updateSubGhzProtocols(protocols: List<String>) {
        val current = _currentFirmware.value
        val updated = current.copy(subGhzProtocols = protocols)
        savePack(updated)
        _currentFirmware.value = updated
    }

    fun updateMascot(mascot: String) {
        val current = _currentFirmware.value
        val updated = current.copy(mascotStyle = mascot)
        savePack(updated)
        _currentFirmware.value = updated
    }

    fun updateColors(primary: String, bg: String, surface: String, accent: String, text: String) {
        val current = _currentFirmware.value
        val updated = current.copy(
            primaryColorHex = primary,
            backgroundColorHex = bg,
            surfaceColorHex = surface,
            accentColorHex = accent,
            textColorHex = text
        )
        savePack(updated)
        _currentFirmware.value = updated
    }

    fun savePack(pack: FirmwarePack) {
        val json = packToJson(pack)
        val customIds = (prefs.getStringSet("custom_pack_ids", emptySet()) ?: emptySet()).toMutableSet()
        customIds.add(pack.id)
        prefs.edit()
            .putStringSet("custom_pack_ids", customIds)
            .putString("custom_pack_${pack.id}", json)
            .putString("active_firmware_id", pack.id)
            .apply()

        val updatedList = _availablePacks.value.filter { it.id != pack.id } + pack
        _availablePacks.value = updatedList
    }

    fun parseZipFile(inputStream: InputStream): Result<FirmwarePack> {
        return try {
            val zis = ZipInputStream(inputStream)
            var entry = zis.nextEntry
            var jsonContent: String? = null

            while (entry != null) {
                val name = entry.name.lowercase()
                if (name.endsWith("firmware.json") || name.endsWith("manifest.json") || name.endsWith("pack.json")) {
                    val reader = BufferedReader(InputStreamReader(zis))
                    val sb = java.lang.StringBuilder()
                    var line: String? = reader.readLine()
                    while (line != null) {
                        sb.append(line).append("\n")
                        line = reader.readLine()
                    }
                    jsonContent = sb.toString()
                    break
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
            zis.close()

            if (jsonContent != null) {
                parseJsonString(jsonContent)
            } else {
                Result.failure(Exception("В ZIP архиве не найден файл firmware.json или manifest.json"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseJsonString(jsonString: String): Result<FirmwarePack> {
        return try {
            val pack = parseJsonToPack(jsonString)
            if (pack != null) {
                savePack(pack)
                applyFirmware(pack)
                Result.success(pack)
            } else {
                Result.failure(Exception("Неверный формат JSON для Firmware Pack"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseJsonToPack(json: String): FirmwarePack? {
        return try {
            val obj = JSONObject(json)
            val id = obj.optString("id", "custom_${System.currentTimeMillis()}")
            val name = obj.optString("name", "Custom Firmware")
            val version = obj.optString("version", "1.0.0")
            val author = obj.optString("author", "User")
            val description = obj.optString("description", "Пользовательский пак оформления Flipper Droid")
            
            // Colors
            val colorsObj = obj.optJSONObject("colors")
            val primary = colorsObj?.optString("primary", obj.optString("primaryColorHex", "#FF8200")) ?: "#FF8200"
            val bg = colorsObj?.optString("background", obj.optString("backgroundColorHex", "#0C0D11")) ?: "#0C0D11"
            val surface = colorsObj?.optString("surface", obj.optString("surfaceColorHex", "#161922")) ?: "#161922"
            val accent = colorsObj?.optString("accent", obj.optString("accentColorHex", "#FFA533")) ?: "#FFA533"
            val text = colorsObj?.optString("text", obj.optString("textColorHex", "#FFFFFF")) ?: "#FFFFFF"

            // Enabled Modules
            val modulesList = mutableListOf<String>()
            val modulesArr = obj.optJSONArray("enabledModules")
            if (modulesArr != null) {
                for (i in 0 until modulesArr.length()) {
                    modulesList.add(modulesArr.getString(i))
                }
            } else {
                modulesList.addAll(listOf("nfc", "rfid", "badusb", "subghz", "ble", "limits", "legal"))
            }

            // Sub-GHz Protocols
            val protocolsList = mutableListOf<String>()
            val protoArr = obj.optJSONArray("subGhzProtocols")
            if (protoArr != null) {
                for (i in 0 until protoArr.length()) {
                    protocolsList.add(protoArr.getString(i))
                }
            } else {
                protocolsList.addAll(listOf("KeeLoq 64bit", "Came 12bit", "Nice Flo", "Princeton PT2262", "RAW OOK"))
            }

            val mascot = obj.optString("mascotStyle", "dolphin")
            val prompt = obj.optString("terminalPrompt", "flipper@droid:~$ ")

            FirmwarePack(
                id = id,
                name = name,
                version = version,
                author = author,
                description = description,
                primaryColorHex = primary,
                backgroundColorHex = bg,
                surfaceColorHex = surface,
                accentColorHex = accent,
                textColorHex = text,
                enabledModules = modulesList,
                subGhzProtocols = protocolsList,
                mascotStyle = mascot,
                terminalPrompt = prompt
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun packToJson(pack: FirmwarePack): String {
        val obj = JSONObject()
        obj.put("id", pack.id)
        obj.put("name", pack.name)
        obj.put("version", pack.version)
        obj.put("author", pack.author)
        obj.put("description", pack.description)
        obj.put("primaryColorHex", pack.primaryColorHex)
        obj.put("backgroundColorHex", pack.backgroundColorHex)
        obj.put("surfaceColorHex", pack.surfaceColorHex)
        obj.put("accentColorHex", pack.accentColorHex)
        obj.put("textColorHex", pack.textColorHex)
        obj.put("enabledModules", JSONArray(pack.enabledModules))
        obj.put("subGhzProtocols", JSONArray(pack.subGhzProtocols))
        obj.put("mascotStyle", pack.mascotStyle)
        obj.put("terminalPrompt", pack.terminalPrompt)
        return obj.toString()
    }
}
