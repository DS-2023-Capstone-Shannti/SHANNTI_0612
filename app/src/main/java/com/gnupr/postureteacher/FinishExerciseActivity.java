package com.gnupr.postureteacher;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class FinishExerciseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_exercise);

        // 5초 후에 MainActivity로 이동하는 코드
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(intent);
//                finish(); // ExercisePage 종료
//            }
//        }, 5000); // 5초 지연

        ///////////////////////////////////////////////////////////
        View homeOverlay = findViewById(R.id.overlay_home);
        homeOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FinishExerciseActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Exercise 페이지로 이동하는 코드
        View exerciseOverlay = findViewById(R.id.overlay_exercise);
        exerciseOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FinishExerciseActivity.this, ShowExerciseActivity.class);
                startActivity(intent);
            }
        });

        // FinishGameActivity로 이동하는 코드
        View gameOverlay = findViewById(R.id.overlay_game);
        gameOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FinishExerciseActivity.this, GameStartActivity.class);
                startActivity(intent);
            }
        });

        View chartOverlay = findViewById(R.id.overlay_chart);
        chartOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, CircleProgressBar.class);
                Intent intent = new Intent(FinishExerciseActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });
        /////////////////////////////////////////////////////////////////////////////////////

}
    private void backToMainActivity () {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // 현재 Activity를 종료합니다.
    }

    public void backClick (View view){
        // 고급 게임 시작
        backToMainActivity();
    }
}

