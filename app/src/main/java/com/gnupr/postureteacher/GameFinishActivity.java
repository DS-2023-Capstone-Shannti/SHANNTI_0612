package com.gnupr.postureteacher;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gnupr.postureteacher.HeroGameRanking;
import com.gnupr.postureteacher.MainActivity;
import com.gnupr.postureteacher.R;
import com.gnupr.postureteacher.UserAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class GameFinishActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer2;
    private ImageView star1;
    private ImageView star2;
    private ImageView star3;

    private FirebaseAuth mFirebaseAuth; // 파이어베이스 인증
    private DatabaseReference mDatabaseRef; // 실시간 데이터베이스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_game);

        // 배경음악을 위한 MediaPlayer 초기화
        mediaPlayer2 = MediaPlayer.create(this, R.raw.result); // 배경음악 리소스 파일 지정
        mediaPlayer2.setVolume(0.3f, 0.3f); // 볼륨 설정
        mediaPlayer2.setLooping(false); // 배경음악 반복 재생 설정
        mediaPlayer2.start(); // 배경음악 재생 시작

        star1= findViewById(R.id.star_gray1);
        star2= findViewById(R.id.star_gray2);
        star3= findViewById(R.id.star_gray3);
        TextView gameMessageTextView = findViewById(R.id.game_message);
        Button ranking = findViewById(R.id.Hero_ranking);

        Intent intent = getIntent();
        int score = intent.getIntExtra("SCORE", 0);
        ///////수정
        int score_ranking = intent.getIntExtra("SCORE_RANKING", 0);

        TextView scoreTextView = findViewById(R.id.scoreTextView);
        scoreTextView.setText("   " + score*10);

//        Intent calendarIntent = new Intent(this, Empty.class);
//        calendarIntent.putExtra("SCORE1", score);

        if (score*10 >= 80) {
            star1.setImageResource(R.drawable.star_yellow);
            star2.setImageResource(R.drawable.star_yellow);
            star3.setImageResource(R.drawable.star_yellow);
            gameMessageTextView.setText("티켓팅도 성공할 수준!");
        } else if (score*10 >= 60) {
            star1.setImageResource(R.drawable.star_yellow);
            star2.setImageResource(R.drawable.star_yellow);
            star3.setImageResource(R.drawable.star_gray);
            gameMessageTextView.setText("   잘했어요!   ");
        } else if (score*10 >= 20) {
            star1.setImageResource(R.drawable.star_yellow);
            star2.setImageResource(R.drawable.star_gray);
            star3.setImageResource(R.drawable.star_gray);
            gameMessageTextView.setText(" 좀 더 노력해봐요 ");
        } else {
            gameMessageTextView.setText("콘서트는 다음 생에..");
        }

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                backToMainActivity();
//                startActivity(calendarIntent);
//            }
//        }, 8000);


        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Shannti");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // 기존 점수가 있는 경우 업데이트
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("Hero_rank_score", score_ranking);
                    updateData.put("Hero_score", score);

                    mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).updateChildren(updateData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
//                                    Toast.makeText(GameFinishActivity.this, "점수가 성공적으로 업데이트되었습니다. 점수 : " + collisionCount, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(ShootingGameOver.this, "점수 업데이트에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // 기존 점수가 없는 경우 새로운 점수 저장
                    UserAccount account = new UserAccount();
                    account.setHero_score(score);
                    account.setHero_ranking_score(score_ranking);
                    mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
//                                    Toast.makeText(ShootingGameOver.this, "새로운 점수가 성공적으로 저장되었습니다. 점수 : " + collisionCount, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(ShootingGameOver.this, "점수 저장에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GameFinishActivity.this, "데이터베이스 오류: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        ranking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameFinishActivity.this, HeroGameRanking.class);
                startActivity(intent);
                finish();
            }
        });
    }



    // MainActivity로 돌아가는 메서드
    private void backToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // 현재 Activity를 종료합니다.
    }

    public void backClick(View view) {
        // 고급 게임 시작
        backToMainActivity();
    }
}