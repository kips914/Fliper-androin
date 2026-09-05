package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.NfcCardModel
import com.example.nfc.FlipperHostApduService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class NfcStorageManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("flipper_droid_nfc", Context.MODE_PRIVATE)

    private val _savedCards = MutableStateFlow<List<NfcCardModel>>(loadCards())
    val savedCards: StateFlow<List<NfcCardModel>> = _savedCards.asStateFlow()

    private val _activeEmulatingCard = MutableStateFlow<NfcCardModel?>(null)
    val activeEmulatingCard: StateFlow<NfcCardModel?> = _activeEmulatingCard.asStateFlow()

    private val _isEmulating = MutableStateFlow(false)
    val isEmulating: StateFlow<Boolean> = _isEmulating.asStateFlow()

    init {
        if (_savedCards.value.isEmpty()) {
            val sampleCards = listOf(
                NfcCardModel(
                    id = "sample_mifare_1k",
                    name = "Офисный пропуск (Mifare Classic 1K)",
                    uidHex = "7A:B4:9C:12",
                    techList = listOf("NfcA", "MifareClassic"),
                    standard = "ISO 14443-3A (13.56 MHz)",
                    atqaHex = "00:04",
                    sakHex = "08",
                    payloadHex = "Sector 00: 7AB49C12 90 08 04 00 62 63 64 65 66",
                    parsedNdef = "Badge ID #48291 [Office North Wing]"
                ),
                NfcCardModel(
                    id = "sample_ntag215",
                    name = "Amiibo / NTAG215 NFC Tag",
                    uidHex = "04:52:8E:1A:7F:60:80",
                    techList = listOf("NfcA", "Ndef", "MifareUltralight"),
                    standard = "ISO 14443-3A (13.56 MHz)",
                    atqaHex = "00:44",
                    sakHex = "00",
                    payloadHex = "NTAG215 (504 bytes RW memory)",
                    parsedNdef = "URI: https://flipperzero.one"
                ),
                NfcCardModel(
                    id = "sample_isodep",
                    name = "Транспортная карта (ISO-DEP 14443-4)",
                    uidHex = "1D:44:81:CC",
                    techList = listOf("NfcA", "IsoDep"),
                    standard = "ISO 14443-4 (NFC-A)",
                    atqaHex = "03:44",
                    sakHex = "20",
                    payloadHex = "AID: A0000000031010 [Transit Application]",
                    parsedNdef = "Pass Token: Valid until 2027"
                )
            )
            _savedCards.value = sampleCards
            persistCards(sampleCards)
        }
    }

    fun saveCard(card: NfcCardModel) {
        val updated = _savedCards.value.filter { it.id != card.id } + card
        _savedCards.value = updated
        persistCards(updated)
    }

    fun deleteCard(cardId: String) {
        val updated = _savedCards.value.filter { it.id != cardId }
        _savedCards.value = updated
        persistCards(updated)
        if (_activeEmulatingCard.value?.id == cardId) {
            stopEmulation()
        }
    }

    fun startEmulation(card: NfcCardModel) {
        _activeEmulatingCard.value = card
        _isEmulating.value = true
        FlipperHostApduService.isEmulating = true
        FlipperHostApduService.emulatedCardUid = card.uidHex.replace(":", "")
        FlipperHostApduService.emulatedCardPayload = if (card.parsedNdef.isNotEmpty()) card.parsedNdef else card.name
    }

    fun stopEmulation() {
        _activeEmulatingCard.value = null
        _isEmulating.value = false
        FlipperHostApduService.isEmulating = false
    }

    private fun loadCards(): List<NfcCardModel> {
        val raw = prefs.getString("saved_nfc_cards", "[]") ?: "[]"
        val list = mutableListOf<NfcCardModel>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val techs = mutableListOf<String>()
                val tArr = obj.optJSONArray("techList")
                if (tArr != null) {
                    for (j in 0 until tArr.length()) techs.add(tArr.getString(j))
                }
                list.add(
                    NfcCardModel(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        uidHex = obj.getString("uidHex"),
                        techList = techs,
                        standard = obj.getString("standard"),
                        atqaHex = obj.optString("atqaHex", ""),
                        sakHex = obj.optString("sakHex", ""),
                        payloadHex = obj.optString("payloadHex", ""),
                        parsedNdef = obj.optString("parsedNdef", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun persistCards(list: List<NfcCardModel>) {
        val arr = JSONArray()
        for (c in list) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("uidHex", c.uidHex)
            obj.put("techList", JSONArray(c.techList))
            obj.put("standard", c.standard)
            obj.put("atqaHex", c.atqaHex)
            obj.put("sakHex", c.sakHex)
            obj.put("payloadHex", c.payloadHex)
            obj.put("parsedNdef", c.parsedNdef)
            obj.put("timestamp", c.timestamp)
            arr.put(obj)
        }
        prefs.edit().putString("saved_nfc_cards", arr.toString()).apply()
    }
}
