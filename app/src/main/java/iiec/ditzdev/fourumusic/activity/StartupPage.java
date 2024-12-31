package iiec.ditzdev.fourumusic.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import iiec.ditzdev.fourumusic.R;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import iiec.ditzdev.fourumusic.components.SettingsLayout;
import iiec.ditzdev.fourumusic.components.SettingsRadioCheck;
import iiec.ditzdev.fourumusic.components.SettingsSwitch;

public class StartupPage extends AppCompatActivity {
    
    private Button btnContinue;
    private SettingsSwitch btnPermStorage;
    private SettingsLayout setTheme;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_page);
        btnContinue = findViewById(R.id.btnContinue);
        btnPermStorage = findViewById(R.id.btnPermStorage);
        setTheme = findViewById(R.id.btnSetTheme);
        // Default Disabled btnContinue
        btnContinue.setEnabled(false);
        setTheme.setOnClickListener(v -> showDialogTheme());
    }
    
    private void showDialogTheme() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View showView = inflater.inflate(R.layout.custom_settheme_dialog, null);
        SettingsRadioCheck setFollowSystem = showView.findViewById(R.id.set_folow_system);
        SettingsRadioCheck setDarkTheme = showView.findViewById(R.id.set_theme_dark);
        SettingsRadioCheck setLightTheme = showView.findViewById(R.id.set_theme_light);
        
        new MaterialAlertDialogBuilder(this)
        .setView(showView)
        .setPositiveButton("OK, Set this", (dialog, which) -> {
            dialog.dismiss();
        })
        .setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        })
        .show();
    }
}
