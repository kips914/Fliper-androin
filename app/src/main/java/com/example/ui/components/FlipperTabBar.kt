package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTab
import com.example.model.FirmwarePack
import com.example.ui.theme.LocalFlipperColors

@Composable
fun FlipperTabBar(
    selectedTab: AppTab,
    firmware: FirmwarePack,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFlipperColors.current
    val scrollState = rememberScrollState()

    // Filter tabs by firmware enabled modules
    val visibleTabs = AppTab.values().filter { tab ->
        tab == AppTab.LIMITATIONS || tab == AppTab.LEGAL || tab == AppTab.FIRMWARE ||
                firmware.enabledModules.contains(tab.moduleId)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (tab in visibleTabs) {
            val isSelected = tab == selectedTab
            val isRfidDisabled = tab == AppTab.RFID125

            val bg = if (isSelected) colors.primary.copy(alpha = 0.25f) else colors.background
            val borderCol = if (isSelected) colors.primary else colors.border
            val textCol = when {
                isSelected -> colors.primary
                isRfidDisabled -> colors.text.copy(alpha = 0.5f)
                else -> colors.text.copy(alpha = 0.85f)
            }

            val prefix = if (isSelected) ">" else "["
            val suffix = if (isSelected) "<" else "]"

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(bg)
                    .border(1.dp, borderCol, RoundedCornerShape(4.dp))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("tab_${tab.moduleId}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$prefix ${tab.shortLabel} $suffix",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = textCol
                )
            }
        }
    }
}
