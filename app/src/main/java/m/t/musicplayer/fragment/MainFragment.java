package m.t.musicplayer.fragment;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.Slider;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import m.t.musicplayer.CreateChannelForNotify;
import m.t.musicplayer.PlayBack;
import m.t.musicplayer.R;
import m.t.musicplayer.adapter.MusicAdapter;
import m.t.musicplayer.databinding.FragmentMainBinding;
import m.t.musicplayer.model.Music;
import m.t.musicplayer.model.MusicState;
import m.t.musicplayer.model.PlayingMusic;
import m.t.musicplayer.service.NotificationReceiver;
import m.t.musicplayer.service.OnClearFormRecentService;


public class MainFragment extends Fragment implements MusicAdapter.OnMusicListener, PlayBack {
    private static FragmentMainBinding binding;
    private Context context;
    private PlayingMusic playingMusic;
    private MusicAdapter adapter;
    private List<Music> musicList = Music.getList();
    private static MediaPlayer mediaPlayer;
    private int cursor;
    private Timer timer;
    private static MusicState musicState = MusicState.STOP;
    private boolean isDragging;
    private NotificationManagerCompat notificationManager;
    private MediaSessionCompat mediaSession;
    private BroadcastReceiver broadcastReceiver;
    private int PLAY_PAUSE;

    @Override
    public void onClick(Music music, int position) {
        timer.cancel();
        timer.purge();
        mediaPlayer.release();
        cursor = position;
        onMusicChange(musicList.get(cursor));
    }


    public MainFragment(Context context) {
        this.context = context;
        adapter = new MusicAdapter(musicList, this);
        playingMusic = new PlayingMusic(context.getApplicationContext());
        notificationManager = NotificationManagerCompat.from(context);
        mediaSession = new MediaSessionCompat(context, "tag");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvMain.setAdapter(adapter);

        // broadCastForNotify
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getExtras().getString("action");
                switch (action) {
                    case "next":
                        onMusicNext();
                        break;
                    case "prev":
                        onMusicPrev();
                        break;
                    case "play":
                        if (musicState == MusicState.PLAYING) {
                            PLAY_PAUSE = R.drawable.ic_play_32dp;
                            onMusicPause();
                        } else {
                            PLAY_PAUSE = R.drawable.ic_pause;
                            onMusicPlay();
                        }
                        break;
                }
            }
        };


        binding.preBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.preBtn.animate().scaleXBy(10).setDuration(500);
                prevMusic();
                binding.preBtn.animate().scaleXBy(0f).setDuration(500);
            }
        });

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.nextBtn.animate().scaleXBy(10).setDuration(500);
                nextMusic();
                binding.nextBtn.animate().scaleXBy(0f).setDuration(500);
            }
        });

        binding.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicState == MusicState.PLAYING) {
                    binding.equalizer.stopBars();
                    mediaPlayer.pause();

                    binding.playBtn.animate().rotation(360).setDuration(800);
                    PLAY_PAUSE = R.drawable.ic_play_32dp;
                    binding.playBtn.setImageResource(PLAY_PAUSE);
                    musicState = MusicState.PAUSE;
                } else {
                    binding.playBtn.animate().rotation(180).setDuration(1000);
                    PLAY_PAUSE = R.drawable.ic_pause;
                    binding.playBtn.setImageResource(PLAY_PAUSE);
                    mediaPlayer.start();
                    binding.equalizer.animateBars();
                    musicState = MusicState.PLAYING;
                }
            }
        });

        binding.trackSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.trackDuration.setText(Music.convertMillisToString((long) value));

            }
        });
        if (playingMusic.getCurrentMusicPlaying() != -1) {
            this.cursor = playingMusic.getCurrentMusicPlaying();
        }

        onMusicChange(musicList.get(cursor));
    }

    private void onMusicChange(final Music music) {
        //***********************************Notification***********************************************
        PLAY_PAUSE = R.drawable.ic_pause;
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), music.getCoverResId());


        Intent intentPrevious = new Intent(context, NotificationReceiver.class).setAction("prev");
        PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(context,
                0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlay = new Intent(context, NotificationReceiver.class)
                .setAction("play");
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context,
                0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent intentNext = new Intent(context, NotificationReceiver.class)
                .setAction("next");
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context,
                0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notification = new NotificationCompat.Builder(context,
                CreateChannelForNotify.CHANNEL_ID)
                .setSmallIcon(PLAY_PAUSE)
                .setLargeIcon(icon)
                .setContentTitle("Player")
                .setContentText(music.getArtist())
                .setShowWhen(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_prev, "prev", pendingIntentPrevious)
                .addAction(PLAY_PAUSE, "pause", pendingIntentPlay)
                .addAction(R.drawable.ic_next, "next", pendingIntentNext)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSession.getSessionToken())
                )
                .setSubText(music.getName())
                .build();
        notificationManager.notify(1, notification);

        context.registerReceiver(broadcastReceiver, new IntentFilter("musics"));
        context.startService(new Intent(context.getApplicationContext(), OnClearFormRecentService.class));
        //**********************************************************************************
        playingMusic.addNumberMusic(cursor);
        adapter.notifyMusicChange(music);
        binding.trackSlider.setValue(0f);
        mediaPlayer = MediaPlayer.create(context, music.getMusicFileResId());
        binding.coverArt.setActualImageResource(music.getCoverResId());
        binding.ivMainArtist.setActualImageResource(music.getArtistResId());
        binding.nameTrackTv.setText(music.getName());
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
                binding.equalizer.animateBars();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isDragging) {
                                    binding.trackSlider.setValue(mediaPlayer.getCurrentPosition());
                                }
                            }
                        });
                    }
                }, 1000, 1000);

                binding.duration.setText(Music.convertMillisToString(mediaPlayer.getDuration()));
                binding.trackDuration.setText(Music.convertMillisToString(mediaPlayer.getCurrentPosition()));
                musicState = MusicState.PLAYING;
                binding.playBtn.setImageResource(R.drawable.ic_pause);
                binding.trackSlider.setValueTo(mediaPlayer.getDuration());

            }
        });

        binding.trackSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                isDragging = true;
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                isDragging = false;
                mediaPlayer.seekTo((int) slider.getValue());
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextMusic();
            }
        });
    }

    private void nextMusic() {
        timer.cancel();
        timer.purge();
        mediaPlayer.release();
        if (musicList.size() - 1 == cursor)
            cursor = 0;
        else cursor++;

        onMusicChange(musicList.get(cursor));
    }

    private void prevMusic() {
        timer.cancel();
        timer.purge();
        mediaPlayer.release();
        if (cursor == 0) cursor = musicList.size() - 1;
        else cursor--;
        onMusicChange(musicList.get(cursor));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        timer.cancel();
        mediaPlayer.release();
        mediaPlayer = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.cancelAll();
        }

        context.unregisterReceiver(broadcastReceiver);
    }


    @Override
    public void onMusicPrev() {
        prevMusic();
    }

    @Override
    public void onMusicNext() {
        nextMusic();
    }

    @Override
    public void onMusicPlay() {
        binding.playBtn.animate().rotation(180).setDuration(1000);
        PLAY_PAUSE = R.drawable.ic_pause;
        binding.playBtn.setImageResource(PLAY_PAUSE);
        mediaPlayer.start();
        binding.equalizer.animateBars();
        musicState = MusicState.PLAYING;
    }

    @Override
    public void onMusicPause() {
        binding.equalizer.stopBars();
        mediaPlayer.pause();

        binding.playBtn.animate().rotation(360).setDuration(800);
        PLAY_PAUSE = R.drawable.ic_play_32dp;
        binding.playBtn.setImageResource(PLAY_PAUSE);
        musicState = MusicState.PAUSE;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Toast.makeText(context, "OnActivityCreated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        Toast.makeText(context, "OnStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(context, "onResume", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        Toast.makeText(context, "OnPause", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Toast.makeText(context, "OnDestroyView", Toast.LENGTH_SHORT).show();
    }

}


