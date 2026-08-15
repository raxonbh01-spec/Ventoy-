package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PayloadEntity
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.VentoyCyan
import java.util.Locale

@Composable
fun PayloadItemCard(
    payload: PayloadEntity,
    onFavoriteToggle: () -> Unit,
    onVerifyHashClick: () -> Unit,
    onBootModeChange: (String) -> Unit,
    onBootPreviewClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showBootModeDialog by remember { mutableStateOf(false) }

    val formattedSize = formatFileSize(payload.sizeBytes)
    val (iconColor, bgColors) = getDistroColors(payload.osFamily, payload.distroName)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("payload_card_${payload.id}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (payload.isFavorite) VentoyCyan.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // OS Icon Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(bgColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getDistroIcon(payload.osFamily),
                        contentDescription = payload.osFamily,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Distro Name & Version
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = payload.distroName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        if (payload.isFavorite) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorite",
                                tint = AmberWarning,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = payload.fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                // Favorite button
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (payload.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Pin to top",
                        tint = if (payload.isFavorite) AmberWarning else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Options Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Simulate Boot") },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = VentoyCyan) },
                            onClick = {
                                showMenu = false
                                onBootPreviewClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Verify SHA-256") },
                            leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = NeonEmerald) },
                            onClick = {
                                showMenu = false
                                onVerifyHashClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Change Boot Mode (${payload.bootMode})") },
                            leadingIcon = { Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = ElectricPurple) },
                            onClick = {
                                showMenu = false
                                showBootModeDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete from USB", color = CrimsonError) },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = CrimsonError) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata Badges Row: Size, Arch, Boot Mode, Verified Checksum
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BadgeChip(label = formattedSize, color = MaterialTheme.colorScheme.onSurfaceVariant)
                BadgeChip(label = payload.architecture, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                // Boot mode chip (clickable to change)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ElectricPurple.copy(alpha = 0.15f),
                    border = BorderStroke(0.6.dp, ElectricPurple.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { showBootModeDialog = true }
                ) {
                    Text(
                        text = payload.bootMode,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricPurple,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Integrity Hash Status
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (payload.isVerified) NeonEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background,
                    border = BorderStroke(0.6.dp, if (payload.isVerified) NeonEmerald.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.clickable { onVerifyHashClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (payload.isVerified) Icons.Default.CheckCircle else Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = if (payload.isVerified) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (payload.isVerified) "Verified" else "Check Hash",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (payload.isVerified) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showBootModeDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBootModeDialog = false },
            title = { Text("Select Boot Mode") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Configure how Ventoy handles ${payload.fileName}:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    listOf(
                        "UEFI & BIOS" to "Default Grub2 native chainloading",
                        "UEFI Only" to "Bypass MBR, force pure UEFI loader",
                        "Legacy BIOS" to "Force Legacy BIOS CSM mode",
                        "Memdisk" to "Load entire ISO into RAM before boot",
                        "Wimboot" to "Specialized Windows PE wimboot engine"
                    ).forEach { (mode, desc) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onBootModeChange(mode)
                                    showBootModeDialog = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (payload.bootMode == mode) VentoyCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (payload.bootMode == mode) VentoyCyan else MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = mode,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (payload.bootMode == mode) VentoyCyan else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showBootModeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BadgeChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes.toDouble() / (1024.0 * 1024.0)
    val gb = bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) {
        String.format(Locale.US, "%.2f GB", gb)
    } else {
        String.format(Locale.US, "%.0f MB", mb)
    }
}

private fun getDistroIcon(family: String): androidx.compose.ui.graphics.vector.ImageVector {
    val f = family.lowercase()
    return when {
        f.contains("windows") -> Icons.Default.Window
        f.contains("linux") || f.contains("security") -> Icons.Default.Terminal
        f.contains("rescue") || f.contains("disk") || f.contains("partition") -> Icons.Default.DeveloperBoard
        else -> Icons.Default.Terminal
    }
}

private fun getDistroColors(family: String, distro: String): Pair<Color, List<Color>> {
    val d = distro.lowercase()
    val f = family.lowercase()
    return when {
        d.contains("ubuntu") -> Color(0xFFE95420) to listOf(Color(0xFF330E00), Color(0xFF1E0A00))
        d.contains("kali") -> Color(0xFF3B82F6) to listOf(Color(0xFF0F172A), Color(0xFF020617))
        d.contains("arch") -> Color(0xFF1793D1) to listOf(Color(0xFF001F3F), Color(0xFF000C1A))
        d.contains("windows") -> Color(0xFF00A4EF) to listOf(Color(0xFF002244), Color(0xFF001122))
        d.contains("fedora") -> Color(0xFF51A2DA) to listOf(Color(0xFF00274C), Color(0xFF001426))
        d.contains("mint") -> Color(0xFF87CF3E) to listOf(Color(0xFF1B3800), Color(0xFF0E1F00))
        f.contains("rescue") || f.contains("partition") -> Color(0xFF10B981) to listOf(Color(0xFF063323), Color(0xFF02170F))
        else -> VentoyCyan to listOf(Color(0xFF002B38), Color(0xFF00141D))
    }
}
