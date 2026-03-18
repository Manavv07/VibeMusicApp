package com.vibemusic.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    // Mini Player Views
    View miniPlayer;
    TextView miniTitle;
    ImageButton btnPrev, btnPlayPause, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1);
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Mini Player Init
        miniPlayer = findViewById(R.id.mini_player);
        miniTitle = findViewById(R.id.mini_title);
        btnPrev = findViewById(R.id.btn_prev);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnNext = findViewById(R.id.btn_next);

        // Button Clicks
        btnPrev.setOnClickListener(v -> {
            MusicPlayerManager.playPrevious(this);
            updateMiniPlayer();
        });

        btnPlayPause.setOnClickListener(v -> {
            MusicPlayerManager.togglePlayPause(this);
            updateMiniPlayer();
        });

        btnNext.setOnClickListener(v -> {
            MusicPlayerManager.playNext(this);
            updateMiniPlayer();
        });

        // Load Home fragment by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            }

            if (id == R.id.nav_search) {
                loadFragment(new SearchFragment());
                return true;
            }

            if (id == R.id.nav_library) {
                loadFragment(new LibraryFragment());
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMiniPlayer();
    }

    private void updateMiniPlayer() {

        // Show only if a song exists
        if (MusicPlayerManager.getCurrentSong() == null) {
            miniPlayer.setVisibility(View.GONE);
            return;
        }

        miniPlayer.setVisibility(View.VISIBLE);

        // Update title
        miniTitle.setText(MusicPlayerManager.getCurrentSong().getTitle());

        // Update play/pause icon
        if (MusicPlayerManager.isPlaying()) {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    // ✅ OUTSIDE the above method
    public void refreshMiniPlayer() {
        updateMiniPlayer();
    }

}