package com.gnupr.postureteacher;

import android.content.Intent;
import android.media.MediaPlayer;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class GameStartActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer2;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        // 배경음악을 위한 MediaPlayer 초기화
        mediaPlayer2 = MediaPlayer.create(this, R.raw.gamestart); // 배경음악 리소스 파일 지정
        mediaPlayer2.setVolume(0.3f, 0.3f); // 볼륨 설정
        mediaPlayer2.setLooping(false); // 배경음악 반복 재생 설정
        mediaPlayer2.start(); // 배경음악 재생 시작

        View homeButtonOverlay = findViewById(R.id.overlay_home_button);
        homeButtonOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer2.release();
                Intent intent = new Intent(GameStartActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }


    public void onEasyButtonClick(View view) {
        mediaPlayer2.release();
        // 하급 게임 시작
        Intent intent = new Intent(this, GamePlayActivity.class);
        intent.putExtra("difficulty", "easy");
        startActivity(intent);
    }

    public void onMediumButtonClick(View view) {
        mediaPlayer2.release();
        // 중급 게임 시작
        Intent intent = new Intent(this, GamePlayActivity2.class);
        intent.putExtra("difficulty", "medium");
        startActivity(intent);
    }

    public void onHardButtonClick(View view) {
        mediaPlayer2.release();
        // 고급 게임 시작
        Intent intent = new Intent(this, GamePlayActivity3.class);
        intent.putExtra("difficulty", "hard");
        startActivity(intent);
    }



}
