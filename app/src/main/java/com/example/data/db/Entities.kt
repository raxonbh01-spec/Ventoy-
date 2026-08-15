package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payloads")
data class PayloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val relativePath: String,
    val uriString: String = "",
    val sizeBytes: Long,
    val osFamily: String, // "Linux", "Windows", "Rescue", "Android", "Hypervisor", "BSD", "Other"
    val distroName: String,
    val version: String,
    val architecture: String = "x86_64", // "x86_64", "arm64", "x86", "Universal"
    val bootMode: String = "UEFI & BIOS", // "UEFI & BIOS", "UEFI Only", "Legacy BIOS", "Memdisk"
    val checksumSha256: String = "",
    val isVerified: Boolean = false,
    val isFavorite: Boolean = false,
    val notes: String = "",
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "usb_drives")
data class UsbDriveEntity(
    @PrimaryKey
    val id: String, // Device name or UUID
    val driveLabel: String,
    val rootUriString: String,
    val totalBytes: Long,
    val freeBytes: Long,
    val fileSystem: String = "exFAT",
    val partitionScheme: String = "GPT (UEFI)", // "GPT (UEFI)", "MBR (Legacy/UEFI)"
    val ventoyVersion: String = "1.0.99",
    val isVentoyInstalled: Boolean = true,
    val hasVentoyDir: Boolean = true,
    val jsonConfigRaw: String = "",
    val lastConnected: Long = System.currentTimeMillis()
)

@Entity(tableName = "distro_catalog")
data class DistroCatalogEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String, // "Linux Distros", "Rescue & Recovery", "Windows Utilities", "Security & PenTest", "Hypervisors"
    val description: String,
    val latestVersion: String,
    val expectedSha256: String,
    val downloadUrl: String,
    val websiteUrl: String,
    val defaultBootMode: String = "UEFI & BIOS",
    val suggestedPersistenceMb: Int = 0,
    val iconKey: String
)
