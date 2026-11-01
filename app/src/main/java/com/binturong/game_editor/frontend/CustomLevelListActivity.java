package com.binturong.game_editor.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.binturong.game_editor.R;
import com.binturong.game_editor.backend.entity.Level;
import com.binturong.game_editor.backend.service.DatabaseClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomLevelListActivity extends HeaderActivity {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private CustomLevelAdapter adapter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_level_list);

        recyclerView = findViewById(R.id.level_list);
        emptyView = findViewById(R.id.empty_view);

        adapter = new CustomLevelAdapter(new CustomLevelAdapter.OnPlayClickListener() {
            @Override
            public void onPlay(Level level) {
                Intent playIntent = new Intent(CustomLevelListActivity.this, QuizActivity.class);
                playIntent.putExtra("mode", "custom");
                playIntent.putExtra("levelId", level.id);
                startActivity(playIntent);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        AppCompatImageButton gift = findViewById(R.id.gift);
        AppCompatImageButton giftReady = findViewById(R.id.giftReady);
        AppCompatImageButton homeHead = findViewById(R.id.homeHead);

        changeGiftVisibility();
        changeGiftReadyVisibility();

        homeHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CustomLevelListActivity.this, MainActivity.class));
            }
        });
        gift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpdateDialog(CustomLevelListActivity.this);
            }
        });
        giftReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExtraLiveDialog(CustomLevelListActivity.this);
            }
        });

        setAddBanner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // В onResume, т.к. MainActivity может ещё догружать уровни с бекенда
        loadLevels();
    }

    private void loadLevels() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Level> levels = DatabaseClient
                        .getInstance(CustomLevelListActivity.this)
                        .levelDao()
                        .getAll();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setLevels(levels);
                        emptyView.setVisibility(levels.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CustomLevelListActivity.this, MainActivity.class));
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}