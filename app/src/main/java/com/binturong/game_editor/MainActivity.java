package com.binturong.game_editor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button mainButton;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private MediaPlayer sound, catchSound;
    private Timer timer;

    private int volumeK;


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

        volumeK = 0;
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

    private void soundStart() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                float volume = (float) (volumeK * 0.1);
                sound.setVolume(volume, volume);
                sound.start();
                volumeK = volumeK++;
            }
        }, 1000, 1000);
        sound.stop();
    }

}