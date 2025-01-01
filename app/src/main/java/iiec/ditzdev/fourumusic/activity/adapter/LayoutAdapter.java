package iiec.ditzdev.fourumusic.activity.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import iiec.ditzdev.fourumusic.activity.fragment.FragmentDownload;
import iiec.ditzdev.fourumusic.activity.fragment.FragmentHome;
import iiec.ditzdev.fourumusic.activity.fragment.FragmentSong;

public class LayoutAdapter extends FragmentStateAdapter {
    public LayoutAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new FragmentHome();
            case 1: return new FragmentSong();
            case 2: return new FragmentDownload();
            default: return new FragmentHome();
        }
    }
    
    @Override
    public int getItemCount() {
        return 3;
    }
}
