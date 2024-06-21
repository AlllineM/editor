package com.binturong.game_editor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatImageButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends HeaderActivity implements View.OnClickListener {

    Button quiz1, play, levelCreate;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sharedPreferences.edit();

        setLevels();

        quiz1 = findViewById(R.id.quiz_1);
        play = findViewById(R.id.play);
        levelCreate = findViewById(R.id.level_create);

        quiz1.setOnClickListener(this);
        play.setOnClickListener(this);
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

    private void setLevels() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String response = getLevels();
                Log.d("response1", response);
                JSONArray levels = parseResponse(response);
                if (levels.length() > 0) {
                    saveLevels(levels);
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
        StringBuilder result = new StringBuilder();
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL("https://test-constructor.ru/api/v.0/level/get/all-base");
            urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        return result.toString();
    }
}