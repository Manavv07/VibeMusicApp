package com.vibemusic.app;

import java.util.ArrayList;
import java.util.List;

public class SongRepository {

    public static List<Song> getSongs() {

        List<Song> songs = new ArrayList<>();

        songs.add(new Song("Night Drive", "Synthwave"));
        songs.add(new Song("Ocean Waves", "Chill"));
        songs.add(new Song("City Lights", "LoFi"));
        songs.add(new Song("Midnight Walk", "Ambient"));

        return songs;
    }
}