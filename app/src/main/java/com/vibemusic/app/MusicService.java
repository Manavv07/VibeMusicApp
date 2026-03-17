package com.vibemusic.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import android.support.v4.media.session.MediaSessionCompat;

public class MusicService extends Service {

    private static MediaPlayer mediaPlayer;

    public static final String ACTION_PLAY = "PLAY";
    public static final String ACTION_PAUSE = "PAUSE";
    public static final String ACTION_STOP = "STOP";
    public static final String ACTION_TOGGLE = "TOGGLE";
    public static final String ACTION_PREVIOUS = "PREVIOUS";
    public static final String ACTION_NEXT = "NEXT";

    private static final String CHANNEL_ID = "music_channel";
    private static final int NOTIFICATION_ID = 1;

    private static final int REQ_PREVIOUS = 1001;
    private static final int REQ_TOGGLE = 1002;
    private static final int REQ_NEXT = 1003;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private MediaSessionCompat mediaSession;
    private boolean isForegroundStarted = false;

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                updateNotification(false);
                if (mediaPlayer.isPlaying()) {
                    progressHandler.postDelayed(this, 1000);
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        mediaSession = new MediaSessionCompat(this, "VibeMusicSession");
        mediaSession.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (ACTION_PLAY.equals(action)) {
                int resId = intent.getIntExtra("resId", -1);
                playMusic(resId);
                updateNotification(true);
            } else if (ACTION_TOGGLE.equals(action)) {
                toggleMusic();
                updateNotification(false);
            } else if (ACTION_PAUSE.equals(action)) {
                pauseMusic();
                updateNotification(false);
            } else if (ACTION_PREVIOUS.equals(action)) {
                previousMusic();
                updateNotification(false);
            } else if (ACTION_NEXT.equals(action)) {
                nextMusic();
                updateNotification(false);
            } else if (ACTION_STOP.equals(action)) {
                stopMusic();
                stopForeground(true);
                isForegroundStarted = false;
                stopSelf();
            }
        }

        return START_STICKY;
    }

    private void playMusic(int resId) {
        if (resId == -1) return;

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, resId);
        if (mediaPlayer == null) return;

        mediaPlayer.setOnCompletionListener(mp -> {
            stopProgressUpdates();
            updateNotification(false);
        });

        mediaPlayer.start();
        startProgressUpdates();
    }

    private void toggleMusic() {
        if (mediaPlayer == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopProgressUpdates();
        } else {
            mediaPlayer.start(); // resume same MediaPlayer instance
            startProgressUpdates();
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopProgressUpdates();
        }
    }

    private void previousMusic() {
        if (mediaPlayer == null) return;
        mediaPlayer.seekTo(0);
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        startProgressUpdates();
    }

    private void nextMusic() {
        if (mediaPlayer == null) return;
        int nextPosition = mediaPlayer.getCurrentPosition() + 10_000;
        int duration = mediaPlayer.getDuration();
        if (duration > 0) {
            mediaPlayer.seekTo(Math.min(nextPosition, duration));
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        startProgressUpdates();
    }

    private void stopMusic() {
        stopProgressUpdates();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void startProgressUpdates() {
        progressHandler.removeCallbacks(progressRunnable);
        progressHandler.post(progressRunnable);
    }

    private void stopProgressUpdates() {
        progressHandler.removeCallbacks(progressRunnable);
    }

    private boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    private Notification buildNotification() {
        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction(ACTION_PREVIOUS);

        Intent toggleIntent = new Intent(this, MusicService.class);
        toggleIntent.setAction(ACTION_TOGGLE);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction(ACTION_NEXT);

        PendingIntent previousPendingIntent = PendingIntent.getService(
                this,
                REQ_PREVIOUS,
                previousIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent togglePendingIntent = PendingIntent.getService(
                this,
                REQ_TOGGLE,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent nextPendingIntent = PendingIntent.getService(
                this,
                REQ_NEXT,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        boolean playing = isPlaying();

        int duration = 0;
        int position = 0;
        if (mediaPlayer != null) {
            duration = Math.max(mediaPlayer.getDuration(), 0);
            position = Math.max(mediaPlayer.getCurrentPosition(), 0);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("VibeMusic")
                .setContentText(playing ? "Playing music..." : "Music paused")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, "Previous", previousPendingIntent)
                .addAction(
                        playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        playing ? "Pause" : "Play",
                        togglePendingIntent
                )
                .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2));

        if (duration > 0) {
            builder.setProgress(duration, position, false);
        } else {
            builder.setProgress(0, 0, false);
        }

        return builder.build();
    }

    private void updateNotification(boolean forceForeground) {
        Notification notification = buildNotification();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (!isForegroundStarted || forceForeground) {
            startForeground(NOTIFICATION_ID, notification);
            isForegroundStarted = true;
        } else if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Music Player",
                        NotificationManager.IMPORTANCE_LOW
                );
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        stopProgressUpdates();
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
            mediaSession = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
