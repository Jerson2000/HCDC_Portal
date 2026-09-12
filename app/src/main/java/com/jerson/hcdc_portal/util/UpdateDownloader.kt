package com.jerson.hcdc_portal.util

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.jerson.hcdc_portal.BuildConfig
import com.jerson.hcdc_portal.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

object UpdateDownloader {

    const val CHANNEL_ID = "app_updates_channel"
    private const val CHANNEL_NAME = "App Updates"
    private const val NOTIFICATION_ID = 202409

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress and completion status when downloading app updates."
                enableVibration(false)
                setShowBadge(false)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun startDownload(context: Context, url: String) {
        createNotificationChannel(context)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (downloadManager == null) {
            Toast.makeText(context, "Download service is unavailable.", Toast.LENGTH_SHORT).show()
            return
        }

        val urlUri = url.toUri()
        val fileName = urlUri.lastPathSegment ?: "HCDC_Portal_update.apk"

        // Prepare destination in public Downloads directory
        val request = DownloadManager.Request(urlUri).apply {
            setTitle(context.getString(R.string.app_name))
            setDescription("Downloading latest update...")
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            // Suppress DownloadManager default system notification so our single custom progress notification is shown
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            setMimeType("application/vnd.android.package-archive")
        }

        val downloadId = try {
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(context, "Downloading update...", Toast.LENGTH_SHORT).show()

        // Start real-time notification progress tracking
        trackProgress(context.applicationContext, downloadId, fileName)
    }

    private fun trackProgress(context: Context, downloadId: Long, fileName: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        scope.launch {
            var isDownloading = true
            var lastPercent = -1

            val baseBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_update)
                .setContentTitle(context.getString(R.string.app_name))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)

            // Initial indeterminate progress notification
            notificationManager.notify(
                NOTIFICATION_ID,
                baseBuilder
                    .setContentText("Starting download...")
                    .setProgress(100, 0, true)
                    .build()
            )

            while (isDownloading && isActive) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)

                if (cursor != null && cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val bytesDownloadedIndex =
                        cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalBytesIndex =
                        cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                    val status = if (statusIndex >= 0) cursor.getInt(statusIndex) else -1
                    val bytesDownloaded =
                        if (bytesDownloadedIndex >= 0) cursor.getLong(bytesDownloadedIndex) else 0L
                    val totalBytes =
                        if (totalBytesIndex >= 0) cursor.getLong(totalBytesIndex) else 0L

                    when (status) {
                        DownloadManager.STATUS_RUNNING -> {
                            if (totalBytes > 0) {
                                val percent = ((bytesDownloaded * 100) / totalBytes).toInt()
                                if (percent != lastPercent) {
                                    lastPercent = percent
                                    val downloadedMb = bytesDownloaded / (1024f * 1024f)
                                    val totalMb = totalBytes / (1024f * 1024f)
                                    val text = String.format(
                                        "Downloading: %d%% (%.1f MB / %.1f MB)",
                                        percent,
                                        downloadedMb,
                                        totalMb
                                    )
                                    notificationManager.notify(
                                        NOTIFICATION_ID,
                                        baseBuilder
                                            .setContentText(text)
                                            .setProgress(100, percent, false)
                                            .build()
                                    )
                                }
                            } else {
                                notificationManager.notify(
                                    NOTIFICATION_ID,
                                    baseBuilder
                                        .setContentText("Downloading update...")
                                        .setProgress(100, 0, true)
                                        .build()
                                )
                            }
                        }

                        DownloadManager.STATUS_SUCCESSFUL -> {
                            isDownloading = false
                            val installIntent = createInstallIntent(context, downloadManager, downloadId, fileName)
                            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            } else {
                                PendingIntent.FLAG_UPDATE_CURRENT
                            }
                            val pendingIntent = PendingIntent.getActivity(
                                context,
                                0,
                                installIntent,
                                flags
                            )

                            val completeNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_cloud_done)
                                .setContentTitle(context.getString(R.string.app_name))
                                .setContentText("Download complete. Tap to install update.")
                                .setProgress(0, 0, false)
                                .setOngoing(false)
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .build()

                            notificationManager.notify(NOTIFICATION_ID, completeNotification)
                        }

                        DownloadManager.STATUS_FAILED -> {
                            isDownloading = false
                            val failedNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_close)
                                .setContentTitle(context.getString(R.string.app_name))
                                .setContentText("Download failed. Tap to retry in the app.")
                                .setProgress(0, 0, false)
                                .setOngoing(false)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .build()

                            notificationManager.notify(NOTIFICATION_ID, failedNotification)
                        }

                        DownloadManager.STATUS_PAUSED -> {
                            notificationManager.notify(
                                NOTIFICATION_ID,
                                baseBuilder
                                    .setContentText("Download paused waiting for connection...")
                                    .build()
                            )
                        }
                    }
                    cursor.close()
                } else {
                    cursor?.close()
                }

                if (isDownloading) {
                    delay(500)
                }
            }
        }
    }

    private fun createInstallIntent(
        context: Context,
        downloadManager: DownloadManager,
        downloadId: Long,
        fileName: String
    ): Intent {
        val apkUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            if (file.exists()) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            } else {
                downloadManager.getUriForDownloadedFile(downloadId)
            }
        } else {
            downloadManager.getUriForDownloadedFile(downloadId)
        }

        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}