package com.binturong.game_editor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatImageButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends HeaderActivity implements View.OnClickListener {

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
    }

    public void onClick(View view) {
        Button clickedButton = (Button) view;
        int clickedButtonId = clickedButton.getId();

        if (clickedButtonId == R.id.main_button) {
            Log.d("result", String.valueOf(clickedButtonId));
        }

//        Intent quizIntent = new Intent(this, QuizActivity.class);
//        quizIntent.putExtra("mode", "classic");
//
//        startActivity(quizIntent);
    }

    private JSONArray parseResponse(String response) {
        try {
            //{"status":200,"result":[{"id":1,"blocks":[{"x":1,"y":2}]}]}
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.getInt("status") != 200) {
                return new JSONArray();
            }
            return new JSONArray(jsonResponse.getString("result"));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}