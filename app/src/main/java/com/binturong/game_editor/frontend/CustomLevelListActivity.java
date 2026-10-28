package com.binturong.game_editor.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.binturong.game_editor.R;
import com.binturong.game_editor.backend.dao.LevelDao;
import com.binturong.game_editor.backend.service.AppDatabase;
import com.binturong.game_editor.backend.entity.Level;
import com.binturong.game_editor.backend.service.DatabaseClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomLevelListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LevelAdapter adapter;
    private AppDatabase db;
    LevelDao levelDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_level_list); // Убедитесь, что в этой разметке есть RecyclerView с id recyclerView

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 1. Инициализируем адаптер ПУСТЫМ списком сразу, чтобы не было ошибки "No adapter attached"
        adapter = new LevelAdapter(new ArrayList<>(), level -> {
            Toast.makeText(this, "Запуск уровня ID: " + level.id, Toast.LENGTH_SHORT).show();
            // Ваша логика старта игры...
        });

        // 2. Сразу привязываем адаптер к RecyclerView
        recyclerView.setAdapter(adapter);

        db = DatabaseClient.getInstance(this);
        levelDao = db.levelDao();

        loadLevels();
    }

    private void loadLevels() {
        // Запрос к БД в фоновом потоке
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Level> levels = db.levelDao().getAll();

            // Возвращаемся в главный поток для обновления UI
            runOnUiThread(() -> {
                if (levels != null) {
                    adapter.setLevels(levels);
                }
                adapter = new LevelAdapter(levels, level -> {
                    // Обработка нажатия "Играть"
                    Toast.makeText(this, "Запуск уровня ID: " + level.id, Toast.LENGTH_SHORT).show();

                    // Пример перехода на активити с игрой и передача данных
                    // Intent intent = new Intent(CustomLevelListActivity.this, GameActivity.class);
                    // intent.putExtra("LEVEL_ID", level.id);
                    // intent.putExtra("BLOCKS_DATA", level.blocks);
                    // startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            });
        });
    }
}
