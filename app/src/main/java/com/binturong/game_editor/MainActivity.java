package com.binturong.game_editor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
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

        mainButton = findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);

        volumeK = 0;
        catchSound = MediaPlayer.create(this, R.raw.crunch);

        sound = new MediaPlayer();
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.rain);
        try {
            sound.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sound.setOnPreparedListener(this::onPrepared);
        sound.prepareAsync();
    }

    public void onClick(View view) {
        Log.d("result", "4");

        Button clickedButton = (Button) view;
        int clickedButtonId = clickedButton.getId();

        Log.d("result", "5");

        if (clickedButtonId == R.id.main_button) {
            Log.d("result", String.valueOf(clickedButtonId));

            catchSound.start();

            if (volumeK > 10) {
                Log.d("result", "gotcha");
            }

            // Intent quizIntent = new Intent(this, QuizActivity.class);
          //  startActivity(quizIntent);
        }
        Log.d("result", "6");
    }

    public void onPrepared(MediaPlayer player) {
        soundStart();
    }

    private void soundStart() {
        Log.d("result", "7");
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("result", "8");
                float volume = (float) (volumeK * 0.05);
                sound.setVolume(volume, volume);
                sound.start();
                volumeK = volumeK + 1;
                Log.d("result", "9");

                if (volumeK > 19) {
                    Log.d("result", "10");
                    timer.purge();
                    timer.cancel();
                    soundStart();
                }
                Log.d("result", "11");
            }
        }, 5000, 5000);
        sound.stop();
    }
}