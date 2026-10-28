package com.binturong.game_editor.frontend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.binturong.game_editor.R;
import com.binturong.game_editor.backend.entity.Level;
import java.util.List;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {

    private List<Level> levelList;
    private OnPlayClickListener listener;

    public interface OnPlayClickListener {
        void onPlayClick(Level level);
    }

    public LevelAdapter(List<Level> levelList, OnPlayClickListener listener) {
        this.levelList = levelList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        Level level = levelList.get(position);

        holder.tvLevelId.setText("ID: " + level.id);
        holder.tvAuthorName.setText("Автор: " + (level.author_name != null ? level.author_name : "Неизвестен"));

        holder.btnPlay.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayClick(level);
            }
        });
    }

    @Override
    public int getItemCount() {
        return levelList != null ? levelList.size() : 0;
    }

    public static class LevelViewHolder extends RecyclerView.ViewHolder {
        TextView tvLevelId;
        TextView tvAuthorName;
        Button btnPlay;

        public LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLevelId = itemView.findViewById(R.id.tvLevelId);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            btnPlay = itemView.findViewById(R.id.btnPlay);
        }
    }
}
