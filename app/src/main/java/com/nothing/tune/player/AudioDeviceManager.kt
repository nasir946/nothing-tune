package com.nothing.tune.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ConnectedAudioDevice(
    val id: Int,
    val name: String,
    val type: Int,
    val typeName: String,
    val isBluetooth: Boolean,
    val isSelected: Boolean = false,
    val isDefault: Boolean = false
)

class AudioDeviceManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _devices = MutableStateFlow<List<ConnectedAudioDevice>>(emptyList())
    val devices: StateFlow<List<ConnectedAudioDevice>> = _devices.asStateFlow()

    private val _isDualAudioActive = MutableStateFlow(false)
    val isDualAudioActive: StateFlow<Boolean> = _isDualAudioActive.asStateFlow()

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            refreshDevices()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            refreshDevices()
        }
    }

    init {
        audioManager.registerAudioDeviceCallback(deviceCallback, null)
        refreshDevices()
    }

    fun refreshDevices() {
        val list = mutableListOf<ConnectedAudioDevice>()
        try {
            val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (dev in outputs) {
                val isBt = isBluetoothDevice(dev.type)
                val typeName = getDeviceTypeName(dev.type)
                val name = try {
                    if (dev.productName.isNotBlank()) dev.productName.toString() else typeName
                } catch (_: Throwable) {
                    typeName
                }

                list.add(
                    ConnectedAudioDevice(
                        id = dev.id,
                        name = name,
                        type = dev.type,
                        typeName = typeName,
                        isBluetooth = isBt,
                        isSelected = isBt || dev.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                        isDefault = dev.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                    )
                )
            }
        } catch (_: Throwable) {}

        // If no devices detected (or in preview), provide fallback entries
        if (list.isEmpty()) {
            list.add(
                ConnectedAudioDevice(
                    id = 1,
                    name = "Phone Speaker",
                    type = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                    typeName = "Speaker",
                    isBluetooth = false,
                    isDefault = true
                )
            )
        }

        _devices.value = list
    }

    fun openSystemMediaOutputSwitcher(activity: Activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ Native Media Output Switcher panel (supports multi-device routing & Dual Audio)
                val intent = Intent("com.android.settings.panel.action.MEDIA_OUTPUT").apply {
                    putExtra("com.android.settings.panel.extra.PACKAGE_NAME", activity.packageName)
                }
                activity.startActivity(intent)
                return
            }
        } catch (_: Throwable) {
            // Panel not supported on this OEM
        }

        try {
            // Fallback: Bluetooth settings
            val btIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            activity.startActivity(btIntent)
        } catch (_: Throwable) {}
    }

    fun toggleDualAudio(enable: Boolean, activity: Activity? = null) {
        _isDualAudioActive.value = enable
        if (enable && activity != null) {
            openSystemMediaOutputSwitcher(activity)
        }
    }

    fun release() {
        try {
            audioManager.unregisterAudioDeviceCallback(deviceCallback)
        } catch (_: Throwable) {}
    }

    companion object {
        fun isBluetoothDevice(type: Int): Boolean {
            return when (type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_BLE_SPEAKER,
                AudioDeviceInfo.TYPE_BLE_BROADCAST -> true
                else -> false
            }
        }

        fun getDeviceTypeName(type: Int): String {
            return when (type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
                AudioDeviceInfo.TYPE_BLE_HEADSET -> "BLE Headset"
                AudioDeviceInfo.TYPE_BLE_SPEAKER -> "BLE Speaker"
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth Headset"
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired Headset"
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired Headphones"
                AudioDeviceInfo.TYPE_USB_DEVICE,
                AudioDeviceInfo.TYPE_USB_HEADSET -> "USB DAC / Audio"
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Phone Speaker"
                else -> "Audio Output"
            }
        }
    }
}
