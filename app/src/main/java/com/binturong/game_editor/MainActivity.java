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

    private int count, volumeK,timerSpeed, maxStep, catchStep, catchDistance, startCatchStep, endCatchStep;

    private float buttonVolume, stepSize;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sharedPreferences.edit();

        mainButton = findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);

        setSettings(); //этот метод обязательно вызвать первым
        setPlayers();
    }

    public void setSettings() {
        count = 0;
        volumeK = 0;
        timerSpeed = 500;
        catchStep = 30;
        catchDistance = 1;
        buttonVolume = 0.05F;

        maxStep = catchStep * 2;
        stepSize = (float) 1 / catchStep;
        Log.d("stepSize", String.valueOf(stepSize));

        startCatchStep = catchStep - catchDistance;
        endCatchStep = catchStep + catchDistance;
    }

    public void setPlayers() {
        catchSoundPlayer = MediaPlayer.create(this, R.raw.crunch);
        missSoundPlayer = MediaPlayer.create(this, R.raw.flap);

        catchSoundPlayer.setVolume(buttonVolume, buttonVolume);
        missSoundPlayer.setVolume(buttonVolume, buttonVolume);

        mainSoundPlayer = new MediaPlayer();
        mainSoundPlayer.setOnPreparedListener(this::onPrepared);

        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.rain);
        try {
            mainSoundPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mainSoundPlayer.prepareAsync();
    }

    public void onClick(View view) {
        Button clickedButton = (Button) view;
        int clickedButtonId = clickedButton.getId();

        if (clickedButtonId == R.id.main_button) {
            Log.d("result", String.valueOf(clickedButtonId));

            if (count > maxStep) return;

            if (count < startCatchStep || count > endCatchStep) { //если не долетел или перелетел)
                missSoundPlayer.start();
            } else {
                //если поймал
                Log.d("result", "gotcha");
                catchSoundPlayer.start();
                restartMainSound();
            }
        }
    }

    public void onPrepared(MediaPlayer player) {
        Log.d("result", "onPrepared");
        startMainSound();
        Log.d("result", "onPrepared");
    }

    public void startMainSound() {
        count = 0;
        volumeK = 0;
        mainSoundPlayer.setVolume(0, 0);
        mainSoundPlayer.start();
        changeVolume();
    }

    public void restartMainSound() {
        timer.purge();
        timer.cancel();

        mainSoundPlayer.stop();
        mainSoundPlayer.reset();

        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.rain);
        try {
            mainSoundPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mainSoundPlayer.prepareAsync();
    }

    private void changeVolume() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("count", String.valueOf(count));
                Log.d("volumeK", String.valueOf(volumeK));

                if (count >= maxStep) {
                    restartMainSound();
                } else {
                    if (count < catchStep) {
                        volumeK = volumeK + 1;
                    } else {
                        volumeK = volumeK - 1;
                    }
                    float volume = volumeK * stepSize;
                    Log.d("volume", String.valueOf(volume));
                    mainSoundPlayer.setVolume(volume, volume);
                }
                count = count + 1;
            }
        }, timerSpeed, timerSpeed);
    }
}