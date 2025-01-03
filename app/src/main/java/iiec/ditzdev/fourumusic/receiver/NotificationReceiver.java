package iiec.ditzdev.fourumusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import iiec.ditzdev.fourumusic.service.MusicService;

public class NotificationReceiver extends BroadcastReceiver {
    private MusicService musicService;

    public NotificationReceiver(MusicService service) {
        this.musicService = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case "PLAY":
                    musicService.playMusic();
                    break;
                case "PAUSE":
                    musicService.pauseMusic();
                    break;
                case "NEXT":
                    musicService.playNextSong();
                    break;
                case "PREVIOUS":
                    musicService.playPreviousSong();
                    break;
            }
        }
    }
}