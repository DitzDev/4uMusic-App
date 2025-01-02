package iiec.ditzdev.fourumusic.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import iiec.ditzdev.fourumusic.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    
    private ActivitySettingsBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }
}
