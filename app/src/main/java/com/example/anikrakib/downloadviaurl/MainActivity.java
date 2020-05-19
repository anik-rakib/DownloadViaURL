package com.example.anikrakib.downloadviaurl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.downloader.PRDownloader;
import com.downloader.Status;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity{
    private static final int PERMISSION_STORAGE_CODE = 1000;
    EditText urlEditText;
    Button download,cancel,pause;
    int download_Id;
    private long downloadId;
    private ThinDownloadManager downloadManager;
    private static String dirPath;
    Context context;


    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadId == id) {
                Toast.makeText(getApplicationContext(), "下载完成\n(Download Completed)", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        dirPath = context.getApplicationInfo().dataDir;

        urlEditText = findViewById(R.id.urlEditText);
        download = findViewById(R.id.downloadButton);
        cancel = findViewById(R.id.cancelButton);
        pause = findViewById(R.id.pauseButton);

        downloadManager = new ThinDownloadManager();

        registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED){
                        String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission,PERMISSION_STORAGE_CODE);

                    }else{
                        Toast.makeText(getApplicationContext(),"正在下载\nDownloading...!",Toast.LENGTH_SHORT).show();
                        startDownload();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"正在下载\nDownloading...!",Toast.LENGTH_SHORT).show();
                    startDownload();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDownload();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Status.RUNNING == PRDownloader.getStatus((int) downloadId)) {
                    PRDownloader.pause((int) downloadId);
                    return;
                }

            }
        });

    }


    private void startDownload() {
        String url = urlEditText.getText().toString().trim();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Download");
        request.setDescription("Downloading file.....");

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,""+System.currentTimeMillis());
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        //downloadManager.enqueue(request);
        downloadId = downloadManager.enqueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_STORAGE_CODE:{
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startDownload();
                }else {
                    Toast.makeText(this,"Permission denied...!",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void cancelDownload(){
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.remove(downloadId);
        if(downloadId!=0){
            Toast.makeText(getApplicationContext(),"下载取消\nDownloading Cancel",Toast.LENGTH_SHORT).show();
        }
    }

}
