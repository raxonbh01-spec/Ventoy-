package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.UsbDriveEntity
import com.example.data.usb.UsbStorageManager
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.DarkNavyBorder
import com.example.ui.theme.DarkNavySurfaceElevated
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.VentoyCyan
import java.util.Locale

@Composable
fun UsbDriveCard(
    drive: UsbDriveEntity?,
    hardwareDevices: List<UsbStorageManager.DetectedUsbHardware>,
    onSelectUsbClick: () -> Unit,
    onInitVentoyClick: () -> Unit,
    onShowGuideClick: () -> Unit,
    onRefreshHardwareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("usb_drive_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: USB Icon, Name, Ventoy Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(VentoyCyan.copy(alpha = 0.25f), MaterialTheme.colorScheme.primaryContainer)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Usb,
                            contentDescription = "USB Drive",
                            tint = VentoyCyan,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = drive?.driveLabel ?: "No USB Selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val hwCount = hardwareDevices.count { it.isMassStorage }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (drive != null) NeonEmerald else AmberWarning)
                            )
                            Text(
                                text = if (drive != null) "OTG Connected" else if (hwCount > 0) "$hwCount USB Detected" else "Non-Root OTG Mode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onRefreshHardwareClick,
                    modifier = Modifier.testTag("refresh_hardware_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh USB",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (drive != null) {
                // Storage Usage Gauge
                val usedBytes = (drive.totalBytes - drive.freeBytes).coerceAtLeast(0L)
                val usedGb = usedBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
                val totalGb = drive.totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
                val freeGb = drive.freeBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
                val ratio = if (drive.totalBytes > 0) (usedBytes.toFloat() / drive.totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
                val animatedRatio by animateFloatAsState(targetValue = ratio, animationSpec = tween(600), label = "ratio")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Storage Allocated",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f GB of %.1f GB used", usedGb, totalGb),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%.1f GB Free", freeGb),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonEmerald
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { animatedRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = VentoyCyan,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata Chips: File System, Partition Scheme, Ventoy Version
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetadataPill(
                        label = "Ventoy v${drive.ventoyVersion}",
                        isHighlighted = drive.isVentoyInstalled,
                        icon = if (drive.isVentoyInstalled) Icons.Default.CheckCircle else Icons.Default.Warning
                    )
                    MetadataPill(
                        label = drive.fileSystem,
                        isHighlighted = false,
                        icon = Icons.Default.Storage
                    )
                    MetadataPill(
                        label = drive.partitionScheme,
                        isHighlighted = false,
                        icon = Icons.Default.Folder
                    )
                }
            } else {
                // Empty state when no USB drive is picked
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "OTG Hint",
                            tint = VentoyCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Connect a USB flash drive via OTG adapter and grant folder access using Storage Access Framework (SAF).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSelectUsbClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("select_usb_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VentoyCyan,
                        contentColor = Color(0xFF002B38)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Usb,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (drive == null) "Select USB Drive" else "Switch Drive",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (drive != null && !drive.hasVentoyDir) {
                    FilledTonalButton(
                        onClick = onInitVentoyClick,
                        modifier = Modifier.testTag("init_ventoy_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Deploy /ventoy", fontWeight = FontWeight.SemiBold)
                    }
                }

                OutlinedButton(
                    onClick = onShowGuideClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("non_root_guide_btn")
                ) {
                    Text("Non-Root Guide", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun MetadataPill(
    label: String,
    isHighlighted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isHighlighted) VentoyCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background,
        border = BorderStroke(
            0.8.dp,
            if (isHighlighted) VentoyCyan.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isHighlighted) VentoyCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                color = if (isHighlighted) VentoyCyan else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
