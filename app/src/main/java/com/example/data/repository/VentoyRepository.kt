package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.db.AppDatabase
import com.example.data.db.DistroCatalogEntity
import com.example.data.db.PayloadEntity
import com.example.data.db.UsbDriveEntity
import com.example.data.model.BootThemePreset
import com.example.data.model.DistroDetectorResult
import com.example.data.model.TransferState
import com.example.data.model.VentoyControlConfig
import com.example.data.model.VentoyJsonRoot
import com.example.data.model.VentoyThemeConfig
import com.example.data.usb.UsbStorageManager
import com.example.data.util.DistroCatalogData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VentoyRepository(
    private val database: AppDatabase,
    private val usbStorageManager: UsbStorageManager,
    private val context: Context
) {
    private val payloadDao = database.payloadDao()
    private val usbDriveDao = database.usbDriveDao()
    private val distroCatalogDao = database.distroCatalogDao()

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val ventoyAdapter = moshi.adapter(VentoyJsonRoot::class.java).indent("  ")

    val allPayloads: Flow<List<PayloadEntity>> = payloadDao.getAllPayloads()
    val allDrives: Flow<List<UsbDriveEntity>> = usbDriveDao.getAllDrives()
    val allDistros: Flow<List<DistroCatalogEntity>> = distroCatalogDao.getAllDistros()

    suspend fun initializeCatalogIfNeeded() = withContext(Dispatchers.IO) {
        val count = distroCatalogDao.getCount()
        if (count == 0) {
            distroCatalogDao.insertAll(DistroCatalogData.INITIAL_DISTRO_CATALOG)
        }
    }

    suspend fun seedDemoDriveIfEmpty() = withContext(Dispatchers.IO) {
        val sampleDrive = UsbDriveEntity(
            id = "demo_sandisk_ultra",
            driveLabel = "SanDisk Ultra MultiBoot (OTG)",
            rootUriString = "content://com.android.externalstorage.documents/tree/1234-5678%3A",
            totalBytes = 64L * 1024L * 1024L * 1024L, // 64 GB
            freeBytes = 38L * 1024L * 1024L * 1024L,  // 38 GB free
            fileSystem = "exFAT (Ventoy 1.0.99)",
            partitionScheme = "GPT (UEFI/BIOS)",
            ventoyVersion = "1.0.99",
            isVentoyInstalled = true,
            hasVentoyDir = true,
            jsonConfigRaw = """
                {
                  "control": [
                    {
                      "VTOY_DEFAULT_SEARCH_ROOT": "/ISO",
                      "VTOY_MENU_TIMEOUT": 10,
                      "VTOY_SECONDARY_BOOT_MODE": "Grub2",
                      "VTOY_FILERES_TXT": "0"
                    }
                  ],
                  "theme": {
                    "file": "/ventoy/theme/theme.txt",
                    "gfxmode": "1920x1080",
                    "display_mode": "GUI",
                    "ventoy_color": "#00D2FF"
                  }
                }
            """.trimIndent()
        )
        usbDriveDao.insertDrive(sampleDrive)

        val demoPayloads = listOf(
            PayloadEntity(
                fileName = "ubuntu-24.04-desktop-amd64.iso",
                relativePath = "/ISO/ubuntu-24.04-desktop-amd64.iso",
                sizeBytes = 6_120_000_000L,
                osFamily = "Linux",
                distroName = "Ubuntu 24.04 LTS",
                version = "24.04 LTS (Noble)",
                architecture = "x86_64",
                bootMode = "UEFI & BIOS",
                checksumSha256 = "81fae63c0a5200257e0544f80e922fa37318be75c0245053075cb6eb77353f47",
                isVerified = true,
                isFavorite = true,
                notes = "Primary workstation live environment"
            ),
            PayloadEntity(
                fileName = "Win11_23H2_English_x64v2.iso",
                relativePath = "/ISO/Win11_23H2_English_x64v2.iso",
                sizeBytes = 6_710_000_000L,
                osFamily = "Windows",
                distroName = "Windows 11 Setup 23H2",
                version = "23H2 (Build 22631)",
                architecture = "x86_64",
                bootMode = "UEFI Only",
                checksumSha256 = "361d112d75954117b355208eb4196144e5cc5669b7a42ecad54f9d0c2ee146f4",
                isVerified = true,
                isFavorite = true,
                notes = "Supports Ventoy TPM/SecureBoot bypass"
            ),
            PayloadEntity(
                fileName = "kali-linux-2024.2-live-amd64.iso",
                relativePath = "/ISO/kali-linux-2024.2-live-amd64.iso",
                sizeBytes = 4_290_000_000L,
                osFamily = "Security / Linux",
                distroName = "Kali Linux Live PenTest",
                version = "2024.2",
                architecture = "x86_64",
                bootMode = "UEFI & BIOS",
                checksumSha256 = "9e5c703b4119d7d4be217c093a1c87fce36730598816f1eb3083ce8da48464f1",
                isVerified = true,
                isFavorite = false,
                notes = "Configured with 8GB persistence overlay"
            ),
            PayloadEntity(
                fileName = "clonezilla-live-3.1.2-22-amd64.iso",
                relativePath = "/ISO/clonezilla-live-3.1.2-22-amd64.iso",
                sizeBytes = 412_000_000L,
                osFamily = "Rescue / Backup",
                distroName = "Clonezilla Live Disk Image",
                version = "3.1.2-22",
                architecture = "x86_64",
                bootMode = "UEFI & BIOS",
                checksumSha256 = "78a1bc1b47b4d1b827e69f8c057ec930bb3d4c3820fa4bc1e26177eb0c7b411d",
                isVerified = true,
                isFavorite = false,
                notes = "Bare metal disk cloning and backup"
            ),
            PayloadEntity(
                fileName = "gparted-live-1.6.0-3-amd64.iso",
                relativePath = "/ISO/gparted-live-1.6.0-3-amd64.iso",
                sizeBytes = 524_000_000L,
                osFamily = "Disk Utility",
                distroName = "GParted Live Partition Manager",
                version = "1.6.0-3",
                architecture = "x86_64",
                bootMode = "UEFI & BIOS",
                checksumSha256 = "682f7c006fb1d4187f54c379a25b1613eb5a8f5ba0ea5cc8708c353f47c34ea8",
                isVerified = true,
                isFavorite = false,
                notes = "Emergency drive partition resizing"
            ),
            PayloadEntity(
                fileName = "HBCD_PE_x64.iso",
                relativePath = "/ISO/HBCD_PE_x64.iso",
                sizeBytes = 3_120_000_000L,
                osFamily = "Windows PE",
                distroName = "Hiren's BootCD PE x64",
                version = "v1.0.8 (Win11 PE)",
                architecture = "x86_64",
                bootMode = "UEFI & BIOS",
                checksumSha256 = "e12f6764508e33f38ffb4e4f71a06cf05943b17c1bf20875e5b306bce8d4c382",
                isVerified = true,
                isFavorite = false,
                notes = "Windows emergency rescue toolkit"
            )
        )
        payloadDao.insertAll(demoPayloads)
    }

    fun getConnectedHardwareUsb() = usbStorageManager.getConnectedUsbDevices()

    suspend fun scanUsbTree(treeUri: Uri) = withContext(Dispatchers.IO) {
        val (drive, payloads) = usbStorageManager.scanStorageTree(treeUri)
        usbDriveDao.insertDrive(drive)
        if (payloads.isNotEmpty()) {
            payloadDao.clearAll()
            payloadDao.insertAll(payloads)
        }
        Pair(drive, payloads)
    }

    suspend fun initializeVentoyOnDrive(treeUri: Uri, themeTitle: String, timeout: Int) = withContext(Dispatchers.IO) {
        val res = usbStorageManager.initializeVentoyStructure(treeUri, themeTitle, timeout)
        if (res.isSuccess) {
            val (drive, payloads) = usbStorageManager.scanStorageTree(treeUri)
            usbDriveDao.insertDrive(drive)
        }
        res
    }

    suspend fun saveVentoyConfig(treeUri: Uri, config: VentoyJsonRoot) = withContext(Dispatchers.IO) {
        usbStorageManager.saveVentoyJson(treeUri, config)
    }

    fun copyFileToUsb(sourceUri: Uri, fileName: String, targetTreeUri: Uri): Flow<TransferState> {
        return usbStorageManager.copyFileToUsb(sourceUri, fileName, targetTreeUri)
    }

    fun calculateChecksum(uri: Uri, algorithm: String = "SHA-256") =
        usbStorageManager.calculateChecksum(uri, algorithm)

    suspend fun addManualPayload(
        fileName: String,
        sizeBytes: Long,
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        val detected = DistroCatalogData.detectDistroFromFileName(fileName)
        val payload = PayloadEntity(
            fileName = fileName,
            relativePath = "/ISO/$fileName",
            sizeBytes = sizeBytes,
            osFamily = detected.osFamily,
            distroName = detected.distroName,
            version = detected.version,
            architecture = detected.architecture,
            bootMode = detected.suggestedBootMode,
            notes = notes
        )
        payloadDao.insertPayload(payload)
    }

    suspend fun toggleFavorite(payload: PayloadEntity) = withContext(Dispatchers.IO) {
        payloadDao.updatePayload(payload.copy(isFavorite = !payload.isFavorite))
    }

    suspend fun updatePayloadBootMode(payload: PayloadEntity, newBootMode: String) = withContext(Dispatchers.IO) {
        payloadDao.updatePayload(payload.copy(bootMode = newBootMode))
    }

    suspend fun updatePayloadChecksum(payload: PayloadEntity, sha256: String, isVerified: Boolean) = withContext(Dispatchers.IO) {
        payloadDao.updatePayload(payload.copy(checksumSha256 = sha256, isVerified = isVerified))
    }

    suspend fun deletePayload(payload: PayloadEntity) = withContext(Dispatchers.IO) {
        if (payload.uriString.isNotEmpty()) {
            usbStorageManager.deleteFileByUri(payload.uriString)
        }
        payloadDao.deletePayload(payload)
    }

    fun parseVentoyJson(json: String): VentoyJsonRoot? {
        return try {
            if (json.isBlank()) null else ventoyAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    fun serializeVentoyJson(config: VentoyJsonRoot): String {
        return ventoyAdapter.toJson(config)
    }
}
