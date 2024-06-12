package com.gnupr.postureteacher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gnupr.postureteacher.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private ActivityMainBinding binding;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Shannti");

        binding.overlayHome.setOnClickListener(v -> navigateToActivity(GameSquat.class));
        binding.overlayExercise.setOnClickListener(v -> navigateToActivity(ShowExerciseActivity.class));
        binding.overlayGame.setOnClickListener(v -> navigateToActivity(GameselectActivity.class));
        binding.overlayChart.setOnClickListener(v -> navigateToActivity(CalendarActivity.class));
        binding.overlayRank.setOnClickListener(v -> navigateToActivity(HeroGameRanking.class));
        binding.overlayLogout.setOnClickListener(v -> logout());

        Intent mainIntent = getIntent();
        int score = mainIntent.getIntExtra("SCORE", 0);
        int totalExerciseTime = mainIntent.getIntExtra("TOTAL_EXERCISE_TIME", 0);
        int totalExerciseKcal = mainIntent.getIntExtra("TOTAL_EXERCISE_kcal", 0);

        binding.mainScore.setText(String.valueOf(score));
        binding.mainScore2.setText(String.valueOf(score));
        binding.mainTime.setText(String.valueOf(totalExerciseTime));
        binding.mainKcal.setText(String.valueOf(totalExerciseKcal));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        binding.todayDate.setText(currentDate);

        String userId = mFirebaseAuth.getCurrentUser().getUid();
        mDatabaseRef.child("UserAccount").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserAccount userAccount = snapshot.getValue(UserAccount.class);
                    if (userAccount != null) {
                        String userName = userAccount.getUserName();
                        int shotScore = userAccount.getShot_score();
                        int heroScore = userAccount.getHero_score();
                        binding.userName.setText(userName + "님");

                        binding.mainScore.setText(shotScore == 0 ? "0" : String.valueOf(shotScore));
                        binding.mainScore2.setText(heroScore == 0 ? "0" : String.valueOf(heroScore));
                    }

                    // 현재 날짜 가져오기
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                    String currentDate = sdf.format(new Date());

                    // 날짜 경로 하위로 Time과 Kcal 데이터 가져오기
                    mDatabaseRef.child("UserAccount").child(userId).child(currentDate).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                            if (dateSnapshot.exists()) {
                                Integer time = dateSnapshot.child("Time").getValue(Integer.class);
                                Integer kcal = dateSnapshot.child("Kcal").getValue(Integer.class);

                                binding.mainTime.setText(time != null ? String.valueOf(time) : "0");
                                binding.mainKcal.setText(kcal != null ? String.valueOf(kcal) : "0");
                            } else {
                                binding.mainTime.setText("0");
                                binding.mainKcal.setText("0");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MainActivity.this, "데이터베이스 오류: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    binding.userName.setText("샨티님");
                    binding.mainScore.setText("0"); // User not found, set score to 0
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // onCancelled 이벤트 처리
            }
        });
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(MainActivity.this, targetActivity);
        startActivity(intent);
    }

    private void logout() {
        mFirebaseAuth.signOut();

        // SharedPreferences에서 로그인 정보 삭제
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false); // 로그인 상태 플래그를 false로 설정
        editor.apply();

        // 로그인 화면으로 이동
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 현재 액티비티를 스택에서 제거
        startActivity(intent);
        finish();
    }
}
