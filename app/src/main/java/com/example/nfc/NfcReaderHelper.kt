package com.example.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import com.example.model.NfcCardModel
import java.nio.charset.Charset

object NfcReaderHelper {

    fun parseTag(tag: Tag): NfcCardModel {
        val idBytes = tag.id ?: byteArrayOf()
        val uidHex = formatHex(idBytes)
        val techList = tag.techList.map { it.substringAfterLast(".") }

        var atqaHex = ""
        var sakHex = ""
        val standard = detectStandard(techList)

        // Try NfcA
        val nfcA = NfcA.get(tag)
        if (nfcA != null) {
            atqaHex = formatHex(nfcA.atqa)
            sakHex = String.format("%02X", nfcA.sak)
        }

        // Try NDEF
        var parsedNdef = ""
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val ndefMessage = ndef.cachedNdefMessage ?: ndef.ndefMessage
                if (ndefMessage != null) {
                    parsedNdef = parseNdefMessage(ndefMessage)
                }
                ndef.close()
            } catch (_: Exception) {
                // Ignore read failures on secure tags
            }
        }

        val cardName = when {
            techList.contains("MifareClassic") -> "MIFARE Classic 1K"
            techList.contains("MifareUltralight") -> "MIFARE Ultralight"
            techList.contains("IsoDep") -> "ISO-DEP (14443-4)"
            techList.contains("Ndef") -> "NFC Forum Type Tag"
            else -> "ISO 14443-A Card"
        }

        return NfcCardModel(
            id = uidHex.ifEmpty { System.currentTimeMillis().toString() },
            name = cardName,
            uidHex = uidHex.ifEmpty { "UNKNOWN_UID" },
            techList = techList,
            standard = standard,
            atqaHex = atqaHex,
            sakHex = sakHex,
            payloadHex = if (parsedNdef.isNotEmpty()) parsedNdef else "Raw UID: $uidHex",
            parsedNdef = parsedNdef
        )
    }

    private fun detectStandard(techList: List<String>): String {
        return when {
            techList.contains("IsoDep") -> "ISO 14443-4 (NFC-A / DESFire)"
            techList.contains("NfcA") -> "ISO 14443-3A (13.56 MHz)"
            techList.contains("NfcB") -> "ISO 14443-3B (13.56 MHz)"
            techList.contains("NfcF") -> "JIS 6319-4 (FeliCa 13.56 MHz)"
            techList.contains("NfcV") -> "ISO 15693 (Vicinity 13.56 MHz)"
            else -> "NFC High Frequency (13.56 MHz)"
        }
    }

    private fun parseNdefMessage(message: NdefMessage): String {
        val records = message.records ?: return ""
        val sb = StringBuilder()
        for ((index, record) in records.withIndex()) {
            val recordText = parseRecord(record)
            sb.append("[$index] $recordText\n")
        }
        return sb.toString().trimEnd()
    }

    private fun parseRecord(record: NdefRecord): String {
        val tnf = record.tnf
        val type = String(record.type, Charsets.US_ASCII)
        val payload = record.payload ?: byteArrayOf()

        return when {
            tnf == NdefRecord.TNF_WELL_KNOWN && type == "T" -> {
                // Text Record
                if (payload.isNotEmpty()) {
                    val languageCodeLength = (payload[0].toInt() and 0x3F)
                    val textEncoding = if ((payload[0].toInt() and 0x80) == 0) Charset.forName("UTF-8") else Charset.forName("UTF-16")
                    val textStart = 1 + languageCodeLength
                    if (payload.size > textStart) {
                        val text = String(payload, textStart, payload.size - textStart, textEncoding)
                        "TEXT: $text"
                    } else "TEXT: [Empty]"
                } else "TEXT: [Empty]"
            }
            tnf == NdefRecord.TNF_WELL_KNOWN && type == "U" -> {
                // URI Record
                if (payload.isNotEmpty()) {
                    val prefix = getUriPrefix(payload[0])
                    val uri = String(payload, 1, payload.size - 1, Charsets.UTF_8)
                    "URI: $prefix$uri"
                } else "URI: [Empty]"
            }
            else -> {
                "TNF: $tnf TYPE: $type (${payload.size} bytes)"
            }
        }
    }

    private fun getUriPrefix(prefixByte: Byte): String {
        return when (prefixByte.toInt()) {
            0x01 -> "http://www."
            0x02 -> "https://www."
            0x03 -> "http://"
            0x04 -> "https://"
            0x05 -> "tel:"
            0x06 -> "mailto:"
            else -> ""
        }
    }

    fun formatHex(bytes: ByteArray): String {
        return bytes.joinToString(":") { String.format("%02X", it) }
    }
}
