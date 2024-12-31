package iiec.ditzdev.fourumusic.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import iiec.ditzdev.fourumusic.R;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Timer;
import java.util.TimerTask;

public class Splash extends AppCompatActivity {
    
    private Timer _timer = new Timer();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        int savedTheme = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);
        setContentView(R.layout.splash);
        boolean isNewUser = prefs.getBoolean("isNewUser", true);
        TimerTask timer = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override    
                    public void run() {
                        if (isNewUser) {
                            Intent intent = new Intent(Splash.this, StartupPage.class);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);     
                            finish();
                        } else {
                            Intent intent = new Intent(Splash.this, MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);     
                            finish();
                        }
                    }
                });
            }
        };
        _timer.schedule(timer, 4000);
    }
}