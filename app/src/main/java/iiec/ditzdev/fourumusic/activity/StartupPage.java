package iiec.ditzdev.fourumusic.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import iiec.ditzdev.fourumusic.R;
import iiec.ditzdev.fourumusic.components.SettingsLayout;
import iiec.ditzdev.fourumusic.components.SettingsRadioCheck;
import iiec.ditzdev.fourumusic.components.SettingsSwitch;

public class StartupPage extends AppCompatActivity {
    
    private Button btnContinue;
    private SettingsSwitch btnPermStorage;
    private SettingsLayout setTheme;
    private SharedPreferences prefs;
    private boolean isStoragePermissionGranted = false;
    private boolean isThemeSelected = false;
    
    private final ActivityResultLauncher<String> requestPermissionLauncher = 
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                handleStoragePermissionGranted();
            }
        });
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_page);
        prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        
        btnContinue = findViewById(R.id.btnContinue);
        btnPermStorage = findViewById(R.id.btnPermStorage);
        setTheme = findViewById(R.id.btnSetTheme);
        
        // Default Disabled btnContinue
        btnContinue.setEnabled(false);
        isThemeSelected = prefs.contains("theme_mode");
        setTheme.setOnClickListener(v -> showDialogTheme());
        btnPermStorage.setOnClickListener(v -> requestStoragePermission());
        btnContinue.setOnClickListener(v -> proceedToMainActivity());
        checkStoragePermission();
    }
    
    private void handleStoragePermissionGranted() {
        isStoragePermissionGranted = true;
        btnPermStorage.setChecked(true);
        checkAllPermissions();
    }
    
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                handleStoragePermissionGranted();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED) {
                handleStoragePermissionGranted();
            }
        }
    }
    
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }
    
    private void showDialogTheme() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View showView = inflater.inflate(R.layout.custom_settheme_dialog, null);
        
        SettingsRadioCheck setFollowSystem = showView.findViewById(R.id.set_folow_system);
        SettingsRadioCheck setDarkTheme = showView.findViewById(R.id.set_theme_dark);
        SettingsRadioCheck setLightTheme = showView.findViewById(R.id.set_theme_light);
        setFollowSystem.setOnClickListener(v -> {
            setFollowSystem.setChecked(true);
            setDarkTheme.setChecked(false);
            setLightTheme.setChecked(false);
        });
        
        setDarkTheme.setOnClickListener(v -> {
            setFollowSystem.setChecked(false);
            setDarkTheme.setChecked(true);
            setLightTheme.setChecked(false);
        });
        
        setLightTheme.setOnClickListener(v -> {
            setFollowSystem.setChecked(false);
            setDarkTheme.setChecked(false);
            setLightTheme.setChecked(true);
        });
        
        int currentTheme = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        switch (currentTheme) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                setFollowSystem.setChecked(true);
                setDarkTheme.setChecked(false);
                setLightTheme.setChecked(false);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                setFollowSystem.setChecked(false);
                setDarkTheme.setChecked(true);
                setLightTheme.setChecked(false);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                setFollowSystem.setChecked(false);
                setDarkTheme.setChecked(false);
                setLightTheme.setChecked(true);
                break;
        }
        
        new MaterialAlertDialogBuilder(this)
            .setView(showView)
            .setPositiveButton(getString(R.string.action_ok), (dialog, which) -> {
                int selectedTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                SharedPreferences.Editor editor = prefs.edit();
                if (setFollowSystem.isChecked()) {
                    selectedTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                } else if (setDarkTheme.isChecked()) {
                    selectedTheme = AppCompatDelegate.MODE_NIGHT_YES;
                } else if (setLightTheme.isChecked()) {
                    selectedTheme = AppCompatDelegate.MODE_NIGHT_NO;
                }
                SharedPreferences.Editor editorTheme = prefs.edit();
                editorTheme.putInt("theme_mode", selectedTheme);
                editorTheme.apply();
                AppCompatDelegate.setDefaultNightMode(selectedTheme);
                isThemeSelected = true;
                checkAllPermissions();
                dialog.dismiss();
                recreate();
            })
            .setNegativeButton(getString(R.string.action_cancel), (dialog, which) -> dialog.dismiss())
            .show();
    }
    
    private void checkAllPermissions() {
        btnContinue.setEnabled(isStoragePermissionGranted/* && isThemeSelected*/);
    }
    
    private void proceedToMainActivity() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isNewUser", false);
        editor.apply();
        Intent intent = new Intent(StartupPage.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2296) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    handleStoragePermissionGranted();
                }
            }
        }
    }
}