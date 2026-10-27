package com.cardroid.app

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Runtime-toggleable knobs surfaced in the UI so we can A/B test wire
 * variants on the real BMW without rebuilding the APK.
 *
 * Read from anywhere; flipped from MainActivity. Values are read at each
 * AirPlay handshake attempt, so changes take effect on the next attempt.
 */
object CarDroidSettings {

    private const val PREFS = "cardroid.settings"
    private const val KEY_AUTO_START_HELPER = "autoStartHelperViaShizuku"
    private const val KEY_AUTO_START_BLUETOOTH = "autoStartBluetooth"
    private const val KEY_DISPLAY_MODE = "displayMode"
    private const val KEY_RESOLUTION_PRESET = "resolutionPreset"
    private const val KEY_VIDEO_FPS = "videoFps"
    private const val KEY_VIDEO_BITRATE_MBPS = "videoBitrateMbps"
    private const val KEY_EXECUTION_MODE = "executionMode"

    enum class DisplayMode(val id: String) {
        DUAL_PANEL("dual_panel"),
        ANDROID_AUTO_COOLWALK("android_auto_coolwalk"),
        SCREEN_MIRROR("screen_mirror")
    }

    enum class ResolutionPreset(val id: String, val width: Int, val height: Int) {
        AUTO("auto", 0, 0),
        RES_800X480("800x480", 800, 480),
        RES_1280X720("1280x720", 1280, 720),
        RES_1920X720_ULTRAWIDE("1920x720", 1920, 720),
        RES_1920X1080("1920x1080", 1920, 1080)
    }

    enum class ExecutionMode(val id: String) {
        AUTO("auto"),
        SHIZUKU("shizuku"),
        ADB_HELPER("adb_helper"),
        MEDIA_PROJECTION_NOROOT("media_projection_noroot")
    }

    private var preferences: android.content.SharedPreferences? = null

    private val _autoStartHelperViaShizuku = MutableStateFlow(true)
    /** Start the shell helper once when CarDroidService comes up and Shizuku is Ready. */
    val autoStartHelperViaShizuku: StateFlow<Boolean> = _autoStartHelperViaShizuku

    private val _autoStartBluetooth = MutableStateFlow(true)
    /** Start Bluetooth listening when the app opens, after boot and after an app update. */
    val autoStartBluetooth: StateFlow<Boolean> = _autoStartBluetooth

    private val _displayMode = MutableStateFlow(DisplayMode.DUAL_PANEL)
    val displayMode: StateFlow<DisplayMode> = _displayMode

    private val _resolutionPreset = MutableStateFlow(ResolutionPreset.AUTO)
    val resolutionPreset: StateFlow<ResolutionPreset> = _resolutionPreset

    private val _videoFps = MutableStateFlow(60)
    val videoFps: StateFlow<Int> = _videoFps

    private val _videoBitrateMbps = MutableStateFlow(10)
    val videoBitrateMbps: StateFlow<Int> = _videoBitrateMbps

    private val _executionMode = MutableStateFlow(ExecutionMode.AUTO)
    val executionMode: StateFlow<ExecutionMode> = _executionMode

    /** Loads the persisted switch once; safe to call from Activity and Service. */
    fun initialize(context: Context) {
        if (preferences != null) return
        synchronized(this) {
            if (preferences == null) {
                val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                preferences = prefs
                _autoStartHelperViaShizuku.value = prefs.getBoolean(KEY_AUTO_START_HELPER, true)
                _autoStartBluetooth.value = prefs.getBoolean(KEY_AUTO_START_BLUETOOTH, true)
                val dmId = prefs.getString(KEY_DISPLAY_MODE, DisplayMode.DUAL_PANEL.id)
                _displayMode.value = DisplayMode.entries.find { it.id == dmId } ?: DisplayMode.DUAL_PANEL
                val resId = prefs.getString(KEY_RESOLUTION_PRESET, ResolutionPreset.AUTO.id)
                _resolutionPreset.value = ResolutionPreset.entries.find { it.id == resId } ?: ResolutionPreset.AUTO
                _videoFps.value = prefs.getInt(KEY_VIDEO_FPS, 60)
                _videoBitrateMbps.value = prefs.getInt(KEY_VIDEO_BITRATE_MBPS, 10)
                val execId = prefs.getString(KEY_EXECUTION_MODE, ExecutionMode.AUTO.id)
                _executionMode.value = ExecutionMode.entries.find { it.id == execId } ?: ExecutionMode.AUTO
            }
        }
    }

    fun setAutoStartBluetooth(enabled: Boolean) {
        _autoStartBluetooth.value = enabled
        preferences?.edit()?.putBoolean(KEY_AUTO_START_BLUETOOTH, enabled)?.apply()
    }

    fun setAutoStartHelperViaShizuku(enabled: Boolean) {
        _autoStartHelperViaShizuku.value = enabled
        preferences?.edit()?.putBoolean(KEY_AUTO_START_HELPER, enabled)?.apply()
    }

    fun setDisplayMode(mode: DisplayMode) {
        _displayMode.value = mode
        preferences?.edit()?.putString(KEY_DISPLAY_MODE, mode.id)?.apply()
    }

    fun setResolutionPreset(preset: ResolutionPreset) {
        _resolutionPreset.value = preset
        preferences?.edit()?.putString(KEY_RESOLUTION_PRESET, preset.id)?.apply()
    }

    fun setVideoFps(fps: Int) {
        _videoFps.value = fps
        preferences?.edit()?.putInt(KEY_VIDEO_FPS, fps)?.apply()
    }

    fun setVideoBitrateMbps(mbps: Int) {
        _videoBitrateMbps.value = mbps
        preferences?.edit()?.putInt(KEY_VIDEO_BITRATE_MBPS, mbps)?.apply()
    }

    fun setExecutionMode(mode: ExecutionMode) {
        _executionMode.value = mode
        preferences?.edit()?.putString(KEY_EXECUTION_MODE, mode.id)?.apply()
    }

    /**
     * If true, [OkcarMfiSapImpl] ignores the X25519/SHA1 derivation and
     * sends the hardcoded `eiv`/`ekey` constants used by OkcarOS's
     * `PcConnectService.java:488-490` -- a known-good pair against several
     * real CarPlay head units. /auth-setup still runs (the car requires the
     * roundtrip even though the result is discarded).
     */
    private val _useHardcodedMfiKeys = MutableStateFlow(false)
    val useHardcodedMfiKeys: StateFlow<Boolean> = _useHardcodedMfiKeys
    fun setUseHardcodedMfiKeys(v: Boolean) { _useHardcodedMfiKeys.value = v }

    /**
     * Constants lifted verbatim from
     * `reference-source/sources/com/okcar/autoconn/conn/PcConnectService.java:488-490`.
     * IV is a 4-byte pattern `68 EA 43 CB` repeated four times.
     */
    val HARDCODED_EIV: ByteArray = byteArrayOf(
        0x68.toByte(), 0xEA.toByte(), 0x43.toByte(), 0xCB.toByte(),
        0x68.toByte(), 0xEA.toByte(), 0x43.toByte(), 0xCB.toByte(),
        0x68.toByte(), 0xEA.toByte(), 0x43.toByte(), 0xCB.toByte(),
        0x68.toByte(), 0xEA.toByte(), 0x43.toByte(), 0xCB.toByte(),
    )
    val HARDCODED_EKEY: ByteArray = byteArrayOf(
        0x67.toByte(), 0x64.toByte(), 0x00.toByte(), 0x20.toByte(),
        0xAC.toByte(), 0x1B.toByte(), 0x1A.toByte(), 0x80.toByte(),
        0x50.toByte(), 0x05.toByte(), 0x8F.toByte(), 0xBA.toByte(),
        0x6A.toByte(), 0x0A.toByte(), 0x0F.toByte(), 0xC8.toByte(),
    )
}
