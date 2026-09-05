package com.example.data

import com.example.model.SubGhzSignal
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

object SubGhzLabEngine {

    fun synthesizeSignal(
        frequencyMhz: Double,
        protocol: String,
        modulation: String,
        hexKey: String,
        customBitLength: Int = 24
    ): SubGhzSignal {
        val cleanKey = hexKey.replace(" ", "").replace("0x", "").ifEmpty { "1A2B3C" }
        val binaryBuilder = StringBuilder()
        for (char in cleanKey) {
            val digit = Character.digit(char, 16)
            if (digit >= 0) {
                binaryBuilder.append(String.format("%4s", Integer.toBinaryString(digit)).replace(' ', '0'))
            }
        }
        val binaryStr = binaryBuilder.toString()
        val bitLength = if (customBitLength > 0) customBitLength else binaryStr.length

        val pulses = mutableListOf<Boolean>()
        var highUs = 320
        var lowUs = 640
        val descBuilder = StringBuilder()

        when {
            protocol.contains("KeeLoq", ignoreCase = true) -> {
                highUs = 400
                lowUs = 800
                descBuilder.append("KeeLoq Code-Hopping Frame (Non-Linear Feedback Shift Register).\n")
                descBuilder.append("Preamble: 12-bit alternating 400us pulses + 4ms Header gap.\n")
                descBuilder.append("32-bit Encrypted Hopping Code + 28-bit Serial Number + 4-bit Button State.")
                
                // Add preamble
                repeat(12) {
                    pulses.add(true)
                    pulses.add(false)
                }
                // Add header gap
                repeat(4) { pulses.add(false) }

                // Add PWM bits
                for (bit in binaryStr.take(bitLength)) {
                    if (bit == '1') {
                        pulses.add(true)
                        pulses.add(true)
                        pulses.add(false)
                    } else {
                        pulses.add(true)
                        pulses.add(false)
                        pulses.add(false)
                    }
                }
            }
            protocol.contains("Came", ignoreCase = true) -> {
                highUs = 320
                lowUs = 640
                descBuilder.append("CAME OOK 12/24-bit Fixed Protocol (Gate / Garage Automation).\n")
                descBuilder.append("Sync Header: High 320us, Low 11520us gap (36 T).\n")
                descBuilder.append("Bit '0': 320us High / 640us Low. Bit '1': 640us High / 320us Low.")

                // Sync header
                pulses.add(true)
                repeat(12) { pulses.add(false) }

                for (bit in binaryStr.take(bitLength)) {
                    if (bit == '1') {
                        pulses.add(true)
                        pulses.add(true)
                        pulses.add(false)
                    } else {
                        pulses.add(true)
                        pulses.add(false)
                        pulses.add(false)
                    }
                }
            }
            protocol.contains("Nice", ignoreCase = true) -> {
                highUs = 700
                lowUs = 1400
                descBuilder.append("Nice Flo 12-bit Fixed Code (433.92 MHz).\n")
                descBuilder.append("Pilot pulse: High 700us, Low 25200us.\n")
                descBuilder.append("Data payload: 12 tri-state/binary keys.")

                pulses.add(true)
                repeat(16) { pulses.add(false) }

                for (bit in binaryStr.take(bitLength)) {
                    if (bit == '1') {
                        pulses.add(true)
                        pulses.add(true)
                        pulses.add(false)
                    } else {
                        pulses.add(true)
                        pulses.add(false)
                        pulses.add(false)
                    }
                }
            }
            protocol.contains("Princeton", ignoreCase = true) -> {
                highUs = 350
                lowUs = 1050
                descBuilder.append("Princeton PT2262 / PT2272 (OOK 24-bit Remote).\n")
                descBuilder.append("Sync gap: 31 clocks.\n")
                descBuilder.append("Address: 8 pins, Data: 4 pins.")

                for (bit in binaryStr.take(bitLength)) {
                    if (bit == '1') {
                        pulses.add(true)
                        pulses.add(true)
                        pulses.add(true)
                        pulses.add(false)
                    } else {
                        pulses.add(true)
                        pulses.add(false)
                        pulses.add(false)
                        pulses.add(false)
                    }
                }
                // Sync gap
                pulses.add(true)
                repeat(10) { pulses.add(false) }
            }
            else -> {
                // Generic OOK
                highUs = 250
                lowUs = 500
                descBuilder.append("Generic OOK / PWM Pulse Train (Sub-GHz Lab).\n")
                descBuilder.append("Carrier: ${frequencyMhz} MHz, Modulation: $modulation.\n")
                descBuilder.append("Payload: $cleanKey ($bitLength bits).")

                repeat(4) {
                    pulses.add(true)
                    pulses.add(false)
                }
                for (bit in binaryStr.take(bitLength)) {
                    if (bit == '1') {
                        pulses.add(true)
                        pulses.add(true)
                        pulses.add(false)
                    } else {
                        pulses.add(true)
                        pulses.add(false)
                        pulses.add(false)
                    }
                }
            }
        }

        return SubGhzSignal(
            frequencyMhz = frequencyMhz,
            protocol = protocol,
            modulation = modulation,
            bitLength = bitLength,
            hexKey = cleanKey,
            repeats = 3,
            highMicroseconds = highUs,
            lowMicroseconds = lowUs,
            pulses = pulses,
            description = descBuilder.toString()
        )
    }

    fun generateSimulatedSpectrum(centerFreq: Double, bandwidthMhz: Double = 2.0, points: Int = 50): List<Float> {
        val list = mutableListOf<Float>()
        val baseNoise = -95.0f // dBm
        for (i in 0 until points) {
            val f = (i - points / 2.0) / (points / 2.0) // -1.0 to 1.0
            val peak = exp(- (f * f) * 20.0).toFloat() * 55.0f // ~ -40 dBm at peak
            val randomNoise = Random.nextFloat() * 8.0f - 4.0f
            val value = (baseNoise + peak + randomNoise).coerceIn(-105.0f, -20.0f)
            list.add(value)
        }
        return list
    }

    fun decodeRawTimings(rawText: String): Pair<String, String> {
        val tokens = rawText.split(Regex("[,;\\s]+")).filter { it.isNotBlank() }
        val bits = StringBuilder()
        for (token in tokens) {
            val num = token.toIntOrNull() ?: continue
            if (num > 0) {
                // High pulse
                if (num > 500) bits.append("1") else bits.append("0")
            }
        }
        val bitString = bits.toString()
        if (bitString.isEmpty()) return "00000000" to "0x00"

        val hexBuilder = StringBuilder("0x")
        val chunks = bitString.chunked(4)
        for (chunk in chunks) {
            val padded = chunk.padEnd(4, '0')
            val hexChar = Integer.toHexString(padded.toInt(2)).uppercase()
            hexBuilder.append(hexChar)
        }
        return bitString to hexBuilder.toString()
    }
}
