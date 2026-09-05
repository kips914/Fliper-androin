package com.example.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class ApduExchange(
    val timestamp: Long = System.currentTimeMillis(),
    val commandHex: String,
    val responseHex: String,
    val status: String
)

class FlipperHostApduService : HostApduService() {

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return STATUS_FAILED

        val cmdHex = bytesToHex(commandApdu)
        val activeUid = emulatedCardUid.ifEmpty { "04A1B2C3D4E5" }
        val activePayload = emulatedCardPayload.ifEmpty { "FLIPPER_DROID_VIRTUAL_TAG" }

        // Standard SELECT AID APDU starts with 00 A4 04 00
        val response = if (commandApdu.size >= 4 &&
            commandApdu[0] == 0x00.toByte() &&
            commandApdu[1] == 0xA4.toByte() &&
            commandApdu[2] == 0x04.toByte()
        ) {
            val payloadBytes = activePayload.toByteArray(Charsets.UTF_8)
            val result = ByteArray(payloadBytes.size + 2)
            System.arraycopy(payloadBytes, 0, result, 0, payloadBytes.size)
            result[result.size - 2] = 0x90.toByte()
            result[result.size - 1] = 0x00.toByte()
            result
        } else {
            // General Read/Get response
            val uidBytes = hexToBytes(activeUid)
            val result = ByteArray(uidBytes.size + 2)
            System.arraycopy(uidBytes, 0, result, 0, uidBytes.size)
            result[result.size - 2] = 0x90.toByte()
            result[result.size - 1] = 0x00.toByte()
            result
        }

        val respHex = bytesToHex(response)
        _apduEvents.tryEmit(
            ApduExchange(
                commandHex = cmdHex,
                responseHex = respHex,
                status = "APDU EXCHANGED: ${response.size} bytes returned"
            )
        )

        return response
    }

    override fun onDeactivated(reason: Int) {
        val reasonStr = when (reason) {
            DEACTIVATION_LINK_LOSS -> "Link Loss (Reader removed)"
            DEACTIVATION_DESELECTED -> "Deselected by Reader"
            else -> "Deactivated ($reason)"
        }
        _apduEvents.tryEmit(
            ApduExchange(
                commandHex = "-",
                responseHex = "-",
                status = reasonStr
            )
        )
    }

    companion object {
        private val STATUS_FAILED = byteArrayOf(0x6F.toByte(), 0x00.toByte())
        
        var isEmulating: Boolean = false
        var emulatedCardUid: String = "04A1B2C3D4E5"
        var emulatedCardPayload: String = "FLIPPER_DROID_TOKEN_001"

        private val _apduEvents = MutableSharedFlow<ApduExchange>(extraBufferCapacity = 50)
        val apduEvents = _apduEvents.asSharedFlow()

        fun bytesToHex(bytes: ByteArray): String {
            val sb = StringBuilder()
            for (b in bytes) {
                sb.append(String.format("%02X", b))
            }
            return sb.toString()
        }

        fun hexToBytes(hex: String): ByteArray {
            val clean = hex.replace(" ", "").replace(":", "")
            val len = clean.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len - 1) {
                data[i / 2] = ((Character.digit(clean[i], 16) shl 4) +
                        Character.digit(clean[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }
    }
}
