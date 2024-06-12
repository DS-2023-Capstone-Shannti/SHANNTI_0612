package com.gnupr.postureteacher;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class GameselectActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameselect);

        // 배경음악을 위한 MediaPlayer 초기화
        mediaPlayer2 = MediaPlayer.create(this, R.raw.gamestart); // 배경음악 리소스 파일 지정
        mediaPlayer2.setVolume(0.3f, 0.3f); // 볼륨 설정
        mediaPlayer2.setLooping(false); // 배경음악 반복 재생 설정
        mediaPlayer2.start(); // 배경음악 재생 시작

        View homeButtonOverlay = findViewById(R.id.overlay_home_button);
        homeButtonOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameselectActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer2 != null) {
            mediaPlayer2.release();
            mediaPlayer2 = null;
        }
    }

    public void onmoleButtonClick(View view) {
        mediaPlayer2.release();
        Intent intent = new Intent(GameselectActivity.this, GameStartActivity.class);
        startActivity(intent);
    }

    public void onshootButtonClick(View view) {
        mediaPlayer2.release();
        Intent intent = new Intent(GameselectActivity.this, ShootingMainActivity.class);
        startActivity(intent);
    }
}
