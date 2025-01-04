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
import iiec.ditzdev.fourumusic.service.MusicService.OnSongChangedListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MusicPlayerActivity extends AppCompatActivity {
    private ActivityMusicPlayerBinding binding;
    private MusicService musicService;
    private boolean serviceBound = false;
    private ArrayList<MusicModels> musicList;
    private ArrayList<MusicModels> originalList;
    private int currentPosition;
    private Handler handler = new Handler();
    private Runnable runnable;
    private boolean isShuffleOn = true;
    private int repeatMode = 0; // 0: no repeat, 1: repeat all, 2: repeat one
    private Random random = new Random();
    
    public interface OnSongChangeListener {
        void onSongChanged(int newPosition);
    }

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
        Intent serviceIntent = new Intent(this, MusicService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        updateToolbarInfo(currentPosition);
        setupClickListeners();
        setupSlider();
        if (musicList != null && !musicList.isEmpty()) {
            startMusicService();
        }
        binding.repeatBtn.setIconTint(
                ContextCompat.getColorStateList(this, R.color.material_dynamic_neutral30));
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

    private void startMusicService() {
        Intent serviceIntent = new Intent(this, MusicService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection =
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
                    musicService = binder.getService();
                    serviceBound = true;
                    musicService.setMusicList(musicList, currentPosition);
                    musicService.setShuffleMode(isShuffleOn);
                    musicService.setRepeatMode(repeatMode);
                    musicService.setOnSongChangeListener(position -> {
                        runOnUiThread(() -> {
                            currentPosition = position;
                            updateUI(currentPosition);
                        });
                    });
                    if (!musicService.isPlaying()) {
                        musicService.playMusic(currentPosition);
                    }
                    updateUI(currentPosition);
                    // setupMediaPlayerControls();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    musicService = null;
                    serviceBound = false;
                }
            };

    private void setupMediaPlayerControls() {
        binding.playPauseBtn.setOnClickListener(
                v -> {
                    if (serviceBound) {
                        if (musicService.isPlaying()) {
                            musicService.pauseMusic();
                        } else {
                            musicService.playMusic();
                        }
                        updatePlayPauseButton();
                    }
                });

        binding.nextBtn.setOnClickListener(
                v -> {
                    if (serviceBound) {
                        musicService.playNextSong();
                        updateUI(musicService.getCurrentPosition());
                    }
                });

        binding.previousBtn.setOnClickListener(
                v -> {
                    if (serviceBound) {
                        musicService.playPreviousSong();
                        updateUI(musicService.getCurrentPosition());
                    }
                });
    }

    private void updatePlayPauseButton() {
        if (serviceBound) {
            binding.playPauseBtn.setImageResource(
                    musicService.isPlaying() ? R.drawable.icon_pause : R.drawable.icon_play);
        }
    }

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
                .setTitle(getString(R.string.action_dialog_title_delete))
                .setMessage(R.string.action_dialog_subtitle_delete)
                .setPositiveButton(
                        getString(R.string.action_dialog_title_delete),
                        (dialog, which) -> deleteSong())
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private void deleteSong() {
        MusicModels currentSong = musicList.get(currentPosition);
        File file = new File(currentSong.getPath());

        if (file.delete()) {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.DATA + "=?";
            String[] selectionArgs = new String[] {currentSong.getPath()};
            contentResolver.delete(uri, selection, selectionArgs);
            musicList.remove(currentPosition);
            originalList.remove(currentPosition);

            if (musicList.isEmpty()) {
                finish();
            } else {
                musicService.playNextSong();
            }
        }
    }

    private void setupClickListeners() {
        binding.playPauseBtn.setOnClickListener(
                v -> {
                    if (serviceBound && musicService != null) {
                        if (musicService.isPlaying()) {
                            musicService.pauseMusic();
                        } else {
                            musicService.playMusic();
                        }
                        updatePlayPauseButton();
                    }
                });

        binding.nextBtn.setOnClickListener(
                v -> {
                    if (serviceBound && musicService != null) {
                        musicService.playNextSong();
                        currentPosition = musicService.getCurrentPosition();
                        updateUI(currentPosition);
                    }
                });

        binding.previousBtn.setOnClickListener(
                v -> {
                    if (serviceBound && musicService != null) {
                        musicService.playPreviousSong();
                        currentPosition = musicService.getCurrentPosition();
                        updateUI(currentPosition);
                    }
                });

        binding.shuffleBtn.setOnClickListener(v -> toggleShuffle());
        binding.repeatBtn.setOnClickListener(v -> toggleRepeat());
    }

    private void setupSlider() {
        binding.seekBar.setLabelFormatter(value -> formatDuration((int) value));
        binding.seekBar.addOnChangeListener(
                new Slider.OnChangeListener() {
                    @Override
                    public void onValueChange(Slider slider, float value, boolean fromUser) {
                        if (fromUser && serviceBound && musicService != null) {
                            musicService.getMediaPlayer().seekTo((int) value);
                        }
                    }
                });

        runnable =
                new Runnable() {
                    @Override
                    public void run() {
                        if (serviceBound && musicService != null) {
                            binding.seekBar.setValue(musicService.getCurrentPlaybackPosition());
                            binding.tvCurrentTime.setText(
                                    formatDuration(musicService.getCurrentPlaybackPosition()));
                        }
                        handler.postDelayed(this, 100);
                    }
                };
        handler.postDelayed(runnable, 100);
    }

    private void updateUI(int position) {
        if (!serviceBound || musicService == null) return;

        MusicModels currentSong = musicList.get(position);
        updateToolbarInfo(position);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(currentSong.getPath());
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            binding.toolbar.setSubtitle(
                    artist != null ? artist : getString(R.string.action_string_artist_notfound));

            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                binding.albumArt.setImageBitmap(bitmap);
            } else {
                binding.albumArt.setImageResource(R.drawable.icon_music_note);
            }
            binding.seekBar.setValueFrom(0);
            binding.seekBar.setValueTo(musicService.getTotalDuration());
            binding.tvTotalDuration.setText(formatDuration(musicService.getTotalDuration()));

        } catch (Exception e) {
            binding.albumArt.setImageResource(R.drawable.icon_music_note);
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        updatePlayPauseButton();
    }
    
    /* @Deprecated */
    //  private void pauseMusic() {
    //    musicService.pauseMusic();
    //    binding.playPauseBtn.setImageResource(R.drawable.icon_play);
    //  }
    //
    //  private void playNextSong() {
    //    if (isShuffleOn) {
    //      currentPosition = random.nextInt(musicList.size());
    //    } else {
    //      currentPosition = (currentPosition + 1) % musicList.size();
    //    }
    //    musicService.playMusic(currentPosition);
    //  }
    //
    //    private void playPreviousSong() {
    //        if (isShuffleOn) {
    //            currentPosition = random.nextInt(musicList.size());
    //        } else {
    //            currentPosition = (currentPosition - 1 + musicList.size()) % musicList.size();
    //        }
    //        musicService.playMusic(currentPosition);
    //    }

    private void toggleShuffle() {
        if (repeatMode != 0) {
            Toast.makeText(this, getString(R.string.action_must_disable_repeat), Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        binding.shuffleBtn.setIconTint(
                ContextCompat.getColorStateList(this, R.color.material_dynamic_neutral30));

        isShuffleOn = !isShuffleOn;
        if (serviceBound && musicService != null) {
            musicService.setShuffleMode(isShuffleOn);
        }
        binding.shuffleBtn.setIconTint(
                ContextCompat.getColorStateList(
                        this,
                        isShuffleOn
                                ? R.color.material_dynamic_primary60
                                : R.color.material_dynamic_neutral30));

        Toast.makeText(
                        this,
                        isShuffleOn
                                ? getString(R.string.random_mode_activate)
                                : getString(R.string.random_mode_disabled),
                        Toast.LENGTH_SHORT)
                .show();
    }

    private void toggleRepeat() {
        if (isShuffleOn) {
            Toast.makeText(
                            this,
                            getString(R.string.action_must_disable_shuffle),
                            Toast.LENGTH_SHORT)
                    .show();
            binding.repeatBtn.setIconTint(
                    ContextCompat.getColorStateList(this, R.color.material_dynamic_neutral30));
            return;
        }

        repeatMode = (repeatMode + 1) % 3;
        if (serviceBound && musicService != null) {
            musicService.setRepeatMode(repeatMode);
        }

        int iconRes;
        int tintColor;
        String toastMessage;

        switch (repeatMode) {
            case 1:
                iconRes = R.drawable.icon_repeat;
                tintColor = R.color.material_dynamic_primary60;
                toastMessage = getString(R.string.repeat_allsongs_mode);
                break;
            case 2:
                iconRes = R.drawable.icon_repeat_one;
                tintColor = R.color.material_dynamic_primary60;
                toastMessage = getString(R.string.repeat_songs_one_mode);
                break;
            default:
                iconRes = R.drawable.icon_repeat;
                tintColor = R.color.material_dynamic_neutral30;
                toastMessage = getString(R.string.repeat_mode_nonactivate);
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
    protected void onResume() {
        super.onResume();
        if (serviceBound) {
            updateUI(currentPosition);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound && musicService != null) {
            musicService.setOnSongChangeListener(null);
            unbindService(serviceConnection);
            serviceBound = false;
        }
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
