package com.binturong.game_editor.frontend;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import androidx.appcompat.widget.AppCompatImageButton;

import com.binturong.game_editor.R;

public class GameDesignerActivity extends HeaderActivity implements View.OnClickListener {

    Button home;

    ToggleButton vibrationButton;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_designer);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sharedPreferences.edit();

        Button play = findViewById(R.id.play);
        AppCompatImageButton gift = findViewById(R.id.gift);
        AppCompatImageButton giftReady = findViewById(R.id.giftReady);
        AppCompatImageButton homeHead = findViewById(R.id.homeHead);

        changeGiftVisibility();
        changeGiftReadyVisibility();

        play.setOnClickListener(this);
        homeHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(GameDesignerActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });

        gift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpdateDialog(GameDesignerActivity.this);
            }
        });

        giftReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExtraLiveDialog(GameDesignerActivity.this);
            }
        });

        setAddBanner();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(GameDesignerActivity.this, MainActivity.class));
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

    public void getLevels() {
        sendGET("level/get/all-base");
    }
}