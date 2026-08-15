package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BootThemePreset
import com.example.data.model.VentoyControlConfig
import com.example.data.model.VentoyJsonRoot
import com.example.data.model.VentoyPasswordConfig
import com.example.data.model.VentoyThemeConfig
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.VentoyCyan

@Composable
fun VentoyConfigEditor(
    config: VentoyJsonRoot,
    selectedThemePreset: BootThemePreset,
    onThemePresetSelect: (BootThemePreset) -> Unit,
    onSaveConfig: (VentoyJsonRoot) -> Unit,
    jsonStringPreview: String,
    modifier: Modifier = Modifier
) {
    var searchRoot by remember(config) {
        mutableStateOf(config.control?.firstOrNull()?.defaultSearchRoot ?: "/ISO")
    }
    var menuTimeout by remember(config) {
        mutableFloatStateOf(config.control?.firstOrNull()?.menuTimeout?.toFloat() ?: 10f)
    }
    var secondaryBootMode by remember(config) {
        mutableStateOf(config.control?.firstOrNull()?.secondaryBootMode ?: "Grub2")
    }
    var win11BypassTpm by remember { mutableStateOf(true) }
    var passwordLockEnabled by remember(config) {
        mutableStateOf(config.password?.password != null)
    }
    var passwordText by remember(config) {
        mutableStateOf(config.password?.password ?: "")
    }
    var showRawJson by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ventoy Plugin Configuration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Visual editor for /ventoy/ventoy.json",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    val newControl = listOf(
                        VentoyControlConfig(
                            defaultSearchRoot = searchRoot,
                            menuTimeout = menuTimeout.toInt(),
                            secondaryBootMode = secondaryBootMode,
                            fileResTxt = "0"
                        )
                    )
                    val newTheme = VentoyThemeConfig(
                        file = "/ventoy/theme/theme.txt",
                        gfxmode = selectedThemePreset.resolution,
                        display_mode = "GUI",
                        ventoy_color = selectedThemePreset.primaryColorHex
                    )
                    val newPassword = if (passwordLockEnabled && passwordText.isNotBlank()) {
                        VentoyPasswordConfig(password = passwordText)
                    } else null

                    val updatedRoot = config.copy(
                        control = newControl,
                        theme = newTheme,
                        password = newPassword
                    )
                    onSaveConfig(updatedRoot)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VentoyCyan,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.testTag("save_ventoy_config_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save to USB", fontWeight = FontWeight.Bold)
            }
        }

        // Section 1: Global Control Plugin
        ConfigSectionCard(
            title = "Global Control Plugin",
            subtitle = "Directory search paths, menu countdown & boot modes",
            icon = Icons.Default.Tune,
            accentColor = VentoyCyan
        ) {
            OutlinedTextField(
                value = searchRoot,
                onValueChange = { searchRoot = it },
                label = { Text("Default ISO Search Root") },
                placeholder = { Text("/ISO") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Menu Timeout Countdown",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${menuTimeout.toInt()} seconds",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = VentoyCyan
                    )
                }
                Slider(
                    value = menuTimeout,
                    onValueChange = { menuTimeout = it },
                    valueRange = 0f..60f,
                    steps = 59
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Secondary Boot Loader Mode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Grub2", "Memdisk", "Wimboot").forEach { mode ->
                    val selected = secondaryBootMode == mode
                    FilterChip(
                        selected = selected,
                        onClick = { secondaryBootMode = mode },
                        label = { Text(mode, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VentoyCyan.copy(alpha = 0.2f),
                            selectedLabelColor = VentoyCyan
                        )
                    )
                }
            }
        }

        // Section 2: Visual Theme & Resolution
        ConfigSectionCard(
            title = "Theme & Display Mode",
            subtitle = "GRUB2 aesthetic, resolution & font scaling",
            icon = Icons.Default.ColorLens,
            accentColor = ElectricPurple
        ) {
            Text(
                text = "Theme Presets",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BootThemePreset.values().take(3).forEach { preset ->
                    val selected = preset == selectedThemePreset
                    FilterChip(
                        selected = selected,
                        onClick = { onThemePresetSelect(preset) },
                        label = { Text(preset.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurple.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricPurple
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active Resolution",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedThemePreset.resolution,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "16:9 Aspect Ratio",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Section 3: Windows 11 Bypass & Compatibility
        ConfigSectionCard(
            title = "Windows 11 Bypass & Compatibility",
            subtitle = "Automatic TPM 2.0, SecureBoot & RAM checks bypass",
            icon = Icons.Default.Security,
            accentColor = NeonEmerald
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bypass TPM & SecureBoot Check",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Allows installing Windows 11 on legacy unsupported hardware via Ventoy registry hooks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = win11BypassTpm,
                    onCheckedChange = { win11BypassTpm = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonEmerald,
                        checkedTrackColor = NeonEmerald.copy(alpha = 0.4f)
                    )
                )
            }
        }

        // Section 4: Password Protection
        ConfigSectionCard(
            title = "Menu Password Lock",
            subtitle = "Require PIN or passphrase to enter boot menu",
            icon = Icons.Default.Lock,
            accentColor = VentoyCyan
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Boot Password",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Locks USB drive payloads from unauthorized boots",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = passwordLockEnabled,
                    onCheckedChange = { passwordLockEnabled = it }
                )
            }

            if (passwordLockEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = passwordText,
                    onValueChange = { passwordText = it },
                    label = { Text("Menu Lock Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Section 5: Raw JSON Inspection
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRawJson = !showRawJson },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = VentoyCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Inspect Generated ventoy.json",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (showRawJson) "Hide" else "Show Code",
                        style = MaterialTheme.typography.bodySmall,
                        color = VentoyCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showRawJson) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF070B12),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = jsonStringPreview.ifBlank {
                                """
                                {
                                  "control": [
                                    {
                                      "VTOY_DEFAULT_SEARCH_ROOT": "$searchRoot",
                                      "VTOY_MENU_TIMEOUT": ${menuTimeout.toInt()},
                                      "VTOY_SECONDARY_BOOT_MODE": "$secondaryBootMode"
                                    }
                                  ],
                                  "theme": {
                                    "file": "/ventoy/theme/theme.txt",
                                    "gfxmode": "${selectedThemePreset.resolution}",
                                    "display_mode": "GUI"
                                  }
                                }
                                """.trimIndent()
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = VentoyCyan,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigSectionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
