package com.binturong.game_editor.frontend;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatImageButton;

import com.binturong.game_editor.R;
import com.binturong.game_editor.backend.dao.LevelDao;
import com.binturong.game_editor.backend.entity.Level;
import com.binturong.game_editor.backend.service.AppDatabase;
import com.binturong.game_editor.backend.service.DatabaseClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends HeaderActivity implements View.OnClickListener {

    Button quiz1, play, levelCreate, customLevelList;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    AppDatabase db;
    LevelDao levelDao;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sharedPreferences.edit();

        db = DatabaseClient.getInstance(this);
        levelDao = db.levelDao();

        editor.putInt("customLevelCount", levelDao.getCount());
        editor.commit();

        setData();

        quiz1 = findViewById(R.id.quiz_1);
        play = findViewById(R.id.play);
        levelCreate = findViewById(R.id.level_create);
        customLevelList = findViewById(R.id.custom_level_list);

        quiz1.setOnClickListener(this);
        play.setOnClickListener(this);

        customLevelList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent customLevelListIntent = new Intent(MainActivity.this, CustomLevelListActivity.class);
                startActivity(customLevelListIntent);
            }
        });
        levelCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editorIntent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(editorIntent);
            }
        });

        AppCompatImageButton settingsHead = findViewById(R.id.settingsHead);
        AppCompatImageButton giftReady = findViewById(R.id.giftReady);
        settingsHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        giftReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExtraLiveDialog(MainActivity.this);
            }
        });

        //это пока убрала, тк при первом запуске активите он из кэша достает подарок,
        //даже если юзер уже обновился и больше не надо
        //changeGiftVisibility();
        AppCompatImageButton gift = findViewById(R.id.gift);
        gift.setVisibility(View.GONE);

        changeGiftReadyVisibility();

        setAddBanner();
    }

    public void onClick(View view) {
        Button clickedButton = (Button) view;

        Intent quizIntent = new Intent(this, QuizActivity.class);

        if (clickedButton.getId() == R.id.quiz_1) {
            quizIntent.putExtra("mode", "classic");
        } else if (clickedButton.getId() == R.id.quiz_1) {
            quizIntent.putExtra("mode", "level");
        }

        startActivity(quizIntent);
    }

    private void setData() {
        setUserToken();
        setLevels();

        if (levelDao.getCount() != sharedPreferences.getInt("customLevelCount", -1)) {
            setCustomLevels();
        }
    }
    private void setUserToken() {
        String userToken = sharedPreferences.getString("userToken", "");
        if (userToken.isEmpty()) {
            userToken = generateNewToken();
            editor.putString("userToken", userToken);
            editor.commit();
        }
    }

    private void setLevels() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //todo: Сильно нужен рефакторинг, но сейчас влом)
                String response = getLevels();
                JSONArray levels = parseResponse(response);
                if (levels.length() > 0) {
                    saveLevels(levels);
                }
            }
        });
        thread.start();
    }

    private void setCustomLevels() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //todo: Сильно нужен рефакторинг, но сейчас влом)
                String response = getCustomLevels();
                JSONArray levels = parseResponse(response);
                if (levels.length() > 0) {
                    saveCustomLevels(levels);
                }
            }
        });
        thread.start();
    }

    private void saveLevels(JSONArray levels) {
        try {
            for (int i=0; i < levels.length(); i++) {
                JSONObject level = levels.getJSONObject(i);
                editor.putString(
                        "level_" + level.getInt("id"),
                        level.getString("blocks")
                );
            }
            editor.commit();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveCustomLevels(JSONArray levels) {
        try {
            for (int i=0; i < levels.length(); i++) {
                JSONObject level = levels.getJSONObject(i);

                Level levelObj = new Level();
                levelObj.id = level.isNull("id") ? 0 : level.getInt("id");
                levelObj.blocks = level.isNull("blocks") ? null : level.getString("blocks");
                levelObj.startSpeed = level.isNull("startSpeed") ? 805 :  level.getInt("startSpeed");
                levelObj.author_name = level.isNull("user_name") ? "Аноним" : level.getString("user_name");
                levelObj.rating = level.isNull("rating") ? 0 : level.getInt("rating");
                levelObj.targetScore = level.isNull("targetScore") ? 12 : level.getInt("targetScore");

                levelDao.insert(levelObj);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONArray parseResponse(String response) {
        try {
            //{"status":200,"result":[{"id":1,"blocks":[{"x":1,"y":2}]}]}
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.getInt("status") != 200) {
                return new JSONArray();
            }
            return new JSONArray(jsonResponse.getString("result"));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String getLevels() {
        return sendGET("level/get/all-base");
    }

    private String getCustomLevels() {
        return sendGET("level/get/all-custom");
    }
}