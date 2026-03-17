package com.vibemusic.app;

import android.content.Context;
import android.content.Intent;

public class MusicPlayerManager {

    public static void play(Context context, int resId) {

        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY);
        intent.putExtra("resId", getResIdFromIndex(resId));

        context.startService(intent);
    }

    public static void pause(Context context) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(MusicService.ACTION_PAUSE);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(MusicService.ACTION_STOP);
        context.startService(intent);
    }

    // 🔥 Map index → actual song file
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
}