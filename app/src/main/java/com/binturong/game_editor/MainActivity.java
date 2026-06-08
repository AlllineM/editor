package com.binturong.game_editor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button mainButton;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private MediaPlayer sound, catchSound;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sharedPreferences.edit();
        catchSound = MediaPlayer.create(this, R.raw.crunch);
        sound = MediaPlayer.create(this, R.raw.rain);

        mainButton = findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);

        sound.setVolume(0, 0);
        sound.start();
        for (int i = 0; i < 10; i++) {
            float coordinate = (float) (i * 0.1);
            sound.setVolume(coordinate, coordinate);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        sound.stop();
    }

    public void onClick(View view) {
        Log.d("result", "4");

        Button clickedButton = (Button) view;
        int clickedButtonId = clickedButton.getId();

        Log.d("result", "5");

        if (clickedButtonId == R.id.main_button) {
            Log.d("result", String.valueOf(clickedButtonId));

            catchSound.start();

            // Intent quizIntent = new Intent(this, QuizActivity.class);
          //  startActivity(quizIntent);
        }
        Log.d("result", "6");
    }

}