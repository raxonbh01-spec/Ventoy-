package com.example.data.usb

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.example.data.db.PayloadEntity
import com.example.data.db.UsbDriveEntity
import com.example.data.model.DistroDetectorResult
import com.example.data.model.TransferState
import com.example.data.model.VentoyControlConfig
import com.example.data.model.VentoyJsonRoot
import com.example.data.model.VentoyThemeConfig
import com.example.data.util.DistroCatalogData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

class UsbStorageManager(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val ventoyAdapter = moshi.adapter(VentoyJsonRoot::class.java).indent("  ")

    data class DetectedUsbHardware(
        val deviceName: String,
        val manufacturer: String,
        val productName: String,
        val vendorId: Int,
        val productId: Int,
        val isMassStorage: Boolean,
        val deviceId: Int
    )

    fun getConnectedUsbDevices(): List<DetectedUsbHardware> {
        val deviceList = usbManager.deviceList
        val results = mutableListOf<DetectedUsbHardware>()
        for ((_, device) in deviceList) {
            val isMassStorage = isMassStorageDevice(device)
            results.add(
                DetectedUsbHardware(
                    deviceName = device.deviceName,
                    manufacturer = device.manufacturerName ?: "Generic USB",
                    productName = device.productName ?: "Mass Storage Device",
                    vendorId = device.vendorId,
                    productId = device.productId,
                    isMassStorage = isMassStorage,
                    deviceId = device.deviceId
                )
            )
        }
        return results
    }

    private fun isMassStorageDevice(device: UsbDevice): Boolean {
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            if (iface.interfaceClass == UsbConstants.USB_CLASS_MASS_STORAGE) {
                return true
            }
        }
        return device.deviceClass == UsbConstants.USB_CLASS_MASS_STORAGE
    }

    suspend fun scanStorageTree(treeUri: Uri): Pair<UsbDriveEntity, List<PayloadEntity>> = withContext(Dispatchers.IO) {
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
            ?: throw IllegalArgumentException("Cannot open Storage Access Framework DocumentTree")

        val files = rootDoc.listFiles()
        val payloads = mutableListOf<PayloadEntity>()
        var totalPayloadBytes = 0L
        var hasVentoyDir = false
        var ventoyJsonContent = ""

        // Check for ventoy folder
        val ventoyDir = rootDoc.findFile("ventoy")
        if (ventoyDir != null && ventoyDir.isDirectory) {
            hasVentoyDir = true
            val ventoyJson = ventoyDir.findFile("ventoy.json")
            if (ventoyJson != null && ventoyJson.isFile) {
                context.contentResolver.openInputStream(ventoyJson.uri)?.use { stream ->
                    ventoyJsonContent = stream.bufferedReader().use { it.readText() }
                }
            }
        }

        // Recursive or top-level + subfolder scan
        fun scanDirectory(dir: DocumentFile, currentPath: String) {
            val list = dir.listFiles()
            for (file in list) {
                if (file.isDirectory) {
                    if (!file.name.equals("ventoy", ignoreCase = true) && !file.name.equals(".spotlight-v100", ignoreCase = true)) {
                        scanDirectory(file, "$currentPath/${file.name}")
                    }
                } else if (file.isFile) {
                    val name = file.name ?: ""
                    val lower = name.lowercase()
                    if (isBootableExtension(lower)) {
                        val size = file.length()
                        totalPayloadBytes += size
                        val detected: DistroDetectorResult = DistroCatalogData.detectDistroFromFileName(name)
                        payloads.add(
                            PayloadEntity(
                                fileName = name,
                                relativePath = if (currentPath.isEmpty()) "/$name" else "$currentPath/$name",
                                uriString = file.uri.toString(),
                                sizeBytes = size,
                                osFamily = detected.osFamily,
                                distroName = detected.distroName,
                                version = detected.version,
                                architecture = detected.architecture,
                                bootMode = detected.suggestedBootMode,
                                notes = "Auto-detected by Ventoy Manager"
                            )
                        )
                    }
                }
            }
        }

        scanDirectory(rootDoc, "")

        val label = rootDoc.name ?: "Ventoy USB Drive"
        val driveId = treeUri.toString().hashCode().toString()
        
        // Approximate drive sizes if system doesn't provide root raw size
        val estimatedTotal = if (totalPayloadBytes > 0) {
            val gbEst = ((totalPayloadBytes / (1024L * 1024L * 1024L)) + 16) / 16 * 16
            maxOf(gbEst * 1024L * 1024L * 1024L, 32L * 1024L * 1024L * 1024L)
        } else {
            32L * 1024L * 1024L * 1024L
        }
        val estimatedFree = maxOf(estimatedTotal - totalPayloadBytes, 0L)

        val driveEntity = UsbDriveEntity(
            id = driveId,
            driveLabel = label,
            rootUriString = treeUri.toString(),
            totalBytes = estimatedTotal,
            freeBytes = estimatedFree,
            fileSystem = "exFAT",
            partitionScheme = "GPT (UEFI/BIOS)",
            ventoyVersion = "1.0.99",
            isVentoyInstalled = hasVentoyDir || payloads.isNotEmpty(),
            hasVentoyDir = hasVentoyDir,
            jsonConfigRaw = ventoyJsonContent,
            lastConnected = System.currentTimeMillis()
        )

        Pair(driveEntity, payloads)
    }

    private fun isBootableExtension(name: String): Boolean {
        return name.endsWith(".iso") ||
                name.endsWith(".img") ||
                name.endsWith(".vhd") ||
                name.endsWith(".vhdx") ||
                name.endsWith(".wim") ||
                name.endsWith(".vfd") ||
                name.endsWith(".efi")
    }

    /**
     * Initializes the Ventoy configuration structure on the USB drive:
     * Creates /ventoy folder and writes ventoy.json and default theme metadata.
     */
    suspend fun initializeVentoyStructure(
        treeUri: Uri,
        themeTitle: String = "Ventoy MultiBoot Live",
        timeoutSec: Int = 10,
        themePreset: String = "Cyberpunk Neon"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
                ?: return@withContext Result.failure(Exception("Could not open USB storage root directory"))

            var ventoyDir = rootDoc.findFile("ventoy")
            if (ventoyDir == null || !ventoyDir.isDirectory) {
                ventoyDir = rootDoc.createDirectory("ventoy")
                    ?: return@withContext Result.failure(Exception("Failed to create /ventoy directory. Check permissions."))
            }

            // Create theme directory
            var themeDir = ventoyDir.findFile("theme")
            if (themeDir == null || !themeDir.isDirectory) {
                themeDir = ventoyDir.createDirectory("theme")
            }

            // Create default ISO folder if not existing
            var isoDir = rootDoc.findFile("ISO")
            if (isoDir == null || !isoDir.isDirectory) {
                rootDoc.createDirectory("ISO")
            }

            // Build ventoy.json content
            val config = VentoyJsonRoot(
                control = listOf(
                    VentoyControlConfig(
                        defaultSearchRoot = "/ISO",
                        menuTimeout = timeoutSec,
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
            )

            val jsonString = ventoyAdapter.toJson(config)

            // Write or overwrite ventoy.json
            var ventoyJsonFile = ventoyDir.findFile("ventoy.json")
            if (ventoyJsonFile != null) {
                ventoyJsonFile.delete()
            }
            ventoyJsonFile = ventoyDir.createFile("application/json", "ventoy.json")
                ?: return@withContext Result.failure(Exception("Failed to create ventoy.json"))

            context.contentResolver.openOutputStream(ventoyJsonFile.uri)?.use { os ->
                os.write(jsonString.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            Result.success("Ventoy directory structure and ventoy.json created successfully!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves updated Ventoy configuration object directly into /ventoy/ventoy.json
     */
    suspend fun saveVentoyJson(treeUri: Uri, config: VentoyJsonRoot): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
                ?: return@withContext Result.failure(Exception("Invalid USB storage root"))

            var ventoyDir = rootDoc.findFile("ventoy")
            if (ventoyDir == null) {
                ventoyDir = rootDoc.createDirectory("ventoy")
                    ?: return@withContext Result.failure(Exception("Cannot create /ventoy directory"))
            }

            var jsonFile = ventoyDir.findFile("ventoy.json")
            if (jsonFile != null) {
                jsonFile.delete()
            }
            jsonFile = ventoyDir.createFile("application/json", "ventoy.json")
                ?: return@withContext Result.failure(Exception("Cannot create ventoy.json file"))

            val jsonString = ventoyAdapter.toJson(config)
            context.contentResolver.openOutputStream(jsonFile.uri)?.use { os ->
                os.write(jsonString.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Copy an ISO or bootable image from internal storage URI to USB root / ISO directory.
     * Yields real-time progress, transfer rate, and ETA.
     */
    fun copyFileToUsb(
        sourceUri: Uri,
        sourceFileName: String,
        targetTreeUri: Uri,
        subFolder: String = "ISO"
    ): Flow<TransferState> = flow {
        emit(
            TransferState(
                isTransferring = true,
                fileName = sourceFileName,
                bytesCopied = 0L,
                totalBytes = 0L,
                statusMessage = "Opening source file..."
            )
        )

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val contentResolver = context.contentResolver
            inputStream = contentResolver.openInputStream(sourceUri)
                ?: throw IllegalStateException("Could not open source file input stream")

            val totalSize = withContext(Dispatchers.IO) {
                try {
                    val cursor = contentResolver.query(sourceUri, null, null, null, null)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                            if (sizeIndex != -1) it.getLong(sizeIndex) else -1L
                        } else -1L
                    } ?: -1L
                } catch (e: Exception) {
                    -1L
                }
            }

            val rootDoc = DocumentFile.fromTreeUri(context, targetTreeUri)
                ?: throw IllegalStateException("Target USB root directory not accessible")

            var targetFolder = rootDoc
            if (subFolder.isNotEmpty()) {
                val foundSub = rootDoc.findFile(subFolder)
                targetFolder = if (foundSub != null && foundSub.isDirectory) {
                    foundSub
                } else {
                    rootDoc.createDirectory(subFolder) ?: rootDoc
                }
            }

            // If file already exists in target, delete or overwrite
            val existing = targetFolder.findFile(sourceFileName)
            existing?.delete()

            val targetFile = targetFolder.createFile("application/octet-stream", sourceFileName)
                ?: throw IllegalStateException("Failed to create destination file '$sourceFileName' on USB")

            outputStream = contentResolver.openOutputStream(targetFile.uri)
                ?: throw IllegalStateException("Could not open destination output stream")

            val buffer = ByteArray(256 * 1024) // 256 KB buffer for high USB throughput
            var bytesCopied = 0L
            var bytesRead: Int
            val startTime = System.currentTimeMillis()
            var lastUpdateTime = startTime

            emit(
                TransferState(
                    isTransferring = true,
                    fileName = sourceFileName,
                    bytesCopied = 0L,
                    totalBytes = totalSize,
                    statusMessage = "Writing to USB drive..."
                )
            )

            while (withContext(Dispatchers.IO) { inputStream.read(buffer) }.also { bytesRead = it } != -1) {
                withContext(Dispatchers.IO) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                bytesCopied += bytesRead

                val now = System.currentTimeMillis()
                if (now - lastUpdateTime >= 250) { // update 4 times per second for smooth UI
                    val elapsedSeconds = (now - startTime) / 1000.0
                    val speed = if (elapsedSeconds > 0) (bytesCopied / 1024.0 / 1024.0) / elapsedSeconds else 0.0
                    val remainingBytes = if (totalSize > bytesCopied) totalSize - bytesCopied else 0L
                    val eta = if (speed > 0) ((remainingBytes / (1024.0 * 1024.0)) / speed).toLong() else 0L

                    emit(
                        TransferState(
                            isTransferring = true,
                            fileName = sourceFileName,
                            bytesCopied = bytesCopied,
                            totalBytes = totalSize,
                            speedMbps = speed,
                            estimatedSecondsRemaining = eta,
                            statusMessage = "Copying ($speed MB/s)..."
                        )
                    )
                    lastUpdateTime = now
                }
            }

            withContext(Dispatchers.IO) {
                outputStream.flush()
            }

            val finalElapsed = (System.currentTimeMillis() - startTime) / 1000.0
            val avgSpeed = if (finalElapsed > 0) (bytesCopied / 1024.0 / 1024.0) / finalElapsed else 0.0

            emit(
                TransferState(
                    isTransferring = false,
                    fileName = sourceFileName,
                    bytesCopied = bytesCopied,
                    totalBytes = bytesCopied,
                    speedMbps = avgSpeed,
                    estimatedSecondsRemaining = 0,
                    statusMessage = "Transfer complete! ($bytesCopied bytes written)"
                )
            )

        } catch (e: Exception) {
            emit(
                TransferState(
                    isTransferring = false,
                    fileName = sourceFileName,
                    error = e.localizedMessage ?: "Unknown transfer error"
                )
            )
        } finally {
            try {
                inputStream?.close()
            } catch (_: Exception) {}
            try {
                outputStream?.close()
            } catch (_: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Calculates SHA-256 (and optional MD5) checksum of a file streaming in real-time.
     */
    fun calculateChecksum(
        uri: Uri,
        algorithm: String = "SHA-256"
    ): Flow<Pair<Int, String>> = flow {
        val digest = MessageDigest.getInstance(algorithm)
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open file for hashing")

        val totalBytes = withContext(Dispatchers.IO) {
            try {
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (sizeIndex != -1) it.getLong(sizeIndex) else -1L
                    } else -1L
                } ?: -1L
            } catch (e: Exception) {
                -1L
            }
        }

        val buffer = ByteArray(512 * 1024) // 512 KB buffer
        var readBytes: Long = 0
        var len: Int

        try {
            while (withContext(Dispatchers.IO) { inputStream.read(buffer) }.also { len = it } != -1) {
                digest.update(buffer, 0, len)
                readBytes += len
                if (totalBytes > 0) {
                    val progress = ((readBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()
                    emit(Pair(progress, ""))
                }
            }
            val hashBytes = digest.digest()
            val hexString = hashBytes.joinToString("") { "%02x".format(it) }
            emit(Pair(100, hexString))
        } finally {
            inputStream.close()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Delete an ISO payload from the USB drive via SAF
     */
    suspend fun deleteFileByUri(uriString: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)
            val doc = DocumentFile.fromSingleUri(context, uri)
                ?: return@withContext Result.failure(Exception("File not found"))
            val deleted = doc.delete()
            Result.success(deleted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
