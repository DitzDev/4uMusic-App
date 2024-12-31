package iiec.ditzdev.fourumusic.app;

import androidx.appcompat.app.AppCompatDelegate;
import iiec.ditzdev.fourumusic.service.CrashHandlerService;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import java.io.StringWriter;
import java.io.PrintWriter;
import android.os.Build;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.io.File;
import java.io.FileOutputStream;
import android.util.Log;


public class MainApp extends Application {
    private static final String CRASH_FLAG = "crash_flag";
    private static final String LAST_CRASH_TIMESTAMP = "last_crash_timestamp";
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("crash_prefs", Context.MODE_PRIVATE);
        setupCrashHandler();
        checkPreviousCrash();
        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        int savedTheme = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);
    }

    private void setupCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, throwable) -> {
                    prefs.edit()
                            .putBoolean(CRASH_FLAG, true)
                            .putLong(LAST_CRASH_TIMESTAMP, System.currentTimeMillis())
                            .apply();
                    String crashInfo = generateCrashInfo(thread, throwable);
                    saveCrashToFile(crashInfo);
                    Intent crashIntent = new Intent(this, CrashHandlerService.class);
                    crashIntent.setAction(CrashHandlerService.ACTION_HANDLE_CRASH);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(crashIntent);
                    } else {
                        startService(crashIntent);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                   //Process.killProcess(Process.myPid());
                    System.exit(10);
                });
    }

    private void checkPreviousCrash() {
        if (prefs.getBoolean(CRASH_FLAG, false)) {
            Intent intent = new Intent(this, CrashHandlerService.class);
            intent.setAction(CrashHandlerService.ACTION_SHOW_NOTIFICATION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            prefs.edit().putBoolean(CRASH_FLAG, false).apply();
        }
    }

    private String generateCrashInfo(Thread thread, Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        return new StringBuilder()
                .append("####################################\n")
                .append("4uMusic App Crash Report Service\n\n")
                .append(
                        "This error is taken based on activities that may not be supported by some versions of Android. Please report this error to: <sxtifyglobal@gmail.com>\n")
                .append("####################################\n\n")
                .append("Crash Report\n\n")
                .append("Time: ")
                .append(
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                .format(new Date()))
                .append("\n")
                .append("Thread: ")
                .append(thread.getName())
                .append("\n")
                .append("Error Type: ")
                .append(throwable.getClass().getName())
                .append("\n")
                .append("Error Message: ")
                .append(throwable.getMessage())
                .append("\n\n")
                .append("Device Information:\n")
                .append("Brand: ")
                .append(Build.BRAND)
                .append("\n")
                .append("Device: ")
                .append(Build.DEVICE)
                .append("\n")
                .append("Model: ")
                .append(Build.MODEL)
                .append("\n")
                .append("Android Version: ")
                .append(Build.VERSION.RELEASE)
                .append("\n")
                .append("SDK: ")
                .append(Build.VERSION.SDK_INT)
                .append("\n\n")
                .append("Stack Trace:\n")
                .append(sw.toString())
                .toString();
    }

    private void saveCrashToFile(String crashInfo) {
        try {
            File crashDir = new File(getExternalFilesDir(null), "crash_reports");
            if (!crashDir.exists()) {
                crashDir.mkdirs();
            }

            String timestamp =
                    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File crashFile = new File(crashDir, "crash_" + timestamp + ".txt");

            FileOutputStream fos = new FileOutputStream(crashFile);
            fos.write(crashInfo.getBytes());
            fos.close();
            prefs.edit().putString("last_crash_file", crashFile.getAbsolutePath()).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}