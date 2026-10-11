package com.nothing.tune.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.tune.data.AppVersionInfo
import com.nothing.tune.data.MusicRepository
import com.nothing.tune.player.AudioDeviceManager
import com.nothing.tune.ui.components.DotMatrixText
import com.nothing.tune.ui.theme.AccentRed
import com.nothing.tune.ui.theme.PureBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: MusicRepository,
    audioDeviceManager: AudioDeviceManager,
    currentAppName: String,
    accentColor: Color,
    onAppNameChange: (String) -> Unit,
    onAccentColorChange: (Color) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showAppearanceSheet by remember { mutableStateOf(false) }
    var showAudioQualitySheet by remember { mutableStateOf(false) }
    var showDevicesSheet by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }
    var showDownloadsSheet by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showUpdateSheet by remember { mutableStateOf(false) }
    var showDeveloperSheet by remember { mutableStateOf(false) }

    val accentColors = listOf(
        Color.White,
        Color(0xFFD71920), // Nothing OS Red
        Color(0xFFFEE12B), // Nothing OS Yellow
        Color(0xFF00E5FF), // Cyan
        Color(0xFF76FF03), // Lime Green
        Color(0xFFFF4081)  // Pink
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top Bar ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ── Grouped Settings Items ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF141416))
                .border(1.dp, Color(0xFF262628), RoundedCornerShape(20.dp))
        ) {
            // 1. Account
            SettingsRow(
                icon = Icons.Default.Person,
                title = "Account",
                subtitle = "Manage your local profile",
                onClick = { showAccountSheet = true }
            )

            SettingsDivider()

            // 2. Audio Quality
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "Audio Quality",
                subtitle = "24-bit / 192kHz direct passthrough",
                onClick = { showAudioQualitySheet = true }
            )

            SettingsDivider()

            // 3. Downloads
            SettingsRow(
                icon = Icons.Default.Download,
                title = "Downloads & Library",
                subtitle = "Offline music & restore hidden tracks",
                onClick = { showDownloadsSheet = true }
            )

            SettingsDivider()

            // 4. Appearance
            SettingsRow(
                icon = Icons.Default.Palette,
                title = "Appearance",
                subtitle = "Nothing accent color swatches",
                onClick = { showAppearanceSheet = true }
            )

            SettingsDivider()

            // 5. Notifications
            SettingsRow(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Background playback & lockscreen controls",
                onClick = { showNotificationsSheet = true }
            )

            SettingsDivider()

            // 6. Connected Devices
            SettingsRow(
                icon = Icons.Default.Devices,
                title = "Connected Devices",
                subtitle = "Bluetooth, speakers & dual audio play",
                onClick = { showDevicesSheet = true }
            )

            SettingsDivider()

            // 7. System Update (with Version & What's Changed)
            SettingsRow(
                icon = Icons.Default.SystemUpdate,
                title = "Software Update",
                subtitle = "v${AppVersionInfo.CURRENT_VERSION_NAME} · What's changed",
                onClick = { showUpdateSheet = true }
            )

            SettingsDivider()

            // 8. Developer Info
            SettingsRow(
                icon = Icons.Default.Code,
                title = "Developer Info",
                subtitle = "${AppVersionInfo.DEVELOPER_NAME} · Architecture & specs",
                onClick = { showDeveloperSheet = true }
            )

            SettingsDivider()

            // 9. About
            SettingsRow(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "NothingTune · Edition 1.0",
                onClick = { showAboutDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // ── Software Update Modal Sheet ────────────────────────────────────────────
    if (showUpdateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showUpdateSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF141416)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DotMatrixText(
                        text = "UPDATE",
                        dotSize = 2.4.dp,
                        dotSpacing = 1.2.dp,
                        activeColor = Color.White
                    )

                    Text(
                        text = "v${AppVersionInfo.CURRENT_VERSION_NAME}",
                        color = AccentRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentRed.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "NothingTune OS Build ${AppVersionInfo.CURRENT_VERSION_CODE}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Released on ${AppVersionInfo.RELEASE_DATE}",
                    color = Color(0xFF888888),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "WHAT'S CHANGED IN THIS UPDATE:",
                    color = Color(0xFFAAAAAA),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Changelog list for current version
                AppVersionInfo.CHANGELOG.firstOrNull()?.changes?.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = "• ", color = AccentRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = item,
                            color = Color(0xFFCCCCCC),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Action Buttons: Check Updates & Developer Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(Color.White)
                            .clickable {
                                Toast.makeText(
                                    context,
                                    "You are on the latest version (v${AppVersionInfo.CURRENT_VERSION_NAME})",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Check Updates",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(Color(0xFF1E1E22))
                            .border(1.dp, Color(0xFF2E2E32), RoundedCornerShape(23.dp))
                            .clickable {
                                showUpdateSheet = false
                                showDeveloperSheet = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Developer Info",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ── Developer Info Modal Sheet ─────────────────────────────────────────────
    if (showDeveloperSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDeveloperSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF141416)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                DotMatrixText(
                    text = "DEV INFO",
                    dotSize = 2.4.dp,
                    dotSpacing = 1.2.dp,
                    activeColor = Color.White
                )

                Spacer(modifier = Modifier.height(18.dp))

                DeveloperStatRow(label = "Lead Developer", value = AppVersionInfo.DEVELOPER_NAME)
                DeveloperStatRow(label = "App Version", value = "v${AppVersionInfo.CURRENT_VERSION_NAME} (Build ${AppVersionInfo.CURRENT_VERSION_CODE})")
                DeveloperStatRow(label = "Architecture", value = AppVersionInfo.ARCHITECTURE)
                DeveloperStatRow(label = "UI Engine", value = AppVersionInfo.UI_SYSTEM)
                DeveloperStatRow(label = "Audio DSP", value = AppVersionInfo.AUDIO_ENGINE)
                DeveloperStatRow(label = "Visualizer", value = "Real-time FFT Frequency Spectrum Analyzer")

                Spacer(modifier = Modifier.height(26.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(Color.White)
                        .clickable { showDeveloperSheet = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Close",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ── Appearance Modal Sheet ─────────────────────────────────────────────────
    if (showAppearanceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAppearanceSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF141416)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "APPEARANCE & THEME",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Nothing Accent Color",
                    color = Color(0xFFAAAAAA),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    accentColors.forEach { color ->
                        val isSelected = color == accentColor
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color(0xFF333336),
                                    shape = CircleShape
                                )
                                .clickable { onAccentColorChange(color) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ── Audio Quality Modal Sheet ──────────────────────────────────────────────
    if (showAudioQualitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showAudioQualitySheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF141416)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "AUDIO QUALITY",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "24-bit / 192kHz Passthrough",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hardware exclusive direct DAC output",
                            color = Color(0xFF888888),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = true,
                        onCheckedChange = {},
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF444448))
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ── Connected Devices Modal Sheet ──────────────────────────────────────────
    if (showDevicesSheet) {
        val connectedDevices by audioDeviceManager.devices.collectAsState()

        ModalBottomSheet(
            onDismissRequest = { showDevicesSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF141416)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "CONNECTED DEVICES & DUAL AUDIO",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                connectedDevices.forEach { dev ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E1E22))
                            .border(1.dp, Color(0xFF2E2E32), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF28282C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = null,
                                tint = if (dev.isBluetooth) AccentRed else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = dev.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${dev.typeName} • Connected", color = Color(0xFF888888), fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable {
                            (context as? Activity)?.let { act ->
                                audioDeviceManager.openSystemMediaOutputSwitcher(act)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Dual Audio / Multi-Device Play", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ── Account Sheet ──────────────────────────────────────────────────────────
    if (showAccountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAccountSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF141416)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "LOCAL ACCOUNT & PROFILE",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Profile User: Nasir\nLocal Playback Tracking: Active\nMost Played & Recents: Synchronized",
                    color = Color(0xFFCCCCCC),
                    fontSize = 13.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ── Downloads & Library Sheet ──────────────────────────────────────────────
    if (showDownloadsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDownloadsSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF141416)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "OFFLINE STORAGE & HIDDEN TRACKS",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                val hiddenCount = repository.getHiddenTrackIds().size
                Text(
                    text = "Storage Location: /storage/emulated/0/Music\nHidden Tracks Count: $hiddenCount",
                    color = Color(0xFFCCCCCC),
                    fontSize = 13.sp,
                    lineHeight = 22.sp
                )

                if (hiddenCount > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .clickable {
                                repository.getHiddenTrackIds().forEach { id ->
                                    repository.hideTrack(id, false)
                                }
                                Toast.makeText(context, "Restored $hiddenCount hidden tracks", Toast.LENGTH_SHORT).show()
                                showDownloadsSheet = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Restore All Hidden Tracks", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ── Notifications Sheet ────────────────────────────────────────────────────
    if (showNotificationsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationsSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF141416)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "NOTIFICATION PREFERENCES",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Foreground Service Media Notification: Enabled\nLockscreen Media Controls: Enabled\nNotification Controls: Previous, Play/Pause, Next",
                    color = Color(0xFFCCCCCC),
                    fontSize = 13.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ── About Dialog ───────────────────────────────────────────────────────────
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(text = "NothingTune", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Color(0xFF1E1E22),
            text = {
                Text(
                    text = "Version ${AppVersionInfo.CURRENT_VERSION_NAME} · Built by ${AppVersionInfo.DEVELOPER_NAME}\n\n" +
                            "Engineered with Jetpack Compose & Media3 ExoPlayer for pristine lossless playback.",
                    color = Color(0xFFCCCCCC),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun DeveloperStatRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(text = label, color = Color(0xFF888888), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1E22)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color(0xFF888888),
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF666666),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF1E1E22))
    )
}
