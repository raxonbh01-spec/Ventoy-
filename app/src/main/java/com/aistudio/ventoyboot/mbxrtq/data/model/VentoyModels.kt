package com.aistudio.ventoyboot.mbxrtq.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Models representing Ventoy's official ventoy.json configuration structure.
 * Reference: https://www.ventoy.net/en/plugin_entry.html
 */
@JsonClass(generateAdapter = true)
data class VentoyJsonRoot(
    val control: List<VentoyControlConfig>? = null,
    val theme: VentoyThemeConfig? = null,
    val auto_install: List<VentoyAutoInstallItem>? = null,
    val persistence: List<VentoyPersistenceItem>? = null,
    val menu_alias: List<VentoyMenuAliasItem>? = null,
    val password: VentoyPasswordConfig? = null,
    val injection: List<VentoyInjectionItem>? = null
)

@JsonClass(generateAdapter = true)
data class VentoyControlConfig(
    @Json(name = "VTOY_DEFAULT_SEARCH_ROOT") val defaultSearchRoot: String? = "/ISO",
    @Json(name = "VTOY_MENU_TIMEOUT") val menuTimeout: Int? = 10,
    @Json(name = "VTOY_DEFAULT_IMAGE") val defaultImage: String? = null,
    @Json(name = "VTOY_SECONDARY_BOOT_MODE") val secondaryBootMode: String? = "Grub2",
    @Json(name = "VTOY_FILERES_TXT") val fileResTxt: String? = "0",
    @Json(name = "VTOY_MAX_SEARCH_LEVEL") val maxSearchLevel: String? = "2"
)

@JsonClass(generateAdapter = true)
data class VentoyThemeConfig(
    val file: String? = "/ventoy/theme/theme.txt",
    val gfxmode: String? = "1920x1080",
    val display_mode: String? = "GUI",
    val ventoy_left: String? = "5%",
    val ventoy_top: String? = "80%",
    val ventoy_color: String? = "#00D2FF",
    val fonts: List<String>? = listOf("/ventoy/theme/terminus-18.pf2")
)

@JsonClass(generateAdapter = true)
data class VentoyAutoInstallItem(
    val image: String,
    val template: String
)

@JsonClass(generateAdapter = true)
data class VentoyPersistenceItem(
    val image: String,
    val backend: String,
    val autosel: Int = 1
)

@JsonClass(generateAdapter = true)
data class VentoyMenuAliasItem(
    val image: String,
    val alias: String
)

@JsonClass(generateAdapter = true)
data class VentoyPasswordConfig(
    val password: String? = null,
    val password_pfx: String? = null
)

@JsonClass(generateAdapter = true)
data class VentoyInjectionItem(
    val image: String,
    val archive: String
)

enum class BootThemePreset(
    val title: String,
    val description: String,
    val resolution: String,
    val primaryColorHex: String,
    val bgGradientStart: Long,
    val bgGradientEnd: Long
) {
    CYBERPUNK_NEON("Cyberpunk Neon", "High-contrast dark terminal with electric cyan & magenta", "1920x1080", "#00D2FF", 0xFF0A0F1D, 0xFF001B2E),
    NORDIC_SLATE("Nordic Slate", "Clean Scandinavian frost with deep obsidian and arctic blue", "1920x1080", "#38BDF8", 0xFF0F172A, 0xFF1E293B),
    RETRO_MATRIX("Retro Matrix", "Phosphor emerald terminal typography and matrix rain accents", "1024x768", "#10B981", 0xFF051B11, 0xFF020D08),
    UBUNTU_AUBERGINE("Aubergine Warm", "Canonical warm purple gradient with modern orange highlights", "1920x1080", "#E95420", 0xFF2C001E, 0xFF77216F),
    KALI_DRAGON("Kali Dragon", "Cyber security dragon emblem with blood sapphire glow", "1920x1080", "#3B82F6", 0xFF0B0F19, 0xFF172554)
}

data class DistroDetectorResult(
    val distroName: String,
    val osFamily: String,
    val version: String,
    val architecture: String,
    val suggestedBootMode: String,
    val iconKey: String
)

data class TransferState(
    val isTransferring: Boolean = false,
    val fileName: String = "",
    val bytesCopied: Long = 0L,
    val totalBytes: Long = 0L,
    val speedMbps: Double = 0.0,
    val estimatedSecondsRemaining: Long = 0L,
    val statusMessage: String = "",
    val error: String? = null
)

data class VentoyReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long
)

data class VentoyReleaseInfo(
    val versionName: String,
    val tagName: String,
    val releaseTitle: String,
    val releaseDate: String,
    val changelog: String,
    val highlights: List<String>,
    val htmlUrl: String,
    val downloadUrl: String,
    val assets: List<VentoyReleaseAsset> = emptyList(),
    val isUpdateAvailable: Boolean = false
)

data class VentoyUpdateStatus(
    val isChecking: Boolean = false,
    val isUpdating: Boolean = false,
    val updateProgress: Float = 0f,
    val statusMessage: String = "",
    val latestRelease: VentoyReleaseInfo? = null,
    val updateAvailable: Boolean = false,
    val updateSuccess: Boolean = false,
    val errorMessage: String? = null
)
