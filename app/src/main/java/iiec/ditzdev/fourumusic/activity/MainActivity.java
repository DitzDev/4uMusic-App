package iiec.ditzdev.fourumusic.activity;

import com.google.android.material.tabs.TabLayoutMediator;
import iiec.ditzdev.fourumusic.R;
import android.icu.util.Calendar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import iiec.ditzdev.fourumusic.activity.adapter.LayoutAdapter;
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
        /* Set adapter */
        LayoutAdapter adapter = new LayoutAdapter(this);
        binding.viewPager.setAdapter(adapter);
        
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, strategy) -> {
            switch (strategy) {
                case 0: tab.setText(getString(R.string.tabs_item_home)); break;
                case 1: tab.setText(getString(R.string.tabs_item_song)); break;
                case 2: tab.setText(getString(R.string.tabs_item_download)); break;
            }
        }).attach();
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
