package com.example

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.model.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.FlipperHeader
import com.example.ui.components.FlipperTabBar
import com.example.ui.components.TerminalConsoleBox
import com.example.ui.screens.BadUsbScreen
import com.example.ui.screens.BleScreen
import com.example.ui.screens.FirmwareScreen
import com.example.ui.screens.LegalScreen
import com.example.ui.screens.LimitationsScreen
import com.example.ui.screens.NfcScreen
import com.example.ui.screens.Rfid125Screen
import com.example.ui.screens.SubGhzScreen
import com.example.ui.theme.FlipperDroidTheme
import com.example.ui.theme.LocalFlipperColors

class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private val viewModel: MainViewModel by viewModels()
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        } catch (_: Exception) {}

        setContent {
            val currentFw by viewModel.currentFirmware.collectAsState()
            val activeTab by viewModel.activeTab.collectAsState()
            val mascotState by viewModel.mascotState.collectAsState()
            val isNfcEmulating by viewModel.nfcManager.isEmulating.collectAsState()
            val isBleScanning by viewModel.isBleScanning.collectAsState()
            val terminalLogs by viewModel.terminalLogs.collectAsState()

            FlipperDroidTheme(firmwarePack = currentFw) {
                val colors = LocalFlipperColors.current
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    modifier = Modifier.fillMaxSize(),
                    containerColor = colors.background
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(colors.background)
                    ) {
                        // Flipper Status Header & Mascot
                        FlipperHeader(
                            firmware = currentFw,
                            mascotState = mascotState,
                            isNfcActive = isNfcEmulating || viewModel.isReaderActive.collectAsState().value,
                            isBleActive = isBleScanning
                        )

                        // Terminal Tab Navigation
                        FlipperTabBar(
                            selectedTab = activeTab,
                            firmware = currentFw,
                            onTabSelected = { viewModel.selectTab(it) }
                        )

                        // Active Module View
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        ) {
                            when (activeTab) {
                                AppTab.NFC -> NfcScreen(viewModel = viewModel)
                                AppTab.RFID125 -> Rfid125Screen()
                                AppTab.BAD_USB -> BadUsbScreen(viewModel = viewModel)
                                AppTab.SUB_GHZ -> SubGhzScreen(viewModel = viewModel)
                                AppTab.BLE -> BleScreen(viewModel = viewModel)
                                AppTab.FIRMWARE -> FirmwareScreen(viewModel = viewModel)
                                AppTab.LIMITATIONS -> LimitationsScreen()
                                AppTab.LEGAL -> LegalScreen()
                            }
                        }

                        // Bottom Terminal Console Log
                        TerminalConsoleBox(
                            logs = terminalLogs,
                            onClearLogs = { viewModel.clearLogs() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            nfcAdapter?.enableReaderMode(
                this,
                this,
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V or
                        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                null
            )
        } catch (_: Exception) {}
    }

    override fun onPause() {
        super.onPause()
        try {
            nfcAdapter?.disableReaderMode(this)
        } catch (_: Exception) {}
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (tag != null) {
            runOnUiThread {
                viewModel.onTagScanned(tag)
            }
        }
    }
}
