package com.aistudio.ventoyboot.mbxrtq.data.util

import com.aistudio.ventoyboot.mbxrtq.data.db.DistroCatalogEntity
import com.aistudio.ventoyboot.mbxrtq.data.model.DistroDetectorResult
import java.util.Locale

object DistroCatalogData {

    val INITIAL_DISTRO_CATALOG = listOf(
        DistroCatalogEntity(
            id = "ubuntu_desktop",
            name = "Ubuntu 24.04 LTS Desktop",
            category = "Linux Distros",
            description = "The world's most popular open source OS for desktops and laptops. Rock solid stability with GNOME desktop.",
            latestVersion = "24.04 LTS (Noble Numbat)",
            expectedSha256 = "81fae63c0a5200257e0544f80e922fa37318be75c0245053075cb6eb77353f47",
            downloadUrl = "https://releases.ubuntu.com/24.04/ubuntu-24.04-desktop-amd64.iso",
            websiteUrl = "https://ubuntu.com",
            defaultBootMode = "UEFI & BIOS",
            suggestedPersistenceMb = 4096,
            iconKey = "ubuntu"
        ),
        DistroCatalogEntity(
            id = "arch_linux",
            name = "Arch Linux Live Install",
            category = "Linux Distros",
            description = "A lightweight and flexible Linux distribution that tries to Keep It Simple. Rolling release model.",
            latestVersion = "2024.08.01",
            expectedSha256 = "d225883d6a2eaec2d5e783da3e1b7c89f5bc3a67d024aaee7f8a32a6773a4b64",
            downloadUrl = "https://geo.mirror.pkgbuild.com/iso/latest/archlinux-x86_64.iso",
            websiteUrl = "https://archlinux.org",
            defaultBootMode = "UEFI & BIOS",
            suggestedPersistenceMb = 0,
            iconKey = "arch"
        ),
        DistroCatalogEntity(
            id = "kali_linux",
            name = "Kali Linux Live",
            category = "Security & PenTest",
            description = "The most advanced Penetration Testing Distribution ever. Packed with hundreds of ethical hacking tools.",
            latestVersion = "2024.2 Live",
            expectedSha256 = "9e5c703b4119d7d4be217c093a1c87fce36730598816f1eb3083ce8da48464f1",
            downloadUrl = "https://cdimage.kali.org/current/kali-linux-live-amd64.iso",
            websiteUrl = "https://kali.org",
            defaultBootMode = "UEFI & BIOS",
            suggestedPersistenceMb = 8192,
            iconKey = "kali"
        ),
        DistroCatalogEntity(
            id = "fedora_workstation",
            name = "Fedora Workstation 40",
            category = "Linux Distros",
            description = "Cutting-edge, reliable, and user-friendly workstation powered by pure GNOME and newest kernel features.",
            latestVersion = "40",
            expectedSha256 = "4d6d1b64132ce47e9ff1364ff0f0967ee7ce1ffef00c7ad6e453c07656ebcd49",
            downloadUrl = "https://download.fedoraproject.org/pub/fedora/linux/releases/40/Workstation/x86_64/iso/Fedora-Workstation-Live-x86_64-40-1.14.iso",
            websiteUrl = "https://fedoraproject.org",
            defaultBootMode = "UEFI & BIOS",
            suggestedPersistenceMb = 4096,
            iconKey = "fedora"
        ),
        DistroCatalogEntity(
            id = "debian_netinst",
            name = "Debian 12 Bookworm",
            category = "Linux Distros",
            description = "The Universal Operating System. Known for its ultra-stable package repository and extensive architecture support.",
            latestVersion = "12.5.0",
            expectedSha256 = "66f369ee628be1f6004b9015bc2fa59f3d53bbbfbc0dd40ea05c065f41aa5709",
            downloadUrl = "https://cdimage.debian.org/debian-cd/current/amd64/iso-cd/debian-12.5.0-amd64-netinst.iso",
            websiteUrl = "https://debian.org",
            defaultBootMode = "UEFI & BIOS",
            suggestedPersistenceMb = 0,
            iconKey = "debian"
        ),
        DistroCatalogEntity(
            id = "clonezilla",
            name = "Clonezilla Live",
            category = "Rescue & Recovery",
            description = "Bare-metal partition and disk imaging/cloning tool. High performance and supports all file systems.",
            latestVersion = "3.1.2-22",
            expectedSha256 = "78a1bc1b47b4d1b827e69f8c057ec930bb3d4c3820fa4bc1e26177eb0c7b411d",
            downloadUrl = "https://downloads.sourceforge.net/clonezilla/clonezilla-live-3.1.2-22-amd64.iso",
            websiteUrl = "https://clonezilla.org",
            defaultBootMode = "UEFI & BIOS",
            suggestedPersistenceMb = 0,
            iconKey = "rescue"
        ),
        DistroCatalogEntity(
            id = "gparted_live",
            name = "GParted Live",
            category = "Rescue & Recovery",
            description = "Small bootable GNU/Linux live CD for resizing, moving, copying, creating, and deleting partitions safely.",
            latestVersion = "1.6.0-3",
            expectedSha256 = "682f7c006fb1d4187f54c379a25b1613eb5a8f5ba0ea5cc8708c353f47c34ea8",
            downloadUrl = "https://downloads.sourceforge.net/gparted/gparted-live-1.6.0-3-amd64.iso",
            websiteUrl = "https://gparted.org",
            defaultBootMode = "UEFI & BIOS",
            suggestedPersistenceMb = 0,
            iconKey = "partition"
        ),
        DistroCatalogEntity(
            id = "system_rescue",
            name = "SystemRescue",
            category = "Rescue & Recovery",
            description = "Linux system rescue toolkit available as a bootable medium for administering or repairing your system and data.",
            latestVersion = "11.00",
            expectedSha256 = "8ab580db3a1ef5d4b79140eb93809b0b4fb9b6a12cc9ecbcfb86ea61fb0b3c66",
            downloadUrl = "https://sourceforge.net/projects/systemrescuecd/files/sysresccd-x86/11.00/systemrescue-11.00-amd64.iso/download",
            websiteUrl = "https://system-rescue.org",
            defaultBootMode = "UEFI & BIOS",
            suggestedPersistenceMb = 2048,
            iconKey = "rescue"
        ),
        DistroCatalogEntity(
            id = "memtest86",
            name = "MemTest86+ V7",
            category = "Rescue & Recovery",
            description = "Thorough, stand-alone memory test for x86 and x86-64 architecture computers. Detects faulty RAM chips.",
            latestVersion = "7.00",
            expectedSha256 = "3e660e5883d6a4c281fe104889c25b3152d5b62b1b369527ecbbf7a3eb0f993d",
            downloadUrl = "https://memtest.org/download/v7.00/mt86plus_7.00_GRUB.iso.zip",
            websiteUrl = "https://memtest.org",
            defaultBootMode = "UEFI & BIOS",
            suggestedPersistenceMb = 0,
            iconKey = "hardware"
        ),
        DistroCatalogEntity(
            id = "tails_privacy",
            name = "Tails OS (Amnesic Incognito)",
            category = "Security & PenTest",
            description = "Portable operating system that protects against surveillance and censorship. Leaves zero traces on host PC.",
            latestVersion = "6.2",
            expectedSha256 = "b72960be6136dca50a97bf958e9999a3cbfdc3825ee5b30619bc9e5c1d63e9c5",
            downloadUrl = "https://download.tails.net/tails/stable/tails-amd64-6.2/tails-amd64-6.2.iso",
            websiteUrl = "https://tails.net",
            defaultBootMode = "UEFI Only",
            suggestedPersistenceMb = 8192,
            iconKey = "tails"
        ),
        DistroCatalogEntity(
            id = "proxmox_ve",
            name = "Proxmox VE 8",
            category = "Hypervisors",
            description = "Complete open-source platform for enterprise virtualization, combining KVM hypervisor and LXC containers.",
            latestVersion = "8.2",
            expectedSha256 = "7bf9e0234a9b6c072c4ce0a2327a3c3065b75a40b3cb1c876ea02e861a7a1c89",
            downloadUrl = "https://enterprise.proxmox.com/iso/proxmox-ve_8.2-1.iso",
            websiteUrl = "https://proxmox.com",
            defaultBootMode = "UEFI & BIOS",
            suggestedPersistenceMb = 0,
            iconKey = "server"
        ),
        DistroCatalogEntity(
            id = "linux_mint",
            name = "Linux Mint 21.3 Cinnamon",
            category = "Linux Distros",
            description = "Comfortable, elegant, and modern desktop experience based on Ubuntu LTS. Out of the box multimedia support.",
            latestVersion = "21.3",
            expectedSha256 = "094625b060d4b998aeebbb6e289be9d4a4d6501dbfb343e06180a067189f7f45",
            downloadUrl = "https://mirrors.kernel.org/linuxmint/stable/21.3/linuxmint-21.3-cinnamon-64bit.iso",
            websiteUrl = "https://linuxmint.com",
            defaultBootMode = "UEFI & BIOS",
            suggestedPersistenceMb = 4096,
            iconKey = "mint"
        ),
        DistroCatalogEntity(
            id = "windows_11_installer",
            name = "Windows 11 Setup Utility",
            category = "Windows Utilities",
            description = "Official Microsoft Windows 11 installation media. Compatible with Ventoy bypass TPM & SecureBoot plugins.",
            latestVersion = "23H2 (x64)",
            expectedSha256 = "361d112d75954117b355208eb4196144e5cc5669b7a42ecad54f9d0c2ee146f4",
            downloadUrl = "https://www.microsoft.com/software-download/windows11",
            websiteUrl = "https://microsoft.com",
            defaultBootMode = "UEFI Only",
            suggestedPersistenceMb = 0,
            iconKey = "windows"
        ),
        DistroCatalogEntity(
            id = "hirens_bootcd",
            name = "Hiren's BootCD PE x64",
            category = "Windows Utilities",
            description = "Emergency diagnostic Windows 11 PE live environment equipped with hundreds of IT tech diagnostic tools.",
            latestVersion = "v1.0.8 (Win11 PE)",
            expectedSha256 = "e12f6764508e33f38ffb4e4f71a06cf05943b17c1bf20875e5b306bce8d4c382",
            downloadUrl = "https://www.hirensbootcd.org/files/HBCD_PE_x64.iso",
            websiteUrl = "https://hirensbootcd.org",
            defaultBootMode = "UEFI & BIOS",
            suggestedPersistenceMb = 0,
            iconKey = "windows"
        )
    )

    fun detectDistroFromFileName(fileName: String): DistroDetectorResult {
        val lower = fileName.lowercase(Locale.ROOT)
        
        // Detect OS architecture
        val arch = when {
            lower.contains("arm64") || lower.contains("aarch64") -> "ARM64"
            lower.contains("amd64") || lower.contains("x86_64") || lower.contains("x64") -> "x86_64"
            lower.contains("i386") || lower.contains("i686") || lower.contains("x86") -> "x86 (32-bit)"
            else -> "Universal (x64/x86)"
        }

        return when {
            lower.contains("win11") || lower.contains("windows 11") || lower.contains("windows11") -> {
                DistroDetectorResult(
                    distroName = "Windows 11 Setup",
                    osFamily = "Windows",
                    version = extractVersion(lower, "23H2"),
                    architecture = "x86_64",
                    suggestedBootMode = "UEFI Only",
                    iconKey = "windows"
                )
            }
            lower.contains("win10") || lower.contains("windows 10") || lower.contains("windows10") -> {
                DistroDetectorResult(
                    distroName = "Windows 10 Setup",
                    osFamily = "Windows",
                    version = extractVersion(lower, "22H2"),
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "windows"
                )
            }
            lower.contains("hiren") || lower.contains("hbcd") -> {
                DistroDetectorResult(
                    distroName = "Hiren's BootCD PE",
                    osFamily = "Windows PE",
                    version = "PE x64",
                    architecture = "x86_64",
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "windows"
                )
            }
            lower.contains("ubuntu") -> {
                DistroDetectorResult(
                    distroName = "Ubuntu Linux",
                    osFamily = "Linux",
                    version = extractVersion(lower, "24.04 LTS"),
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "ubuntu"
                )
            }
            lower.contains("kali") -> {
                DistroDetectorResult(
                    distroName = "Kali Linux",
                    osFamily = "Security / Linux",
                    version = extractVersion(lower, "2024.2"),
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "kali"
                )
            }
            lower.contains("arch") -> {
                DistroDetectorResult(
                    distroName = "Arch Linux",
                    osFamily = "Linux",
                    version = extractVersion(lower, "Rolling"),
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "arch"
                )
            }
            lower.contains("fedora") -> {
                DistroDetectorResult(
                    distroName = "Fedora Workstation",
                    osFamily = "Linux",
                    version = extractVersion(lower, "40"),
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "fedora"
                )
            }
            lower.contains("debian") -> {
                DistroDetectorResult(
                    distroName = "Debian GNU/Linux",
                    osFamily = "Linux",
                    version = extractVersion(lower, "12.5 Bookworm"),
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "debian"
                )
            }
            lower.contains("mint") -> {
                DistroDetectorResult(
                    distroName = "Linux Mint",
                    osFamily = "Linux",
                    version = extractVersion(lower, "21.3"),
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "mint"
                )
            }
            lower.contains("clonezilla") -> {
                DistroDetectorResult(
                    distroName = "Clonezilla Live",
                    osFamily = "Rescue / Backup",
                    version = extractVersion(lower, "3.1.2"),
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "rescue"
                )
            }
            lower.contains("gparted") -> {
                DistroDetectorResult(
                    distroName = "GParted Live",
                    osFamily = "Disk Utility",
                    version = extractVersion(lower, "1.6.0"),
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "partition"
                )
            }
            lower.contains("systemrescue") || lower.contains("sysresccd") -> {
                DistroDetectorResult(
                    distroName = "SystemRescue",
                    osFamily = "Rescue / Recovery",
                    version = extractVersion(lower, "11.00"),
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "rescue"
                )
            }
            lower.contains("memtest") -> {
                DistroDetectorResult(
                    distroName = "MemTest86+ RAM Diagnostic",
                    osFamily = "Hardware Tool",
                    version = extractVersion(lower, "v7.00"),
                    architecture = "Universal",
                    suggestedBootMode = "UEFI & BIOS (Memdisk)",
                    iconKey = "hardware"
                )
            }
            lower.contains("tails") -> {
                DistroDetectorResult(
                    distroName = "Tails OS (Amnesic)",
                    osFamily = "Security / Privacy",
                    version = extractVersion(lower, "6.2"),
                    architecture = "x86_64",
                    suggestedBootMode = "UEFI Only",
                    iconKey = "tails"
                )
            }
            lower.contains("proxmox") -> {
                DistroDetectorResult(
                    distroName = "Proxmox VE Server",
                    osFamily = "Hypervisor",
                    version = extractVersion(lower, "8.2"),
                    architecture = "x86_64",
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "server"
                )
            }
            lower.contains("freebsd") -> {
                DistroDetectorResult(
                    distroName = "FreeBSD",
                    osFamily = "BSD",
                    version = extractVersion(lower, "14.0"),
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "bsd"
                )
            }
            lower.contains("android") -> {
                DistroDetectorResult(
                    distroName = "Android-x86 OS",
                    osFamily = "Android OS",
                    version = extractVersion(lower, "9.0 Pie"),
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "android"
                )
            }
            lower.endsWith(".vhd") || lower.endsWith(".vhdx") -> {
                DistroDetectorResult(
                    distroName = cleanFileName(fileName),
                    osFamily = "Virtual Disk (VHD)",
                    version = "VHD Boot",
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "vhd"
                )
            }
            lower.endsWith(".wim") -> {
                DistroDetectorResult(
                    distroName = cleanFileName(fileName),
                    osFamily = "Windows Imaging (WIM)",
                    version = "Wimboot",
                    architecture = arch,
                    suggestedBootMode = "Wimboot UEFI/BIOS",
                    iconKey = "windows"
                )
            }
            else -> {
                DistroDetectorResult(
                    distroName = cleanFileName(fileName),
                    osFamily = "Bootable Payload",
                    version = "Custom",
                    architecture = arch,
                    suggestedBootMode = "UEFI & BIOS",
                    iconKey = "generic"
                )
            }
        }
    }

    private fun cleanFileName(fileName: String): String {
        return fileName.replace(".iso", "", ignoreCase = true)
            .replace(".img", "", ignoreCase = true)
            .replace(".vhd", "", ignoreCase = true)
            .replace(".vhdx", "", ignoreCase = true)
            .replace(".wim", "", ignoreCase = true)
            .replace("_", " ")
            .replace("-", " ")
            .trim()
    }

    private fun extractVersion(name: String, fallback: String): String {
        val regex = Regex("""\b(\d+(\.\d+)+([a-zA-Z0-9_-]+)?)\b""")
        val match = regex.find(name)
        return match?.value ?: fallback
    }
}
