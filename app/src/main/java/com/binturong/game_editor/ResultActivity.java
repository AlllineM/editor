package com.binturong.game_editor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;

import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;

public class ResultActivity extends HeaderActivity implements View.OnClickListener {

    Button home;

    TextView resultTextView;
    TextView recordTextView;
    TextView ratingTextView;
    TextView bookLinkTextView;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sharedPreferences.edit();

        resultTextView = findViewById(R.id.result);
        recordTextView = findViewById(R.id.record);
        bookLinkTextView = findViewById(R.id.book_link);

        Button play = findViewById(R.id.play);
        AppCompatImageButton gift = findViewById(R.id.gift);
        AppCompatImageButton giftReady = findViewById(R.id.giftReady);
        AppCompatImageButton homeHead = findViewById(R.id.homeHead);
        AppCompatImageButton settingsHead = findViewById(R.id.settingsHead);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int score = extras.getInt("score");
            String resultText = "Результат: " + score;
            resultTextView.setText(resultText);
        }

        int maxScore = sharedPreferences.getInt("maxScore", 0);
        String recordText = "Личный рекорд: " + maxScore;
        recordTextView.setText(recordText);

        changeGiftVisibility();
        changeGiftReadyVisibility();

        play.setOnClickListener(this);
        homeHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });

        settingsHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(ResultActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        gift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpdateDialog(ResultActivity.this);
            }
        });

        giftReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExtraLiveDialog(ResultActivity.this);
            }
        });

        setAddBanner();
    }

    @Override
    public void onClick(View view) {
        Button clickedButton = (Button) view;
        if (clickedButton.getId() == R.id.play) {
            Intent playIntent = new Intent(this, QuizActivity.class);
            startActivity(playIntent);
        }
    }
}