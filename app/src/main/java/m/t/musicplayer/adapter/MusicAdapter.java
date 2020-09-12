package m.t.musicplayer.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import m.t.musicplayer.R;
import m.t.musicplayer.model.Music;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private List<Music> musicList;
    private int playingMusicPosition = -1;
    private OnMusicListener listener;

    public MusicAdapter(List<Music> musicList, OnMusicListener listener) {
        this.musicList = musicList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MusicViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_music, parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        holder.bindMusic(musicList.get(position));
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView simpleDraweeView;
        private TextView artistTv;
        private TextView musicNameTv;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            simpleDraweeView = itemView.findViewById(R.id.iv_music);
            artistTv = itemView.findViewById(R.id.tv_music_artist);
            musicNameTv = itemView.findViewById(R.id.tv_music_name);
        }

        public void bindMusic(final Music music) {
            simpleDraweeView.setActualImageResource(music.getCoverResId());
            artistTv.setText(music.getArtist());
            musicNameTv.setText(music.getName());

            if (playingMusicPosition == getAdapterPosition()) {
                musicNameTv.setTextColor(Color.RED);
            } else {
                musicNameTv.setTextColor(Color.blue(1));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(music, getAdapterPosition());
                }
            });
        }
    }

    public void notifyMusicChange(Music music) {
        int index = musicList.indexOf(music);
        if (index != -1) {
            if (playingMusicPosition != index) {
                notifyItemChanged(playingMusicPosition);
                playingMusicPosition = index;
                notifyItemChanged(playingMusicPosition);
            }
        }
    }

    public interface OnMusicListener {
        void onClick(Music music, int position);
    }
}
