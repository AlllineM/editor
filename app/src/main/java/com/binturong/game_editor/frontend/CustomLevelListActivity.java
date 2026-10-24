package com.binturong.game_editor.frontend;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import androidx.appcompat.widget.AppCompatImageButton;

import com.binturong.game_editor.R;

public class CustomLevelListActivity extends HeaderActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    Button home;

    ToggleButton vibrationButton;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sharedPreferences.edit();

        Button play = findViewById(R.id.play);
        Button gameDesigner = findViewById(R.id.game_designer);
        AppCompatImageButton gift = findViewById(R.id.gift);
        AppCompatImageButton giftReady = findViewById(R.id.giftReady);
        AppCompatImageButton homeHead = findViewById(R.id.homeHead);

        vibrationButton = findViewById(R.id.vibrationButton);
        vibrationButton.setOnCheckedChangeListener(this);

        boolean isVibrateOn = sharedPreferences.getBoolean("vibration", true);
        vibrationButton.setChecked(isVibrateOn);

        changeGiftVisibility();
        changeGiftReadyVisibility();

        play.setOnClickListener(this);

        gameDesigner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gameDesignerIntent = new Intent(CustomLevelListActivity.this, GameDesignerActivity.class);
                startActivity(gameDesignerIntent);
            }
        });
        homeHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(CustomLevelListActivity.this, MainActivity.class);
                startActivity(mainIntent);
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
    public void onBackPressed() {
        startActivity(new Intent(CustomLevelListActivity.this, MainActivity.class));
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        Button clickedButton = (Button) view;
        if (clickedButton.getId() == R.id.play) {
            Intent playIntent = new Intent(this, QuizActivity.class);
            startActivity(playIntent);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        editor.putBoolean("vibration", isChecked);
        editor.commit();
    }
}