package com.jerson.hcdc_portal.util;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadRoomsWorker extends Worker {

    public DownloadRoomsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String url = getInputData().getString("url");
        String fileName = getInputData().getString("fileName");

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = Objects.requireNonNull(response.body());

            File outputFile = new File(getApplicationContext().getFilesDir(), fileName);

            // Delete the file if it exists
            if (outputFile.exists()) {
                outputFile.delete();
            }

            FileOutputStream outputStream = new FileOutputStream(outputFile);

            InputStream inputStream = body.byteStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            String filePath = outputFile.getAbsolutePath();
            displayDownloadedFilePath(filePath);
            return Result.success();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private void displayDownloadedFilePath(String filePath) {
        System.out.println("File Path: " + filePath);
    }
}
