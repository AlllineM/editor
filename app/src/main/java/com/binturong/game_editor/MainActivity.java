package com.binturong.game_editor;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button mainButton;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private MediaPlayer mainSoundPlayer, catchSoundPlayer, missSoundPlayer;
    private Timer timer;

    private int volumeK = 8, timerSpeed = 1200;

    private float buttonVolume = 0.1F;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sharedPreferences.edit();

        mainButton = findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);

      //  setSettings(); //этот метод обязательно первым
        setPlayers();
    }

    public void setSettings() {
        volumeK = 8;
        buttonVolume = 0.2F;
        timerSpeed = 1200;
    }

    public void setPlayers() {
        catchSoundPlayer = MediaPlayer.create(this, R.raw.crunch);
        missSoundPlayer = MediaPlayer.create(this, R.raw.flap);

        catchSoundPlayer.setVolume(buttonVolume, buttonVolume);
        missSoundPlayer.setVolume(buttonVolume, buttonVolume);

        mainSoundPlayer = new MediaPlayer();
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.rain);
        try {
            mainSoundPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mainSoundPlayer.setOnPreparedListener(this::onPrepared);
        mainSoundPlayer.prepareAsync();
    }

    public void onClick(View view) {
        Button clickedButton = (Button) view;
        int clickedButtonId = clickedButton.getId();

        if (clickedButtonId == R.id.main_button) {
            Log.d("result", String.valueOf(clickedButtonId));

            if (volumeK < 30) {
                missSoundPlayer.start();
            } else {
                Log.d("result", "gotcha");
                catchSoundPlayer.start();
                mainSoundPlayer.stop();
                timer.purge();
                timer.cancel();
            }

            // Intent quizIntent = new Intent(this, QuizActivity.class);
          //  startActivity(quizIntent);
        }
        Log.d("result", "6");
    }

    public void onPrepared(MediaPlayer player) {
        Log.d("result", "onPrepared");

        float volume = (float) (volumeK * 0.025);
        Log.d("volume", String.valueOf(volume));
        mainSoundPlayer.setVolume(volume, volume);
        mainSoundPlayer.start();
        changeVolume();
        Log.d("result", "onPrepared");
    }

    private void changeVolume() {
        Log.d("result", "7");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("volumeK", String.valueOf(volumeK));

                float volume = (float) (volumeK * 0.025);

                Log.d("volume", String.valueOf(volume));
                mainSoundPlayer.setVolume(volume, volume);
                volumeK = volumeK + 1;

                if (volumeK > 39) {
                    Log.d("volumeK", String.valueOf(volumeK));
                    timer.purge();
                    timer.cancel();
                    mainSoundPlayer.stop();
                }
                Log.d("result", "11");
            }
        }, timerSpeed, timerSpeed);
    }
}