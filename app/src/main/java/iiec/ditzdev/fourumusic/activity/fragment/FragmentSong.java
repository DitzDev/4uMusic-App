package iiec.ditzdev.fourumusic.activity.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import iiec.ditzdev.fourumusic.databinding.FragmentSongLayoutBinding;

public class FragmentSong extends Fragment {
    private FragmentSongLayoutBinding binding;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflate, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSongLayoutBinding.inflate(inflate, container, false);
        return binding.getRoot();
    }
    
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
