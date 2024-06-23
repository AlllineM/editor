package com.binturong.game_editor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class HeaderActivity extends BaseActivity {

    TextView resultTextView;
    TextView recordTextView;
    TextView ratingTextView;
    TextView bookLinkTextView;

    private AppCompatImageButton home, settings, gift;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Boolean isNeedUpdate = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sharedPreferences.edit();

        home = findViewById(R.id.homeHead);
        settings = findViewById(R.id.settingsHead);
        gift = findViewById(R.id.gift);

        checkIfNeedUpdate();
    }

    public void showUpdateDialog(Activity context) {
        //Диалог для обновление игры
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Обнови игру и получили подарок - 15 минут игры с 4мя жизнями");
        builder.setTitle("Подарок");
        builder.setCancelable(true);
        builder.setPositiveButton("ОБНОВИТЬ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //ссылка на рустор
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://www.rustore.ru/catalog/app/com.binturong.game_editor"));
                startActivity(intent);
            }
        });
        builder.setPositiveButtonIcon(getDrawable(R.drawable.gift));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.show();
            }
        });
    }

    public void showExtraLiveDialog(Activity context) {
        //Диалог для обновление игры
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //счетчик, сколько осталось, лол)
        long nowTimestamp = System.currentTimeMillis();
        long deadLine = sharedPreferences.getLong("giftDeadLine", 0);

        String left;
        if (deadLine > nowTimestamp) {
            long timeLeft = deadLine - nowTimestamp;
            int timeLeftInSec = (int) (timeLeft / 1000);
            int min = timeLeftInSec / 60;
            int sec = timeLeftInSec % 60;
            left = min + " мин " + sec + " сек";
        } else {
            left = "0 мин 0 сек";
        }

        builder.setMessage("Дополнительная жизнь активирована. Осталось " + left);
        builder.setTitle("Подарок");
        builder.setCancelable(true);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.show();
            }
        });
    }


    public void checkIfNeedUpdate() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String response = getLastVersion();
                String lastVersion = parseResponse(response);

                boolean localIsNeedUpdate = sharedPreferences.getBoolean("isNeedUpdate", false);
                isNeedUpdate = isNeedUpdate(lastVersion);

                //Если чел обновился(раньше апдейт был нужен, а теперь нет)
                if (localIsNeedUpdate && !isNeedUpdate) {
                    //выдаем жизнь
                    editor.putBoolean("isExtraLiveAvailable", true);
                    long timestampLong = System.currentTimeMillis();

                    long deadLine = timestampLong + 1000 * 60 * 15;
                    editor.putLong("giftDeadLine", deadLine);
                }

                editor.putBoolean("isNeedUpdate", isNeedUpdate);
                editor.commit();
            }

            private String getLastVersion() {
                StringBuilder result = new StringBuilder();
                HttpURLConnection urlConnection = null;

                try {
                    URL url = new URL("https://test-constructor.ru/api/v.0/version/get");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }

                return result.toString();
            }

            private String parseResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getInt("status") != 200) {
                        return "0";
                    }
                    JSONObject result = new JSONObject(jsonResponse.getString("result"));

                    return result.getString("currentVersion");

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            /**
             * Сравнивает с захардкоденной текущей версией
             * @param lastVersion
             * @return
             */
            private boolean isNeedUpdate(String lastVersion) {
                return !Objects.equals(lastVersion, "0.1");
            }
        });
        thread.start();
    }

    //todo: эту штуку можно еще и объединить
    public void changeGiftVisibility () {
        AppCompatImageButton gift = findViewById(R.id.gift);
        if (sharedPreferences.getBoolean("isNeedUpdate", false)) {
            gift.setVisibility(View.VISIBLE);
        } else {
            gift.setVisibility(View.GONE);
        }
    }

    public void changeGiftReadyVisibility () {
        AppCompatImageButton giftReady = findViewById(R.id.giftReady);

        if (isExtraLiveAvailable()) {
            giftReady.setVisibility(View.VISIBLE);
        } else {
            giftReady.setVisibility(View.GONE);
        }
    }

    //Смотрит на сохраненный таймстамп - когда срок подарка выйдет
    public boolean isExtraLiveAvailable() {
        long deadLine = sharedPreferences.getLong("giftDeadLine", 0);

        return System.currentTimeMillis() < deadLine;
    }
}