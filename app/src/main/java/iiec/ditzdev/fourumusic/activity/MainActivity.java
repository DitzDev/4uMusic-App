package iiec.ditzdev.fourumusic.activity;

import iiec.ditzdev.fourumusic.R;
import android.icu.util.Calendar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import iiec.ditzdev.fourumusic.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        /* Greeting */
        binding.toolbar.setSubtitle(greetingTime());
    }
    
    private String greetingTime() {
        Calendar calender = Calendar.getInstance();
        int hour = calender.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 &&  hour < 12) {
            greeting = getString(R.string.greeting_morning);
        } else if (hour >= 12 && hour < 15) {
            greeting = getString(R.string.greeting_afternoon);
        } else if (hour >= 15 && hour < 19) {
            greeting = getString(R.string.good_afternoon);
        } else {
            greeting = getString(R.string.good_night);
        }
        return greeting;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
