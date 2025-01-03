package iiec.ditzdev.fourumusic.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import iiec.ditzdev.fourumusic.activity.MusicView;
import iiec.ditzdev.fourumusic.databinding.FragmentHomeLayoutBinding;

public class FragmentHome extends Fragment {
    private FragmentHomeLayoutBinding binding;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflate, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeLayoutBinding.inflate(inflate, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        binding.recentSongs.setOnClickListener(p -> {
            startActivity(new Intent(requireContext(), MusicView.class));
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
