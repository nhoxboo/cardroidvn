package com.cardroid.app.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.core.content.FileProvider
import com.cardroid.app.CarDroidLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object AppUpdater {
    const val CURRENT_VERSION = "v0.2.1-vn"
    private const val GITHUB_LATEST_RELEASE_API =
        "https://api.github.com/repos/nhoxboo/cardroidvn/releases/latest"

    data class UpdateInfo(
        val latestTag: String,
        val releaseNotes: String,
        val downloadUrl: String,
        val hasNewVersion: Boolean,
    )

    suspend fun checkLatestRelease(): UpdateInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(GITHUB_LATEST_RELEASE_API)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "cardroidvn-updater")
                connectTimeout = 8000
                readTimeout = 8000
            }

            if (conn.responseCode != 200) {
                CarDroidLog.log("Updater: GitHub API returned ${conn.responseCode}")
                return@withContext null
            }

            val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val json = JSONObject(response)
            val tagName = json.optString("tag_name", "")
            val body = json.optString("body", "Có bản cập nhật mới.")
            val assets = json.optJSONArray("assets")

            var downloadUrl = ""
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
            }

            val hasNewVersion = isNewerVersion(tagName, CURRENT_VERSION)
            CarDroidLog.log("Updater: Current=$CURRENT_VERSION Latest=$tagName HasNew=$hasNewVersion")

            UpdateInfo(
                latestTag = tagName,
                releaseNotes = body,
                downloadUrl = downloadUrl,
                hasNewVersion = hasNewVersion
            )
        }.getOrNull()
    }

    private fun isNewerVersion(remoteTag: String, currentTag: String): Boolean {
        if (remoteTag.isBlank()) return false
        val cleanRemote = remoteTag.removePrefix("v").replace("-vn", "").trim()
        val cleanCurrent = currentTag.removePrefix("v").replace("-vn", "").trim()
        return cleanRemote > cleanCurrent
    }

    fun startDownloadAndInstall(context: Context, downloadUrl: String) {
        if (downloadUrl.isBlank()) return
        val appContext = context.applicationContext
        val fileName = "cardroidvn-update.apk"

        val destinationFile = File(
            appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        if (destinationFile.exists()) destinationFile.delete()

        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("cardroidvn Đang cập nhật")
            setDescription("Tải về bản phát hành mới từ GitHub...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationUri(Uri.fromFile(destinationFile))
            setMimeType("application/vnd.android.package-archive")
        }

        val manager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    appContext.unregisterReceiver(this)
                    installApk(appContext, destinationFile)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        } else {
            appContext.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    private fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(installIntent)
    }
}

@Composable
fun UpdateAvailableDialog(
    updateInfo: AppUpdater.UpdateInfo,
    onConfirmUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🚀 Có phiên bản mới: ${updateInfo.latestTag}") },
        text = {
            Text(
                "Đã phát hiện bản cập nhật mới trên GitHub của Hoài Nam.\n\n" +
                    "Nội dung cập nhật:\n" + updateInfo.releaseNotes.take(300)
            )
        },
        confirmButton = {
            Button(onClick = onConfirmUpdate) {
                Text("Cập nhật ngay")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Để sau")
            }
        }
    )
}
