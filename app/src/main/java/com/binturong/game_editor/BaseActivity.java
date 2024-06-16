package com.binturong.game_editor;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;

public class BaseActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }

    public void setAddBanner() {
        BannerAdView banner =  findViewById(R.id.banner);
        banner.setAdUnitId("R-M-9403186-1");
        banner.setAdSize(BannerAdSize.fixedSize(this, 320, 50));
        AdRequest adRequest = new AdRequest.Builder().build();
        banner.loadAd(adRequest);
    }
}