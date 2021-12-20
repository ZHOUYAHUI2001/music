package com.bistu.cs.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bistu.cs.musicplayer.R;

import java.io.IOException;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    Context context;
    List<MusicBean> mDatas;
    OnItemClickLister onItemClickLister;

    public void setOnItemClickLister(OnItemClickLister onItemClickLister){
        this.onItemClickLister = onItemClickLister;
    }
    public interface OnItemClickLister{
        public void OnItemClick(View view,int position) throws IOException;
    }

    public MusicAdapter(Context context, List<MusicBean> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_local_music,parent,false);
        MusicViewHolder holder = new MusicViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MusicBean musicBean = mDatas.get(position);
        holder.idTv.setText(musicBean.getId());
        holder.songTv.setText(musicBean.getSong());
        holder.singerTv.setText(musicBean.getSinger());
        holder.albumTv.setText(musicBean.getAlbum());
        holder.timeTv.setText(musicBean.getDuration());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    onItemClickLister.OnItemClick(v,position);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class MusicViewHolder extends RecyclerView.ViewHolder{
        TextView idTv,songTv,singerTv,albumTv,timeTv;
        public MusicViewHolder (View itemView){
            super(itemView);
            idTv = itemView.findViewById(R.id.item_local_music_num);
            songTv = itemView.findViewById(R.id.item_local_music_song);
            singerTv = itemView.findViewById(R.id.item_local_music_singer);
            albumTv = itemView.findViewById(R.id.item_local_music_album);
            timeTv = itemView.findViewById(R.id.item_local_music_durtion);
        }
    }
}
