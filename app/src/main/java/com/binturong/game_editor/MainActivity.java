package com.binturong.game_editor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sharedPreferences.edit();

        mainButton = findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);
    }

    public void onClick(View view) {
        Log.d("result", "4");

        Button clickedButton = (Button) view;
        int clickedButtonId = clickedButton.getId();

        Log.d("result", "5");

        if (clickedButtonId == R.id.main_button) {
            Log.d("result", String.valueOf(clickedButtonId));



           // Intent quizIntent = new Intent(this, QuizActivity.class);
          //  startActivity(quizIntent);
        }
        Log.d("result", "6");
    }

}