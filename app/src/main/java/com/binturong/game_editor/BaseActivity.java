package com.binturong.game_editor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BaseActivity extends AppCompatActivity {

    public final String baseUrl = "https://test-constructor.ru/api/v.0/";
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }

    public void setAddBanner() {
        BannerAdView banner =  findViewById(R.id.banner);
        banner.setAdUnitId("R-M-9795843-1");
        banner.setAdSize(BannerAdSize.fixedSize(this, 320, 50));
        AdRequest adRequest = new AdRequest.Builder().build();
        banner.loadAd(adRequest);
    }

    public String sendGET(String urlString) {
        StringBuilder result = new StringBuilder();
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(baseUrl + urlString);
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

    public String sendPOST(String urlString, String jsonInputString) {
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL (baseUrl + urlString);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            Log.d("log", "log1");

            try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            Log.d("log", "log2");

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                Log.d("resp", response.toString());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("log", "log3");

            con.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }
}