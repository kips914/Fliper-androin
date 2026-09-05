package com.example.data

import com.example.model.DuckyScriptItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DuckyExecutionStep(
    val lineNumber: Int,
    val command: String,
    val argument: String,
    val status: String,
    val typedBuffer: String
)

class DuckyScriptManager {

    val defaultTemplates = listOf(
        DuckyScriptItem(
            id = "audit_win",
            title = "Windows System Audit (Educational)",
            description = "Безопасный аудит параметров системы: версия ОС, имя хоста и сетевой адаптер.",
            targetOS = "Windows",
            scriptContent = """
REM === Flipper Droid Authorized Self-Audit ===
REM Opens Run Dialog and queries host info
DELAY 800
GUI r
DELAY 400
STRING cmd
ENTER
DELAY 600
STRING hostname & whoami & ipconfig | findstr "IPv4"
ENTER
DELAY 400
STRING echo [Audit complete. Device safe.]
ENTER
""".trimIndent()
        ),
        DuckyScriptItem(
            id = "notepad_dolphin",
            title = "Notepad Flipper Dolphin Message",
            description = "Открывает блокнот и вводит приветствие от маскота Flipper Droid.",
            targetOS = "Windows",
            scriptContent = """
REM === Flipper Droid Notepad Greeting ===
DELAY 600
GUI r
DELAY 400
STRING notepad
ENTER
DELAY 800
STRING  .-''''-.       .-''''-. 
ENTER
STRING /        \     /        \
ENTER
STRING |  (o) (o) |   |  (o) (o) |
ENTER
STRING  \   __   /     \   __   /
ENTER
STRING   `'....'`       `'....'`
ENTER
STRING [ FLIPPER DROID VIRTUAL BADUSB ACTIVE ]
ENTER
STRING Test script executed successfully via virtual HID!
ENTER
""".trimIndent()
        ),
        DuckyScriptItem(
            id = "matrix_effect",
            title = "Terminal Matrix Visual",
            description = "Запуск безопасного визуального эффекта зеленой консоли Matrix.",
            targetOS = "Windows",
            scriptContent = """
REM === Safe Matrix Console Visual ===
DELAY 600
GUI r
DELAY 400
STRING cmd /k "color 0a & echo Initializing Flipper Droid Sandbox... & title Flipper Droid Console"
ENTER
DELAY 500
STRING echo Scanning virtual interfaces... OK
ENTER
DELAY 300
STRING echo All security parameters compliant.
ENTER
""".trimIndent()
        ),
        DuckyScriptItem(
            id = "linux_diag",
            title = "Linux Kernel Diagnostic",
            description = "Вызов терминала Linux и вывод информации о ядре и архитектуре.",
            targetOS = "Linux",
            scriptContent = """
REM === Linux Self-Diagnosis Script ===
DELAY 800
CTRL ALT t
DELAY 600
STRING uname -a && uptime
ENTER
DELAY 400
STRING echo "[*] Simulated BadUSB execution on target Linux OS"
ENTER
""".trimIndent()
        )
    )

    private val _executionLogs = MutableStateFlow<List<DuckyExecutionStep>>(emptyList())
    val executionLogs: StateFlow<List<DuckyExecutionStep>> = _executionLogs.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private val _simulatedScreenText = MutableStateFlow("")
    val simulatedScreenText: StateFlow<String> = _simulatedScreenText.asStateFlow()

    fun validateSyntax(script: String): List<String> {
        val errors = mutableListOf<String>()
        val lines = script.lines()
        val validCommands = setOf(
            "REM", "DELAY", "STRING", "ENTER", "GUI", "WINDOWS",
            "CTRL", "ALT", "SHIFT", "TAB", "ESC", "DOWN", "UP",
            "LEFT", "RIGHT", "CAPSLOCK", "DELETE", "BACKSPACE", "MENU"
        )

        for ((idx, rawLine) in lines.withIndex()) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            val parts = line.split(" ", limit = 2)
            val cmd = parts[0].uppercase()

            if (cmd !in validCommands) {
                errors.add("Строка ${idx + 1}: Неизвестная команда '$cmd'")
            } else if (cmd == "DELAY") {
                if (parts.size < 2 || parts[1].trim().toIntOrNull() == null) {
                    errors.add("Строка ${idx + 1}: Укажите задержку в миллисекундах (например, DELAY 500)")
                }
            }
        }
        return errors
    }

    suspend fun runSimulation(script: String, onStep: (DuckyExecutionStep) -> Unit) {
        if (_isExecuting.value) return
        _isExecuting.value = true
        _executionLogs.value = emptyList()
        _simulatedScreenText.value = ""

        val lines = script.lines()
        val screenBuffer = StringBuilder()

        for ((idx, rawLine) in lines.withIndex()) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            val parts = line.split(" ", limit = 2)
            val cmd = parts[0].uppercase()
            val arg = if (parts.size > 1) parts[1] else ""

            when (cmd) {
                "REM" -> {
                    val step = DuckyExecutionStep(idx + 1, cmd, arg, "COMMENT", screenBuffer.toString())
                    appendStep(step)
                    onStep(step)
                    delay(50)
                }
                "DELAY" -> {
                    val ms = arg.toIntOrNull() ?: 300
                    val simDelay = ms.coerceIn(50, 1000)
                    val step = DuckyExecutionStep(idx + 1, cmd, "${ms}ms", "WAITING", screenBuffer.toString())
                    appendStep(step)
                    onStep(step)
                    delay(simDelay.toLong())
                }
                "STRING" -> {
                    // Simulate typing character by character
                    for (ch in arg) {
                        screenBuffer.append(ch)
                        _simulatedScreenText.value = screenBuffer.toString()
                        delay(20)
                    }
                    val step = DuckyExecutionStep(idx + 1, cmd, arg, "TYPED", screenBuffer.toString())
                    appendStep(step)
                    onStep(step)
                }
                "ENTER" -> {
                    screenBuffer.append("\n")
                    _simulatedScreenText.value = screenBuffer.toString()
                    val step = DuckyExecutionStep(idx + 1, cmd, "<RETURN>", "PRESSED", screenBuffer.toString())
                    appendStep(step)
                    onStep(step)
                    delay(150)
                }
                "GUI", "WINDOWS" -> {
                    val hotkey = if (arg.isNotEmpty()) "WIN + $arg" else "WIN"
                    val step = DuckyExecutionStep(idx + 1, cmd, hotkey, "HOTKEY", screenBuffer.toString())
                    appendStep(step)
                    onStep(step)
                    delay(200)
                }
                "CTRL", "ALT", "SHIFT" -> {
                    val hotkey = "$cmd $arg"
                    val step = DuckyExecutionStep(idx + 1, cmd, hotkey, "COMBO", screenBuffer.toString())
                    appendStep(step)
                    onStep(step)
                    delay(200)
                }
                else -> {
                    val step = DuckyExecutionStep(idx + 1, cmd, arg, "EXECUTED", screenBuffer.toString())
                    appendStep(step)
                    onStep(step)
                    delay(100)
                }
            }
        }

        _isExecuting.value = false
    }

    private fun appendStep(step: DuckyExecutionStep) {
        _executionLogs.value = _executionLogs.value + step
    }

    fun stopSimulation() {
        _isExecuting.value = false
    }
}
