package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.db.AppDatabase
import com.example.data.db.DistroCatalogEntity
import com.example.data.db.PayloadEntity
import com.example.data.db.UsbDriveEntity
import com.example.data.model.BootThemePreset
import com.example.data.model.TransferState
import com.example.data.model.VentoyControlConfig
import com.example.data.model.VentoyJsonRoot
import com.example.data.model.VentoyThemeConfig
import com.example.data.repository.VentoyRepository
import com.example.data.usb.UsbStorageManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val iconKey: String) {
    OVERVIEW("Drives", "drive"),
    PAYLOADS("ISOs & Boot", "payloads"),
    CONFIG("Plugins & Theme", "plugins"),
    SIMULATOR("Boot Preview", "simulator"),
    CATALOG("Distro Hub", "catalog")
}

data class HashVerificationUiState(
    val isHashing: Boolean = false,
    val payloadId: Long? = null,
    val payloadFileName: String = "",
    val progressPercent: Int = 0,
    val calculatedHash: String = "",
    val expectedHash: String = "",
    val isMatch: Boolean? = null,
    val error: String? = null
)

data class VentoyUiState(
    val currentTab: AppTab = AppTab.OVERVIEW,
    val activeDrive: UsbDriveEntity? = null,
    val hardwareDevices: List<UsbStorageManager.DetectedUsbHardware> = emptyList(),
    val searchQuery: String = "",
    val selectedFamilyFilter: String = "All",
    val isRefreshing: Boolean = false,
    val transferState: TransferState = TransferState(),
    val hashVerification: HashVerificationUiState = HashVerificationUiState(),
    val ventoyConfig: VentoyJsonRoot = VentoyJsonRoot(
        control = listOf(
            VentoyControlConfig(
                defaultSearchRoot = "/ISO",
                menuTimeout = 10,
                secondaryBootMode = "Grub2",
                fileResTxt = "0"
            )
        ),
        theme = VentoyThemeConfig(
            file = "/ventoy/theme/theme.txt",
            gfxmode = "1920x1080",
            display_mode = "GUI",
            ventoy_color = "#00D2FF"
        )
    ),
    val selectedThemePreset: BootThemePreset = BootThemePreset.CYBERPUNK_NEON,
    val isDarkTheme: Boolean = true,
    val toastMessage: String? = null,
    val showInitVentoyDialog: Boolean = false,
    val showAddPayloadDialog: Boolean = false,
    val showNonRootGuideDialog: Boolean = false
)

class VentoyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "ventoy_multiboot_db"
    ).build()

    private val usbStorageManager = UsbStorageManager(application)
    private val repository = VentoyRepository(database, usbStorageManager, application)

    private val _uiState = MutableStateFlow(VentoyUiState())
    val uiState: StateFlow<VentoyUiState> = _uiState.asStateFlow()

    val allPayloads: StateFlow<List<PayloadEntity>> = repository.allPayloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDrives: StateFlow<List<UsbDriveEntity>> = repository.allDrives
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDistros: StateFlow<List<DistroCatalogEntity>> = repository.allDistros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var transferJob: Job? = null
    private var hashingJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeCatalogIfNeeded()
            repository.seedDemoDriveIfEmpty()
            refreshHardwareUsb()
        }

        // Auto select active drive
        viewModelScope.launch {
            allDrives.collect { drives ->
                if (drives.isNotEmpty() && _uiState.value.activeDrive == null) {
                    val first = drives.first()
                    _uiState.update { it.copy(activeDrive = first) }
                    parseAndSetConfig(first.jsonConfigRaw)
                }
            }
        }
    }

    fun setTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFamilyFilter(family: String) {
        _uiState.update { it.copy(selectedFamilyFilter = family) }
    }

    fun toggleAppTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun setShowInitDialog(show: Boolean) {
        _uiState.update { it.copy(showInitVentoyDialog = show) }
    }

    fun setShowAddPayloadDialog(show: Boolean) {
        _uiState.update { it.copy(showAddPayloadDialog = show) }
    }

    fun setShowNonRootGuideDialog(show: Boolean) {
        _uiState.update { it.copy(showNonRootGuideDialog = show) }
    }

    fun refreshHardwareUsb() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val hw = repository.getConnectedHardwareUsb()
            _uiState.update { it.copy(hardwareDevices = hw, isRefreshing = false) }
        }
    }

    fun onUsbDocumentTreeSelected(treeUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                val (drive, payloads) = repository.scanUsbTree(treeUri)
                _uiState.update {
                    it.copy(
                        activeDrive = drive,
                        isRefreshing = false,
                        toastMessage = "Scanned ${drive.driveLabel} (${payloads.size} ISO payloads found)"
                    )
                }
                parseAndSetConfig(drive.jsonConfigRaw)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        toastMessage = "Error reading USB: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun initializeVentoyOnDrive(themeTitle: String, timeout: Int) {
        val drive = _uiState.value.activeDrive ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val res = repository.initializeVentoyOnDrive(Uri.parse(drive.rootUriString), themeTitle, timeout)
            if (res.isSuccess) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        showInitVentoyDialog = false,
                        toastMessage = "Ventoy structure & ventoy.json created successfully!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        toastMessage = "Initialization error: ${res.exceptionOrNull()?.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun parseAndSetConfig(rawJson: String) {
        val parsed = repository.parseVentoyJson(rawJson)
        if (parsed != null) {
            _uiState.update { it.copy(ventoyConfig = parsed) }
        }
    }

    fun updateVentoyConfig(config: VentoyJsonRoot) {
        _uiState.update { it.copy(ventoyConfig = config) }
        val drive = _uiState.value.activeDrive ?: return
        viewModelScope.launch {
            try {
                val uri = Uri.parse(drive.rootUriString)
                val res = repository.saveVentoyConfig(uri, config)
                if (res.isSuccess) {
                    showToast("ventoy.json saved to USB drive!")
                }
            } catch (e: Exception) {
                // If in demo mode, update local memory state
                showToast("Updated Ventoy configuration!")
            }
        }
    }

    fun selectThemePreset(preset: BootThemePreset) {
        _uiState.update { it.copy(selectedThemePreset = preset) }
        val current = _uiState.value.ventoyConfig
        val updatedTheme = (current.theme ?: VentoyThemeConfig()).copy(
            gfxmode = preset.resolution,
            ventoy_color = preset.primaryColorHex
        )
        updateVentoyConfig(current.copy(theme = updatedTheme))
    }

    fun copyFileToUsb(sourceUri: Uri, fileName: String) {
        val drive = _uiState.value.activeDrive
        if (drive == null) {
            showToast("Please connect or select a USB drive first")
            return
        }

        transferJob?.cancel()
        transferJob = viewModelScope.launch {
            repository.copyFileToUsb(sourceUri, fileName, Uri.parse(drive.rootUriString)).collect { state ->
                _uiState.update { it.copy(transferState = state) }
                if (!state.isTransferring && state.error == null && state.bytesCopied > 0) {
                    // Rescan
                    try {
                        repository.scanUsbTree(Uri.parse(drive.rootUriString))
                    } catch (_: Exception) {}
                    showToast("Successfully transferred $fileName to USB!")
                }
            }
        }
    }

    fun cancelTransfer() {
        transferJob?.cancel()
        _uiState.update { it.copy(transferState = TransferState()) }
        showToast("Transfer cancelled")
    }

    fun startHashVerification(payload: PayloadEntity, expectedHash: String = "") {
        _uiState.update {
            it.copy(
                hashVerification = HashVerificationUiState(
                    isHashing = true,
                    payloadId = payload.id,
                    payloadFileName = payload.fileName,
                    progressPercent = 0,
                    expectedHash = expectedHash.ifEmpty { payload.checksumSha256 }
                )
            )
        }

        if (payload.uriString.isBlank()) {
            // Simulated verify for demo files
            viewModelScope.launch {
                for (p in 10..100 step 15) {
                    kotlinx.coroutines.delay(120)
                    _uiState.update {
                        it.copy(hashVerification = it.hashVerification.copy(progressPercent = p))
                    }
                }
                val hash = payload.checksumSha256.ifEmpty {
                    "81fae63c0a5200257e0544f80e922fa37318be75c0245053075cb6eb77353f47"
                }
                val match = if (expectedHash.isNotBlank()) hash.equals(expectedHash.trim(), ignoreCase = true) else true
                _uiState.update {
                    it.copy(
                        hashVerification = it.hashVerification.copy(
                            isHashing = false,
                            progressPercent = 100,
                            calculatedHash = hash,
                            isMatch = match
                        )
                    )
                }
                repository.updatePayloadChecksum(payload, hash, match)
            }
            return
        }

        hashingJob?.cancel()
        hashingJob = viewModelScope.launch {
            try {
                val uri = Uri.parse(payload.uriString)
                repository.calculateChecksum(uri, "SHA-256").collect { (prog, hash) ->
                    if (hash.isNotEmpty()) {
                        val match = if (expectedHash.isNotBlank()) hash.equals(expectedHash.trim(), ignoreCase = true) else true
                        _uiState.update {
                            it.copy(
                                hashVerification = it.hashVerification.copy(
                                    isHashing = false,
                                    progressPercent = 100,
                                    calculatedHash = hash,
                                    isMatch = match
                                )
                            )
                        }
                        repository.updatePayloadChecksum(payload, hash, match)
                    } else {
                        _uiState.update {
                            it.copy(hashVerification = it.hashVerification.copy(progressPercent = prog))
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        hashVerification = it.hashVerification.copy(
                            isHashing = false,
                            error = e.localizedMessage ?: "Hashing failed"
                        )
                    )
                }
            }
        }
    }

    fun dismissHashVerification() {
        hashingJob?.cancel()
        _uiState.update { it.copy(hashVerification = HashVerificationUiState()) }
    }

    fun addManualPayload(fileName: String, sizeGb: Double, notes: String) {
        viewModelScope.launch {
            val bytes = (sizeGb * 1024L * 1024L * 1024L).toLong()
            repository.addManualPayload(fileName, bytes, notes)
            _uiState.update { it.copy(showAddPayloadDialog = false) }
            showToast("Added payload: $fileName")
        }
    }

    fun toggleFavorite(payload: PayloadEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(payload)
        }
    }

    fun updateBootMode(payload: PayloadEntity, bootMode: String) {
        viewModelScope.launch {
            repository.updatePayloadBootMode(payload, bootMode)
            showToast("Boot mode set to $bootMode")
        }
    }

    fun deletePayload(payload: PayloadEntity) {
        viewModelScope.launch {
            repository.deletePayload(payload)
            showToast("Deleted ${payload.fileName}")
        }
    }
}
