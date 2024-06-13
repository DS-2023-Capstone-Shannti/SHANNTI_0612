package com.gnupr.postureteacher;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class ShootingGameOver extends AppCompatActivity {
    TextView tvPoints;
    Button shootRanking;

    private int collisionCount;

    private FirebaseAuth mFirebaseAuth; // 파이어베이스 인증
    private DatabaseReference mDatabaseRef; // 실시간 데이터베이스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shooting_game_over);

        String points = getIntent().getStringExtra("SCORE");
        shootRanking = findViewById(R.id.shootRanking);
        tvPoints = findViewById(R.id.tvPoints);

        View overlayAgain = findViewById(R.id.overlay_again);
        overlayAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShootingGameOver.this, ShootingMainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        View overlayExit = findViewById(R.id.overlay_exit);
        overlayExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShootingGameOver.this, GameselectActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Intent intent = getIntent();
        collisionCount = intent.getIntExtra("collisionCount", 0); // Default to 0 if not found

        // Use the collisionCount value as needed in your activity
        tvPoints.setText(String.valueOf(collisionCount));

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Shannti");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // 기존 점수가 있는 경우 업데이트
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("Shot_score", collisionCount);

                    mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).updateChildren(updateData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ShootingGameOver.this, "점수가 성공적으로 업데이트되었습니다. 점수 : " + collisionCount, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ShootingGameOver.this, "점수 업데이트에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // 기존 점수가 없는 경우 새로운 점수 저장
                    UserAccount account = new UserAccount();
                    account.setShot_score(collisionCount);

                    mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ShootingGameOver.this, "새로운 점수가 성공적으로 저장되었습니다. 점수 : " + collisionCount, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ShootingGameOver.this, "점수 저장에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShootingGameOver.this, "데이터베이스 오류: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        shootRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShootingGameOver.this, ShootingGameRanking.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // 뒤로가기 버튼이 눌렸을 때 GameStartActivity로 이동
        Intent intent = new Intent(ShootingGameOver.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private int convertStringToInt(String scoreString) {
        try {
            return Integer.parseInt(scoreString);
        } catch (NumberFormatException e) {
            return 0; // null이거나 변환 불가능할 경우 0으로 반환
        }
    }
}
