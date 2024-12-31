package iiec.ditzdev.fourumusic.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.util.Date;
import android.app.Notification;
import android.os.IBinder;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.io.FileOutputStream;
import android.os.Build;
import android.app.NotificationChannel;
import androidx.core.app.NotificationCompat;
import android.app.PendingIntent;

public class CrashHandlerService extends Service {
    public static final String ACTION_HANDLE_CRASH = "action_handle_crash";
    public static final String ACTION_SHOW_NOTIFICATION = "action_show_notification";
    public static final String ACTION_RESTART_APP = "action_restart_app";
    public static final String ACTION_SHARE_LOG = "action_share_log";
    
    private static final String NOTIFICATION_CHANNEL_ID = "crash_handler_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_HANDLE_CRASH.equals(action) || 
                ACTION_SHOW_NOTIFICATION.equals(action)) {
                showCrashNotification();
            } else if (ACTION_RESTART_APP.equals(action)) {
                restartApp();
            } else if (ACTION_SHARE_LOG.equals(action)) {
                shareCrashLog();
            }
        }
        return START_STICKY;
    }

    private void showCrashNotification() {
        startForeground(NOTIFICATION_ID, createNotification());
    }

    private Notification createNotification() {
        Intent restartIntent = new Intent(this, CrashHandlerService.class)
            .setAction(ACTION_RESTART_APP);
        PendingIntent restartPendingIntent = PendingIntent.getService(
            this, 0, restartIntent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        Intent shareIntent = new Intent(this, CrashHandlerService.class)
            .setAction(ACTION_SHARE_LOG);
        PendingIntent sharePendingIntent = PendingIntent.getService(
            this, 1, shareIntent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("DevCode Has Crashed")
            .setContentText("An Error Occurred in the Application Process, Press Share Log to see the Error details")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setAutoCancel(true)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_share, "Share Log", sharePendingIntent)
            .addAction(android.R.drawable.ic_menu_rotate, "Restart App", restartPendingIntent)
            .build();
    }

    private void shareCrashLog() {
        try {
            String lastCrashFile = getSharedPreferences("crash_prefs", MODE_PRIVATE)
                .getString("last_crash_file", null);
            
            if (lastCrashFile != null) {
                File crashFile = new File(lastCrashFile);
                if (crashFile.exists()) {
                    Uri fileUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        crashFile
                    );

                    Intent shareIntent = new Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_STREAM, fileUri)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(Intent.createChooser(shareIntent, "Share Crash Log")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restartApp() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                          Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        stopSelf();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "DevCode Crash",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Catch any unexpected errors");
            channel.enableLights(true);
            channel.enableVibration(true);
            
            NotificationManager notificationManager = 
                getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}