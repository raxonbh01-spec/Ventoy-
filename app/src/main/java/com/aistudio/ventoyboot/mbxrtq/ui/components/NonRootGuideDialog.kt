package com.aistudio.ventoyboot.mbxrtq.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.ventoyboot.mbxrtq.ui.theme.ElectricPurple
import com.aistudio.ventoyboot.mbxrtq.ui.theme.NeonEmerald
import com.aistudio.ventoyboot.mbxrtq.ui.theme.VentoyCyan

@Composable
fun NonRootGuideDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VentoyCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = VentoyCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Non-Root OTG Architecture",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "How Ventoy Multi-Boot works completely without root privileges on modern Android:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                GuideStepCard(
                    stepNumber = "1",
                    title = "USB OTG & Storage Access Framework (SAF)",
                    description = "Android uses the Storage Access Framework (`ACTION_OPEN_DOCUMENT_TREE`) to grant this app full direct read, write, and streaming capabilities to any connected USB drive or SD card without needing root or dangerous block device permissions.",
                    icon = Icons.Default.Cable,
                    accentColor = VentoyCyan
                )

                GuideStepCard(
                    stepNumber = "2",
                    title = "Direct ISO File Copying",
                    description = "Ventoy's magic is that you never need to re-format or re-burn drives when adding new OSes! Just copy any `.iso`, `.img`, or `.vhd` file directly into the `/ISO` folder on your USB drive. Ventoy's bootloader detects them on the fly.",
                    icon = Icons.Default.Folder,
                    accentColor = NeonEmerald
                )

                GuideStepCard(
                    stepNumber = "3",
                    title = "Plugin & Theme Configuration (ventoy.json)",
                    description = "This app acts as a complete visual editor for Ventoy's native plugin system: theme styling, timeout, auto-installation unattend XMLs, persistence dat allocations, and menu locks are saved directly to `/ventoy/ventoy.json`.",
                    icon = Icons.Default.Code,
                    accentColor = ElectricPurple
                )

                GuideStepCard(
                    stepNumber = "4",
                    title = "Preparing a Brand New Ventoy USB",
                    description = "To initialize a blank USB drive for the very first time with Ventoy bootloader:\n• Option A: Plug USB into PC once and run Ventoy installer (MBR or GPT).\n• Option B: On Android, use standard OTG with WebUSB flashing or termux dd.\n• Once formatted, all future ISO management is 100% portable on your phone!",
                    icon = Icons.Default.DeveloperBoard,
                    accentColor = VentoyCyan
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VentoyCyan,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("Got It")
            }
        }
    )
}

@Composable
private fun GuideStepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
