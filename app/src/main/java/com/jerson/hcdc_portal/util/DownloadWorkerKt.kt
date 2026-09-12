package com.jerson.hcdc_portal.util

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@HiltWorker
class DownloadWorkerKt @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val TAG = "DownloadWorker"
    override suspend fun doWork(): Result {
        val url = inputData.getString("url")
        val fileName = inputData.getString("fileName")

        if (url.isNullOrBlank() || fileName.isNullOrBlank()) {
            return Result.failure()
        }

        return try {
            withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                response.use { resp ->
                    if (!resp.isSuccessful) {
                        return@withContext Result.retry()
                    }
                    val body = resp.body ?: return@withContext Result.failure()
                    val outputFile = File(applicationContext.cacheDir, fileName)
                    if (outputFile.exists()) {
                        outputFile.delete()
                    }

                    val buffer = ByteArray(8192)
                    body.byteStream().use { input ->
                        FileOutputStream(outputFile).use { output ->
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}