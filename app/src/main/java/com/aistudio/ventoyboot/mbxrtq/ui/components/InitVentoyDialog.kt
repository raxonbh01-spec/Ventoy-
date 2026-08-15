package com.aistudio.ventoyboot.mbxrtq.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.ventoyboot.mbxrtq.ui.theme.NeonEmerald
import com.aistudio.ventoyboot.mbxrtq.ui.theme.VentoyCyan

@Composable
fun InitVentoyDialog(
    onDismiss: () -> Unit,
    onConfirm: (themeTitle: String, timeout: Int) -> Unit
) {
    var themeTitle by remember { mutableStateOf("Ventoy MultiBoot Live") }
    var timeoutSec by remember { mutableFloatStateOf(10f) }

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
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = VentoyCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Deploy Ventoy Directory",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "This will create the `/ventoy` folder, initialize `ventoy.json` configuration, and prepare `/ISO` directory on your USB storage.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = themeTitle,
                    onValueChange = { themeTitle = it },
                    label = { Text("Menu Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Boot Menu Timeout",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${timeoutSec.toInt()} seconds",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = VentoyCyan
                        )
                    }
                    Slider(
                        value = timeoutSec,
                        onValueChange = { timeoutSec = it },
                        valueRange = 3f..60f,
                        steps = 57
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(themeTitle, timeoutSec.toInt()) },
                modifier = Modifier.testTag("confirm_init_ventoy_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VentoyCyan,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("Deploy Structure", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddPayloadDialog(
    onDismiss: () -> Unit,
    onConfirm: (fileName: String, sizeGb: Double, notes: String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var sizeGbStr by remember { mutableStateOf("4.5") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonEmerald.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Register Bootable ISO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Manually register an ISO, IMG, or VHD payload profile for the USB drive:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name (e.g. archlinux-2024.iso)") },
                    placeholder = { Text("ubuntu-24.04-desktop-amd64.iso") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("manual_iso_name_input")
                )

                OutlinedTextField(
                    value = sizeGbStr,
                    onValueChange = { sizeGbStr = it },
                    label = { Text("Size in Gigabytes (GB)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Distro info (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = if (fileName.isNotBlank()) fileName.trim() else "custom-os.iso"
                    val parsedSize = sizeGbStr.toDoubleOrNull() ?: 4.0
                    onConfirm(finalName, parsedSize, notes)
                },
                modifier = Modifier.testTag("confirm_add_payload_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonEmerald,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("Add Payload", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
