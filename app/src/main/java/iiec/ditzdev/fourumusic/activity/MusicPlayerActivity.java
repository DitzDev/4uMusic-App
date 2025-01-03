package iiec.ditzdev.fourumusic.activity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import iiec.ditzdev.fourumusic.R;
import iiec.ditzdev.fourumusic.activity.adapter.MusicAdapter;
import iiec.ditzdev.fourumusic.databinding.ActivityMusicPlayerBinding;
import iiec.ditzdev.fourumusic.models.MusicModels;
import iiec.ditzdev.fourumusic.service.MusicService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MusicPlayerActivity extends AppCompatActivity {
    private ActivityMusicPlayerBinding binding;
    private MediaPlayer mediaPlayer;
    private MusicService musicService;
    private boolean serviceBound = false;
    private ArrayList<MusicModels> musicList;
    private ArrayList<MusicModels> originalList;
    private int currentPosition;
    private Handler handler = new Handler();
    private Runnable runnable;
    private boolean isShuffleOn = false;
    private int repeatMode = 0; // 0: no repeat, 1: repeat all, 2: repeat one
    private Random random = new Random();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMusicPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        musicList = getIntent().getParcelableArrayListExtra("MUSIC_LIST");
        originalList = new ArrayList<>(musicList);
        currentPosition = getIntent().getIntExtra("POSITION", 0);
        updateToolbarInfo(currentPosition);
        initializeMediaPlayer();
        setupClickListeners();
        setupSlider();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            showDeleteDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            serviceBound = true;
            musicService.setMusicList(musicList, currentPosition);
            musicService.playMusic(currentPosition);
            updateUI(currentPosition);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
    
    private void updateToolbarInfo(int position) {
        MusicModels currentSong = musicList.get(position);
        binding.toolbar.setTitle(currentSong.getTitle());
        if (binding.toolbar.getTitle() instanceof TextView) {
            TextView titleView = (TextView) binding.toolbar.getTitle();
            titleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleView.setSelected(true);
            titleView.setSingleLine(true);
        }
    }     
    private void getMusicData() {
        musicList = getIntent().getParcelableArrayListExtra("MUSIC_LIST");
        currentPosition = getIntent().getIntExtra("POSITION", 0);
        updateToolbarInfo(currentPosition);
    }

    private void bindService() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void showDeleteDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Lagu")
            .setMessage("Apakah Anda yakin ingin menghapus lagu ini?")
            .setPositiveButton("Hapus", (dialog, which) -> deleteSong())
            .setNegativeButton("Batal", null)
            .show();
    }

    private void deleteSong() {
        MusicModels currentSong = musicList.get(currentPosition);
        File file = new File(currentSong.getPath());
        
        if (file.delete()) {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.DATA + "=?";
            String[] selectionArgs = new String[]{ currentSong.getPath() };
            contentResolver.delete(uri, selection, selectionArgs);
            musicList.remove(currentPosition);
            originalList.remove(currentPosition);
            
            if (musicList.isEmpty()) {
                finish();
            } else {
                playNextSong();
            }
        }
    }

    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        playMusic(currentPosition);
    }

    private void setupClickListeners() {
        binding.playPauseBtn.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                pauseMusic();
            } else {
                playMusic();
            }
        });

        binding.nextBtn.setOnClickListener(v -> playNextSong());
        binding.previousBtn.setOnClickListener(v -> playPreviousSong());

        binding.shuffleBtn.setOnClickListener(v -> toggleShuffle());
        binding.repeatBtn.setOnClickListener(v -> toggleRepeat());
    }

    private void setupSlider() {
        binding.seekBar.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo((int) value);
                }
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    binding.seekBar.setValue(mediaPlayer.getCurrentPosition());
                    binding.tvCurrentTime.setText(formatDuration(mediaPlayer.getCurrentPosition()));
                }
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(runnable, 100);
    }

    private void playMusic(int position) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicList.get(position).getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            
            updateUI(position);
            
            mediaPlayer.setOnCompletionListener(mp -> {
                if (repeatMode == 2) { // Repeat one
                    playMusic(currentPosition);
                } else {
                    playNextSong();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUI(int position) {
        MusicModels currentSong = musicList.get(position);
        updateToolbarInfo(position);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(currentSong.getPath());
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            binding.toolbar.setSubtitle(artist != null ? artist : "Artis tidak diketahui");
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                binding.albumArt.setImageBitmap(bitmap);
            } else {
                binding.albumArt.setImageResource(R.drawable.icon_music_note);
            }
        } catch (Exception e) {
            binding.albumArt.setImageResource(R.drawable.icon_music_note);
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Update controls
        binding.playPauseBtn.setImageResource(
        mediaPlayer.isPlaying() ? R.drawable.icon_pause : R.drawable.icon_play);
        binding.seekBar.setValueFrom(0);
        binding.seekBar.setValueTo(mediaPlayer.getDuration());
        binding.tvTotalDuration.setText(formatDuration(mediaPlayer.getDuration()));
    }

    private void playMusic() {
        mediaPlayer.start();
        binding.playPauseBtn.setImageResource(R.drawable.icon_pause);
    }

    private void pauseMusic() {
        mediaPlayer.pause();
        binding.playPauseBtn.setImageResource(R.drawable.icon_play);
    }

    private void playNextSong() {
        if (isShuffleOn) {
            currentPosition = random.nextInt(musicList.size());
        } else {
            currentPosition = (currentPosition + 1) % musicList.size();
        }
        playMusic(currentPosition);
    }

    private void playPreviousSong() {
        if (isShuffleOn) {
            currentPosition = random.nextInt(musicList.size());
        } else {
            currentPosition = (currentPosition - 1 + musicList.size()) % musicList.size();
        }
        playMusic(currentPosition);
    }

    private void toggleShuffle() {
        isShuffleOn = !isShuffleOn;
        binding.shuffleBtn.setIconTint(
            ContextCompat.getColorStateList(this, 
                isShuffleOn ? R.color.material_dynamic_primary60 : R.color.material_dynamic_neutral30));
                
        Toast.makeText(this, 
            isShuffleOn ? "Mode acak aktif" : "Mode acak nonaktif", 
            Toast.LENGTH_SHORT).show();
    }    
    private void toggleRepeat() {
        repeatMode = (repeatMode + 1) % 3; // Cycle between 0, 1, 2
        
        int iconRes;
        int tintColor;
        String toastMessage;
        
        switch (repeatMode) {
            case 1: // Repeat all
                iconRes = R.drawable.icon_repeat;
                tintColor = R.color.material_dynamic_primary60;
                toastMessage = "Ulangi semua lagu";
                break;
            case 2: // Repeat one
                iconRes = R.drawable.icon_repeat_one;
                tintColor = R.color.material_dynamic_primary60;
                toastMessage = "Ulangi lagu saat ini";
                break;
            default: // No repeat
                iconRes = R.drawable.icon_repeat;
                tintColor = R.color.material_dynamic_neutral30;
                toastMessage = "Mode pengulangan nonaktif";
                break;
        }
        
        binding.repeatBtn.setIcon(ContextCompat.getDrawable(this, iconRes));
        binding.repeatBtn.setIconTint(ContextCompat.getColorStateList(this, tintColor));
        
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
    }

    private String formatDuration(long duration) {
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseMusic();
    }

   @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}