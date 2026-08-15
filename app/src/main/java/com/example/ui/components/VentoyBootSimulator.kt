package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PayloadEntity
import com.example.data.model.BootThemePreset
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.VentoyCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VentoyBootSimulator(
    payloads: List<PayloadEntity>,
    activeThemePreset: BootThemePreset,
    onThemeSelect: (BootThemePreset) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var simulatedBootMode by remember { mutableStateOf("UEFI (x86_64)") }
    var isBooting by remember { mutableStateOf(false) }
    var bootedDistroName by remember { mutableStateOf<String?>(null) }
    var bootLogLines by remember { mutableStateOf<List<String>>(emptyList()) }
    var countdownTimer by remember { mutableIntStateOf(10) }
    var isTimerActive by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Countdown timer tick
    LaunchedEffect(isTimerActive, isBooting, countdownTimer) {
        if (isTimerActive && !isBooting && countdownTimer > 0) {
            delay(1000)
            countdownTimer -= 1
            if (countdownTimer == 0 && payloads.isNotEmpty()) {
                val item = payloads.getOrNull(selectedIndex) ?: payloads.first()
                startBootSequence(item.distroName) { isBooting = it; bootedDistroName = item.distroName }
            }
        }
    }

    // Scroll to item when index changes
    LaunchedEffect(selectedIndex) {
        if (payloads.isNotEmpty()) {
            listState.animateScrollToItem(selectedIndex.coerceIn(0, payloads.size - 1))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Theme preset bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = null,
                    tint = VentoyCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GRUB2 Theme Preset",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "${activeThemePreset.resolution} • ${activeThemePreset.title}",
                style = MaterialTheme.typography.bodySmall,
                color = VentoyCyan,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Theme Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BootThemePreset.values().take(4).forEach { preset ->
                val selected = preset == activeThemePreset
                FilterChip(
                    selected = selected,
                    onClick = { onThemeSelect(preset) },
                    label = {
                        Text(
                            text = preset.title.split(" ").first(),
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VentoyCyan.copy(alpha = 0.2f),
                        selectedLabelColor = VentoyCyan
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = if (selected) VentoyCyan else MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        }

        // The Simulated PC Boot Screen Frame
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("ventoy_screen_canvas"),
            shape = RoundedCornerShape(16.dp),
            color = Color(activeThemePreset.bgGradientStart),
            border = BorderStroke(2.dp, VentoyCyan.copy(alpha = 0.6f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(activeThemePreset.bgGradientStart),
                                Color(activeThemePreset.bgGradientEnd)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                if (isBooting) {
                    // Simulated OS Boot Console
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Terminal,
                                        contentDescription = null,
                                        tint = NeonEmerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "UEFI Chainloader: ${bootedDistroName ?: "Live OS"}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = NeonEmerald,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                IconButton(
                                    onClick = { isBooting = false; countdownTimer = 10 },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Exit Boot", tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = """
                                    [  0.000000] Linux version 6.8.0-generic (buildd@ventoy) (gcc 13.2)
                                    [  0.012489] ACPI: RSDP 0x000000007FFE0014 000024 (v02 VENTOY)
                                    [  0.024911] SecureBoot: Disabled (Ventoy Bypass Engaged)
                                    [  0.052100] Memory: 16298512K/16777216K available (RAM disk init)
                                    [  0.119024] ISO loopback mount: ${payloads.getOrNull(selectedIndex)?.fileName ?: "image.iso"}
                                    [  0.245129] Systemd[1]: Starting Live Media Switch Root...
                                    [  0.412000] [ OK ] Mounted Configuration File System.
                                    [  0.620000] [ OK ] Reached target Graphical Desktop Live Session.
                                """.trimIndent(),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF86F8C1),
                                lineHeight = 16.sp
                            )
                        }

                        // Bottom animated loading pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = VentoyCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Simulated Live Environment Running...",
                                style = MaterialTheme.typography.bodySmall,
                                color = VentoyCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    // Authentic Ventoy GRUB2 Interface
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Header Bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "VENTOY",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 2.sp,
                                        color = VentoyCyan
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "1.0.99",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                Text(
                                    text = simulatedBootMode,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonEmerald
                                )
                            }

                            Text(
                                text = "Ventoy MultiBoot Live Menu",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Center ISO Menu List
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, VentoyCyan.copy(alpha = 0.3f))
                        ) {
                            if (payloads.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No bootable ISOs found on drive.\nCopy ISO files into /ISO to preview.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    itemsIndexed(payloads) { idx, payload ->
                                        val isSelected = idx == selectedIndex
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedIndex = idx
                                                    isTimerActive = false
                                                },
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSelected) VentoyCyan.copy(alpha = 0.25f) else Color.Transparent,
                                            border = if (isSelected) BorderStroke(1.dp, VentoyCyan) else null
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = if (isSelected) "▶ " else "  ",
                                                        color = VentoyCyan,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                    Text(
                                                        text = payload.fileName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                                                        maxLines = 1
                                                    )
                                                }

                                                Text(
                                                    text = payload.bootMode,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = if (isSelected) VentoyCyan else Color.White.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom Ventoy GRUB2 Hotkeys & Countdown
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Use ↑ and ↓ keys to select an entry",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )

                                if (isTimerActive && countdownTimer > 0) {
                                    Text(
                                        text = "Booting in ${countdownTimer}s...",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonEmerald
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "[Enter] Boot  [Ctrl+R] Memdisk  [Ctrl+W] Wimboot  [F1] Help  [F5] Tools",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = VentoyCyan.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Virtual Hardware Control Bar (Up, Down, Boot Mode, Enter)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    isTimerActive = false
                    if (payloads.isNotEmpty()) {
                        selectedIndex = (selectedIndex - 1 + payloads.size) % payloads.size
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = VentoyCyan)
            }

            IconButton(
                onClick = {
                    isTimerActive = false
                    if (payloads.isNotEmpty()) {
                        selectedIndex = (selectedIndex + 1) % payloads.size
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Down", tint = VentoyCyan)
            }

            OutlinedButton(
                onClick = {
                    simulatedBootMode = if (simulatedBootMode.contains("UEFI")) "Legacy BIOS (MBR)" else "UEFI (x86_64)"
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (simulatedBootMode.contains("UEFI")) "Mode: UEFI" else "Mode: BIOS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    if (payloads.isNotEmpty()) {
                        val item = payloads.getOrNull(selectedIndex) ?: payloads.first()
                        isBooting = true
                        bootedDistroName = item.distroName
                    }
                },
                modifier = Modifier.testTag("simulate_boot_action_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VentoyCyan,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Boot ISO", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun startBootSequence(distroName: String, onStateChange: (Boolean) -> Unit) {
    onStateChange(true)
}
