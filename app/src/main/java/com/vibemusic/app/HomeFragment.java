package com.vibemusic.app;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    TextView textViewSong;
    private boolean isReceiverRegistered = false;


    private final BroadcastReceiver songChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && MusicService.ACTION_SONG_CHANGED.equals(intent.getAction())) {
                String songName = intent.getStringExtra("songName");
                if (textViewSong != null && songName != null) {
                    textViewSong.setText(songName);
                }
            }
        }
    };

    public HomeFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.songRecyclerView);
        textViewSong = view.findViewById(R.id.textViewSong);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Song> songs = SongRepository.getSongs();

        SongAdapter adapter = new SongAdapter(songs);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(MusicService.ACTION_SONG_CHANGED);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireContext().registerReceiver(
                        songChangedReceiver,
                        filter,
                        Context.RECEIVER_NOT_EXPORTED
                );
            } else {
                requireContext().registerReceiver(songChangedReceiver, filter);
            }

            isReceiverRegistered = true;
        }
    }

    @Override
    public void onPause() {
        Context context = getContext();
        if (context != null && isReceiverRegistered) {
            context.unregisterReceiver(songChangedReceiver);
            isReceiverRegistered = false;
        }
        super.onPause();
    }
}
