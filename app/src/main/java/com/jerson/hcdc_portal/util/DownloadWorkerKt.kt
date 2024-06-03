package com.jerson.hcdc_portal.util

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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

        return try {
            val client = OkHttpClient()
            val request = url?.let {
                Request.Builder()
                    .url(it)
                    .build()
            }
            val response: Response? = request?.let { client.newCall(it).execute() }
            val body: ResponseBody? = response?.body
            val outputFile = fileName?.let { File(applicationContext.cacheDir, it) }

            if (outputFile != null) {
                if (outputFile.exists()) {
                    outputFile.delete()
                }
            }

            val outputStream = FileOutputStream(outputFile)
            val inputStream = body?.byteStream()
            val buffer = ByteArray(1024)
            var bytesRead: Int?
            while (inputStream?.read(buffer).also { bytesRead = it } != -1) {
                bytesRead?.let { outputStream.write(buffer, 0, it) }
            }
            outputStream.close()
            inputStream?.close()
            val filePath = outputFile?.absolutePath
            Log.e(TAG, "doWork: $filePath")
            Result.success()
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure()
        }
    }
}