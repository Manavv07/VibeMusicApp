package com.vibemusic.app;

import android.content.Context;
import android.content.Intent;

public class MusicPlayerManager {

    private static int currentIndex = -1;
    private static boolean isPlaying = false;
    private static Song currentSong = null;

    public static void play(Context context, int index) {

        currentIndex = index;
        isPlaying = true;

        // ✅ Temporary: empty artist
        currentSong = new Song(
                getTitleFromIndex(index),
                ""
        );

        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY);
        intent.putExtra("resId", getResIdFromIndex(index));

        context.startService(intent);
    }

    public static void pause(Context context) {
        isPlaying = false;

        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(MusicService.ACTION_PAUSE);
        context.startService(intent);
    }

    public static void stop(Context context) {
        isPlaying = false;
        currentIndex = -1;
        currentSong = null;

        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(MusicService.ACTION_STOP);
        context.startService(intent);
    }

    // ✅ Toggle Play/Pause
    public static void togglePlayPause(Context context) {
        if (isPlaying) {
            pause(context);
        } else {
            if (currentIndex != -1) {
                play(context, currentIndex);
            }
        }
    }

    // ✅ Next Song
    public static void playNext(Context context) {
        if (currentIndex == -1) return;

        currentIndex++;

        if (currentIndex > 3) {
            currentIndex = 0;
        }

        play(context, currentIndex);
    }

    // ✅ Previous Song
    public static void playPrevious(Context context) {
        if (currentIndex == -1) return;

        currentIndex--;

        if (currentIndex < 0) {
            currentIndex = 3;
        }

        play(context, currentIndex);
    }

    // ✅ For Mini Player
    public static Song getCurrentSong() {
        return currentSong;
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    // 🔥 Map index → song file
    private static int getResIdFromIndex(int index) {

        switch (index) {
            case 0:
                return R.raw.night_drive;
            case 1:
                return R.raw.ocean_waves;
            case 2:
                return R.raw.city_lights;
            case 3:
                return R.raw.midnight;
            default:
                return -1;
        }
    }

    // 🔥 Map index → title
    private static String getTitleFromIndex(int index) {
        switch (index) {
            case 0:
                return "Night Drive";
            case 1:
                return "Ocean Waves";
            case 2:
                return "City Lights";
            case 3:
                return "Midnight";
            default:
                return "Unknown";
        }
    }
}