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

import java.util.ArrayList;
import java.util.List;

public class CustomLevelAdapter extends RecyclerView.Adapter<CustomLevelAdapter.ViewHolder> {

    public interface OnPlayClickListener {
        void onPlay(Level level);
    }

    private final List<Level> levels = new ArrayList<>();
    private final OnPlayClickListener listener;

    public CustomLevelAdapter(OnPlayClickListener listener) {
        this.listener = listener;
    }

    public void setLevels(List<Level> newLevels) {
        levels.clear();
        levels.addAll(newLevels);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_custom_level, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Level level = levels.get(position);

        holder.levelId.setText("Уровень #" + level.id);

        // difficulty - примитивный int, поэтому "не указана" = 0
        if (level.difficulty > 0) {
            holder.difficulty.setVisibility(View.VISIBLE);
            holder.difficulty.setText("Сложность: " + level.difficulty);
        } else {
            holder.difficulty.setVisibility(View.GONE);
        }

        String author = (level.author_name == null || level.author_name.trim().isEmpty())
                ? "Аноним" : level.author_name;
        holder.author.setText("Автор: " + author);

        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPlay(level);
            }
        });
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView levelId, difficulty, author;
        final Button play;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            levelId = itemView.findViewById(R.id.level_id);
            difficulty = itemView.findViewById(R.id.level_difficulty);
            author = itemView.findViewById(R.id.level_author);
            play = itemView.findViewById(R.id.level_play);
        }
    }
}