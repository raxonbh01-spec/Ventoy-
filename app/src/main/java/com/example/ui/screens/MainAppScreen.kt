package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.DistroCatalogEntity
import com.example.data.db.PayloadEntity
import com.example.data.db.UsbDriveEntity
import com.example.ui.components.AddPayloadDialog
import com.example.ui.components.DistroCatalogSheet
import com.example.ui.components.HashVerifierDialog
import com.example.ui.components.InitVentoyDialog
import com.example.ui.components.NonRootGuideDialog
import com.example.ui.components.PayloadItemCard
import com.example.ui.components.TransferProgressDialog
import com.example.ui.components.UsbDriveCard
import com.example.ui.components.VentoyBootSimulator
import com.example.ui.components.VentoyConfigEditor
import com.example.ui.components.formatFileSize
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.VentoyCyan
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.VentoyViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: VentoyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val payloads by viewModel.allPayloads.collectAsStateWithLifecycle()
    val drives by viewModel.allDrives.collectAsStateWithLifecycle()
    val distros by viewModel.allDistros.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Storage Access Framework DocumentTree launcher for USB drive
    val usbTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) {}
            viewModel.onUsbDocumentTreeSelected(uri)
        }
    }

    // File picker launcher to copy ISO from phone storage to USB
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = getFileNameFromUri(context, uri) ?: "bootable_image.iso"
            viewModel.copyFileToUsb(uri, fileName)
        }
    }

    // Handle Toast notifications
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VentoyCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Usb,
                                contentDescription = null,
                                tint = VentoyCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Ventoy",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = VentoyCyan
                                )
                                Text(
                                    text = " MultiBoot",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Non-Root OTG Manager",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = NeonEmerald,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setShowNonRootGuideDialog(true) },
                        modifier = Modifier.testTag("help_guide_topbar_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help Guide",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleAppTheme() },
                        modifier = Modifier.testTag("theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = uiState.currentTab == tab
                    val icon = when (tab) {
                        AppTab.OVERVIEW -> Icons.Default.Storage
                        AppTab.PAYLOADS -> Icons.Default.Folder
                        AppTab.CONFIG -> Icons.Default.Tune
                        AppTab.SIMULATOR -> Icons.Default.PlayArrow
                        AppTab.CATALOG -> Icons.Default.CloudDownload
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(tab) },
                        icon = {
                            if (tab == AppTab.PAYLOADS && payloads.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = VentoyCyan,
                                            contentColor = Color.Black
                                        ) {
                                            Text(payloads.size.toString())
                                        }
                                    }
                                ) {
                                    Icon(icon, contentDescription = tab.title)
                                }
                            } else {
                                Icon(icon, contentDescription = tab.title)
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF002B38),
                            selectedTextColor = VentoyCyan,
                            indicatorColor = VentoyCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (uiState.currentTab == AppTab.PAYLOADS) {
                ExtendedFloatingActionButton(
                    onClick = {
                        try {
                            filePickerLauncher.launch(arrayOf("*/*", "application/x-iso9660-image", "application/octet-stream"))
                        } catch (_: Exception) {
                            filePickerLauncher.launch(arrayOf("*/*"))
                        }
                    },
                    icon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                    text = { Text("Copy ISO to USB", fontWeight = FontWeight.Bold) },
                    containerColor = VentoyCyan,
                    contentColor = Color(0xFF002B38),
                    modifier = Modifier.testTag("copy_iso_fab")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = uiState.currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_content"
            ) { tab ->
                when (tab) {
                    AppTab.OVERVIEW -> {
                        OverviewTabContent(
                            drive = uiState.activeDrive,
                            hardwareDevices = uiState.hardwareDevices,
                            payloads = payloads,
                            onSelectUsb = { usbTreeLauncher.launch(null) },
                            onInitVentoy = { viewModel.setShowInitDialog(true) },
                            onShowGuide = { viewModel.setShowNonRootGuideDialog(true) },
                            onRefreshHardware = { viewModel.refreshHardwareUsb() },
                            onNavigateToPayloads = { viewModel.setTab(AppTab.PAYLOADS) },
                            onNavigateToSimulator = { viewModel.setTab(AppTab.SIMULATOR) }
                        )
                    }

                    AppTab.PAYLOADS -> {
                        PayloadsTabContent(
                            payloads = payloads,
                            searchQuery = uiState.searchQuery,
                            selectedFamily = uiState.selectedFamilyFilter,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onFamilyChange = { viewModel.setFamilyFilter(it) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onVerifyHash = { viewModel.startHashVerification(it) },
                            onBootModeChange = { payload, mode -> viewModel.updateBootMode(payload, mode) },
                            onBootPreview = {
                                viewModel.setTab(AppTab.SIMULATOR)
                            },
                            onDelete = { viewModel.deletePayload(it) },
                            onAddManual = { viewModel.setShowAddPayloadDialog(true) },
                            onCopyFromStorage = {
                                try {
                                    filePickerLauncher.launch(arrayOf("*/*", "application/x-iso9660-image"))
                                } catch (_: Exception) {
                                    filePickerLauncher.launch(arrayOf("*/*"))
                                }
                            }
                        )
                    }

                    AppTab.CONFIG -> {
                        VentoyConfigEditor(
                            config = uiState.ventoyConfig,
                            selectedThemePreset = uiState.selectedThemePreset,
                            onThemePresetSelect = { viewModel.selectThemePreset(it) },
                            onSaveConfig = { viewModel.updateVentoyConfig(it) },
                            jsonStringPreview = uiState.activeDrive?.jsonConfigRaw ?: ""
                        )
                    }

                    AppTab.SIMULATOR -> {
                        VentoyBootSimulator(
                            payloads = payloads,
                            activeThemePreset = uiState.selectedThemePreset,
                            onThemeSelect = { viewModel.selectThemePreset(it) }
                        )
                    }

                    AppTab.CATALOG -> {
                        DistroCatalogSheet(
                            distros = distros,
                            onAddDistroToDrive = { distro ->
                                viewModel.addManualPayload(
                                    fileName = "${distro.id}.iso",
                                    sizeGb = 3.5,
                                    notes = "${distro.name} - Downloaded via Distro Hub"
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showInitVentoyDialog) {
        InitVentoyDialog(
            onDismiss = { viewModel.setShowInitDialog(false) },
            onConfirm = { title, timeout ->
                viewModel.initializeVentoyOnDrive(title, timeout)
            }
        )
    }

    if (uiState.showAddPayloadDialog) {
        AddPayloadDialog(
            onDismiss = { viewModel.setShowAddPayloadDialog(false) },
            onConfirm = { fileName, sizeGb, notes ->
                viewModel.addManualPayload(fileName, sizeGb, notes)
            }
        )
    }

    if (uiState.showNonRootGuideDialog) {
        NonRootGuideDialog(
            onDismiss = { viewModel.setShowNonRootGuideDialog(false) }
        )
    }

    TransferProgressDialog(
        state = uiState.transferState,
        onCancel = { viewModel.cancelTransfer() }
    )

    HashVerifierDialog(
        state = uiState.hashVerification,
        onDismiss = { viewModel.dismissHashVerification() },
        onManualHashCompare = { expected ->
            val payloadId = uiState.hashVerification.payloadId
            val p = payloads.find { it.id == payloadId }
            if (p != null) {
                viewModel.startHashVerification(p, expected)
            }
        }
    )
}

@Composable
private fun OverviewTabContent(
    drive: UsbDriveEntity?,
    hardwareDevices: List<com.example.data.usb.UsbStorageManager.DetectedUsbHardware>,
    payloads: List<PayloadEntity>,
    onSelectUsb: () -> Unit,
    onInitVentoy: () -> Unit,
    onShowGuide: () -> Unit,
    onRefreshHardware: () -> Unit,
    onNavigateToPayloads: () -> Unit,
    onNavigateToSimulator: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UsbDriveCard(
                drive = drive,
                hardwareDevices = hardwareDevices,
                onSelectUsbClick = onSelectUsb,
                onInitVentoyClick = onInitVentoy,
                onShowGuideClick = onShowGuide,
                onRefreshHardwareClick = onRefreshHardware
            )
        }

        // Quick Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Bootable ISOs",
                    value = "${payloads.size}",
                    subtitle = "${payloads.count { it.isVerified }} verified hashes",
                    icon = Icons.Default.Folder,
                    accentColor = VentoyCyan,
                    modifier = Modifier.weight(1f).clickable { onNavigateToPayloads() }
                )

                MetricCard(
                    title = "Partition Table",
                    value = drive?.partitionScheme?.split(" ")?.first() ?: "GPT",
                    subtitle = drive?.fileSystem ?: "exFAT Ventoy",
                    icon = Icons.Default.DeveloperBoard,
                    accentColor = NeonEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Action Hero Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interactive Boot Simulator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Test your Multi-Boot USB GRUB2 menu layout & distro entries before plugging into PC.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onNavigateToSimulator,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VentoyCyan,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preview", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Hardware Devices Inspection
        if (hardwareDevices.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Hardware USB Devices Detected (${hardwareDevices.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    hardwareDevices.forEach { dev ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cable,
                                    contentDescription = null,
                                    tint = if (dev.isMassStorage) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${dev.productName} (${dev.manufacturer})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "VID: ${dev.vendorId} • PID: ${dev.productId} • Class: ${if (dev.isMassStorage) "Mass Storage" else "USB Device"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PayloadsTabContent(
    payloads: List<PayloadEntity>,
    searchQuery: String,
    selectedFamily: String,
    onSearchChange: (String) -> Unit,
    onFamilyChange: (String) -> Unit,
    onFavoriteToggle: (PayloadEntity) -> Unit,
    onVerifyHash: (PayloadEntity) -> Unit,
    onBootModeChange: (PayloadEntity, String) -> Unit,
    onBootPreview: () -> Unit,
    onDelete: (PayloadEntity) -> Unit,
    onAddManual: () -> Unit,
    onCopyFromStorage: () -> Unit
) {
    val families = listOf("All", "Linux", "Windows", "Rescue", "Disk Utility")

    val filtered = payloads.filter { item ->
        val matchesFamily = selectedFamily == "All" || item.osFamily.contains(selectedFamily, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
                item.fileName.contains(searchQuery, ignoreCase = true) ||
                item.distroName.contains(searchQuery, ignoreCase = true) ||
                item.osFamily.contains(searchQuery, ignoreCase = true)
        matchesFamily && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search and Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Filter ISOs...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VentoyCyan) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).testTag("payloads_search_input")
            )

            IconButton(
                onClick = onAddManual,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                content = { Icon(Icons.Default.Add, contentDescription = "Manual Add", tint = VentoyCyan) }
            )
        }

        // Family Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(families) { fam ->
                val selected = fam == selectedFamily
                FilterChip(
                    selected = selected,
                    onClick = { onFamilyChange(fam) },
                    label = { Text(fam, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
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

        // Payload Cards List
        if (filtered.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Bootable Images Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap 'Copy ISO to USB' to transfer Ubuntu, Windows, or rescue ISOs from your phone storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onCopyFromStorage,
                        colors = ButtonDefaults.buttonColors(containerColor = VentoyCyan, contentColor = MaterialTheme.colorScheme.background)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Browse Device Files")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    PayloadItemCard(
                        payload = item,
                        onFavoriteToggle = { onFavoriteToggle(item) },
                        onVerifyHashClick = { onVerifyHash(item) },
                        onBootModeChange = { mode -> onBootModeChange(item, mode) },
                        onBootPreviewClick = onBootPreview,
                        onDeleteClick = { onDelete(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = accentColor
            )
        }
    }
}

private fun getFileNameFromUri(context: android.content.Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    result = it.getString(nameIndex)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}
