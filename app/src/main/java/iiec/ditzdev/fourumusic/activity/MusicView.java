package iiec.ditzdev.fourumusic.activity;

import android.content.ContentResolver;
import android.net.Uri;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import iiec.ditzdev.fourumusic.R;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import iiec.ditzdev.fourumusic.activity.adapter.MusicAdapter;
import iiec.ditzdev.fourumusic.databinding.LayoutViewMusicBinding;
import iiec.ditzdev.fourumusic.models.MusicModels;
import java.util.ArrayList;

public class MusicView extends AppCompatActivity {

    private LayoutViewMusicBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LayoutViewMusicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        binding.toolbar.setNavigationOnClickListener(
                v -> {
                    onBackPressed();
                });
        ArrayList<MusicModels> musicList = loadMusic();
        MusicAdapter adapter = new MusicAdapter(this, musicList);
        RecyclerView mView = binding.nestedScrollView.findViewById(R.id.recyclerView);
        mView.setLayoutManager(new LinearLayoutManager(this));
        mView.setAdapter(adapter);
        adapter.setOnItemClickListener(music -> {
            
        });
    }

    private ArrayList<MusicModels> loadMusic() {
        ArrayList<MusicModels> musicList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        };
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String path =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String duration =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                if (duration != null) {
                    musicList.add(new MusicModels(title, path, duration));
                }
            }
            cursor.close();
        }
        return musicList;
    }
}
