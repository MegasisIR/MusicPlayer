package m.t.musicplayer.model;

import android.content.Context;
import android.content.SharedPreferences;

public class PlayingMusic {
    private SharedPreferences sharedPreferences;

    public PlayingMusic(Context context) {
        this.sharedPreferences = context.getSharedPreferences("playing_music", Context.MODE_PRIVATE);
    }

    public void addNumberMusic(int musicPlayingNumber) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("music_number", musicPlayingNumber);
        editor.apply();
    }

    public int getCurrentMusicPlaying(){
        return sharedPreferences.getInt("music_number",-1);
    }
}
