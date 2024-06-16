package com.binturong.game_editor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatImageButton;

public class MainActivity extends HeaderActivity implements View.OnClickListener {

    Button quiz1, play, levelCreate;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            quizIntent.putExtra("mode", "eternal");
        } else if (clickedButton.getId() == R.id.quiz_1) {
            quizIntent.putExtra("mode", "level");
        }

        startActivity(quizIntent);
    }
}