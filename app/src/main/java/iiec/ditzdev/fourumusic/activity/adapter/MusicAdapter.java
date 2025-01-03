package iiec.ditzdev.fourumusic.activity.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import iiec.ditzdev.fourumusic.activity.MusicPlayerActivity;
import java.util.ArrayList;
import iiec.ditzdev.fourumusic.R;
import iiec.ditzdev.fourumusic.models.MusicModels;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private Context context;
    private ArrayList<MusicModels> musicList;
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onItemClick(MusicModels music);
    }

    public MusicAdapter(Context context, ArrayList<MusicModels> musicList) {
        this.context = context;
        this.musicList = musicList;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        MusicModels music = musicList.get(position);
        holder.musicFileName.setText(music.getTitle());
        holder.musicFileName.setSelected(true);
        holder.musicDuration.setText(formatDuration(music.getDuration()));
        holder.itemView.setOnClickListener(
                v -> {
                    if (listener != null) {
                        listener.onItemClick(music);
                        context.startActivity(new Intent(context, MusicPlayerActivity.class).putParcelableArrayListExtra("MUSIC_LIST", musicList).putExtra("POSITION", position));
                    }
                });
        }
    @Override
    public int getItemCount() {
        return musicList.size();
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView musicFileName, musicDuration;
        ImageView musicThumbnail;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            musicFileName = itemView.findViewById(R.id.musicFileName);
            musicDuration = itemView.findViewById(R.id.musicDuration);
        }
    }

    private String formatDuration(String duration) {
        long millis = Long.parseLong(duration);
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}