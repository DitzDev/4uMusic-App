package iiec.ditzdev.fourumusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import iiec.ditzdev.fourumusic.R;
import iiec.ditzdev.fourumusic.activity.MusicPlayerActivity;
import iiec.ditzdev.fourumusic.models.MusicModels;
import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service {
    public static final String CHANNEL_ID = "MUSIC_PLAYER_CHANNEL";
    public static final int NOTIFICATION_ID = 1;
    
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new LocalBinder();
    private ArrayList<MusicModels> musicList;
    private int currentPosition;
    private MediaSessionCompat mediaSession;
    private boolean isShuffleOn = false;
    private int repeatMode = 0;

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        createNotificationChannel();
        initMediaSession();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Controls for music player");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, "MusicService");
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                playMusic();
            }

            @Override
            public void onPause() {
                pauseMusic();
            }

            @Override
            public void onSkipToNext() {
                playNextSong();
            }

            @Override
            public void onSkipToPrevious() {
                playPreviousSong();
            }
        });
        mediaSession.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setMusicList(ArrayList<MusicModels> musicList, int position) {
        this.musicList = musicList;
        this.currentPosition = position;
    }

    public void playMusic(int position) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicList.get(position).getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentPosition = position;
            updateMediaSession();
            showNotification();
            
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

    public void playMusic() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateMediaSession();
            showNotification();
        }
    }

    public void pauseMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateMediaSession();
            showNotification();
        }
    }

    public void playNextSong() {
        if (isShuffleOn) {
            currentPosition = (int) (Math.random() * musicList.size());
        } else {
            currentPosition = (currentPosition + 1) % musicList.size();
        }
        playMusic(currentPosition);
    }

    public void playPreviousSong() {
        if (isShuffleOn) {
            currentPosition = (int) (Math.random() * musicList.size());
        } else {
            currentPosition = (currentPosition - 1 + musicList.size()) % musicList.size();
        }
        playMusic(currentPosition);
    }

    private void updateMediaSession() {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY | 
                       PlaybackStateCompat.ACTION_PAUSE |
                       PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                       PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            .setState(mediaPlayer.isPlaying() ? 
                     PlaybackStateCompat.STATE_PLAYING : 
                     PlaybackStateCompat.STATE_PAUSED,
                     mediaPlayer.getCurrentPosition(),
                     1.0f);
                     
        mediaSession.setPlaybackState(stateBuilder.build());

        MusicModels currentSong = musicList.get(currentPosition);
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.getTitle());

        // Get artist and album art
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(currentSong.getPath());
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (artist != null) {
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist);
            }

            byte[] artBytes = retriever.getEmbeddedPicture();
            if (artBytes != null) {
                Bitmap art = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, art);
            }
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mediaSession.setMetadata(metadataBuilder.build());
    }

    private void showNotification() {
        MusicModels currentSong = musicList.get(currentPosition);
        
        // Intent for opening the player activity
        Intent intent = new Intent(this, MusicPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Media control actions
        NotificationCompat.Action prevAction = new NotificationCompat.Action(
            R.drawable.icon_skip_previous, "Previous",
            retrievePlaybackAction(3));

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
            mediaPlayer.isPlaying() ? R.drawable.icon_pause : R.drawable.icon_play,
            mediaPlayer.isPlaying() ? "Pause" : "Play",
            retrievePlaybackAction(mediaPlayer.isPlaying() ? 1 : 0));

        NotificationCompat.Action nextAction = new NotificationCompat.Action(
            R.drawable.icon_skip_next, "Next",
            retrievePlaybackAction(2));

        // Get album art
        Bitmap albumArt = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(currentSong.getPath());
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                albumArt = BitmapFactory.decodeByteArray(art, 0, art.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_music_note)
            .setLargeIcon(albumArt)
            .setContentTitle(currentSong.getTitle())
            .setContentText(getArtistName(currentSong.getPath()))
            .setContentIntent(contentIntent)
            .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2))
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .setOngoing(mediaPlayer.isPlaying())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // Show notification
        if (mediaPlayer.isPlaying()) {
            startForeground(NOTIFICATION_ID, builder.build());
        } else {
            stopForeground(false);
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
        }
    }

    private PendingIntent retrievePlaybackAction(int action) {
        Intent intent = new Intent();
        
        switch (action) {
            case 0: // Play
                intent.setAction("PLAY");
                break;
            case 1: // Pause
                intent.setAction("PAUSE");
                break;
            case 2: // Next
                intent.setAction("NEXT");
                break;
            case 3: // Previous
                intent.setAction("PREVIOUS");
                break;
        }
        
        return PendingIntent.getBroadcast(this, action, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private String getArtistName(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            return artist != null ? artist : "Artis tidak diketahui";
        } catch (Exception e) {
            return "Artis tidak diketahui";
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setShuffleMode(boolean isShuffleOn) {
        this.isShuffleOn = isShuffleOn;
    }

    public void setRepeatMode(int repeatMode) {
        this.repeatMode = repeatMode;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }
    }
}