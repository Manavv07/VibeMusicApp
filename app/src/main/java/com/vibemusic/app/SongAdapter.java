package com.vibemusic.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private List<Song> songList;

    public SongAdapter(List<Song> songList) {
        this.songList = songList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView artist;

        public ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(android.R.id.text1);
            artist = itemView.findViewById(android.R.id.text2);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    MusicPlayerManager.play(v.getContext(), position);

                    if (v.getContext() instanceof MainActivity) {
                        ((MainActivity) v.getContext()).refreshMiniPlayer();
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Song song = songList.get(position);

        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }
}