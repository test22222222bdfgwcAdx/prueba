package com.example.canales;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Updater {

    public interface UpdateCallback {
        void noUpdate();
    }

    public static void checkUpdate(Context context, UpdateCallback callback) {

        String UPDATE_URL = "https://raw.githubusercontent.com/futbolparaguayotv/updateapk/refs/heads/main/update.json";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(UPDATE_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity) context).runOnUiThread(callback::noUpdate);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();

                try {
                    JSONObject obj = new JSONObject(data);

                    int latestVersion = obj.getInt("versionCode");
                    String apkUrl = obj.getString("apkUrl");
                    String mensaje = obj.optString("mensaje", "Nueva versión disponible");

                    int currentVersion = context.getPackageManager()
                            .getPackageInfo(context.getPackageName(), 0)
                            .versionCode;

                    if (latestVersion > currentVersion) {
                        ((Activity) context).runOnUiThread(() -> {
                            new AlertDialog.Builder(context)
                                    .setTitle("Actualización disponible")
                                    .setMessage(mensaje)
                                    .setCancelable(false)
                                    .setPositiveButton("Descargar", (dialog, which) -> {
                                        descargarApk(context, apkUrl);
                                    })
                                    .setNegativeButton("Después", (dialog, which) -> {
                                        callback.noUpdate();
                                    })
                                    .show();
                        });
                    } else {
                        ((Activity) context).runOnUiThread(callback::noUpdate);
                    }

                } catch (Exception e) {
                    ((Activity) context).runOnUiThread(callback::noUpdate);
                }
            }
        });
    }

    private static void descargarApk(Context context, String apkUrl) {
        try {
            String fileName = "GoatFutbolUpdate.apk";

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
            request.setTitle("Descargando actualización");
            request.setDescription("GOAT Fútbol TV");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(
                    context,
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
            );

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            long downloadId = manager.enqueue(request);

            Toast.makeText(context, "Descargando actualización...", Toast.LENGTH_LONG).show();

            new Thread(() -> {
                boolean descargando = true;

                while (descargando) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);

                    Cursor cursor = manager.query(query);

                    if (cursor != null && cursor.moveToFirst()) {
                        int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int status = cursor.getInt(statusIndex);

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            descargando = false;
                            cursor.close();

                            ((Activity) context).runOnUiThread(() -> instalarApk(context, fileName));

                        } else if (status == DownloadManager.STATUS_FAILED) {
                            descargando = false;
                            cursor.close();

                            ((Activity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "Falló la descarga", Toast.LENGTH_LONG).show()
                            );
                        } else {
                            cursor.close();
                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        descargando = false;
                    }
                }
            }).start();

        } catch (Exception e) {
            Toast.makeText(context, "Error descarga: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void instalarApk(Context context, String fileName) {
        try {
            File apkFile = new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    fileName
            );

            Uri apkUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    apkFile
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(context, "Error instalar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}