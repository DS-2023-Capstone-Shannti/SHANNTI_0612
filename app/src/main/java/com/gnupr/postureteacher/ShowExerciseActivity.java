package com.gnupr.postureteacher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ShowExerciseActivity extends AppCompatActivity {
    private AlertDialog difficultyDialog;
    private String selectedDifficulty;
    private static final String PREFS_NAME = "ExercisePrefs";
    private static final String DIFFICULTY_KEY = "selectedDifficulty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_exercise);

        restoreDifficultyPreference();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("difficulty")) {
            selectedDifficulty = intent.getStringExtra("difficulty");
            saveDifficultyPreference(); // 업데이트된 난이도를 저장
        }

        if (selectedDifficulty == null) {
            showDifficultyDialog();
        } else {
            Log.d("ShowExerciseActivity", "Selected difficulty: " + selectedDifficulty);
        }

        setupButtonListeners();
    }

    private void setupButtonListeners() {
//        setButtonClickListener(R.id.done_button, FinishExerciseActivity.class);
        setButtonClickListener(R.id.overlay_home_button, MainActivity.class);

        setButtonClickListener(R.id.overlay_squat1, SquatAnalysisActivity.class);
        setButtonClickListener(R.id.overlay_lunge1, LungeAnalysisActivity.class);
        setButtonClickListener(R.id.overlay_balance1, BalanceAnalysisActivity.class);
        setButtonClickListener(R.id.overlay_plank1, PlankAnalysisActivity.class);

        setExerciseOverlayClickListener(R.id.overlay_squat, SquatActivity.class);
        setExerciseOverlayClickListener(R.id.overlay_lunge, LungeActivity.class);
        setExerciseOverlayClickListener(R.id.overlay_balance, BalanceActivity.class);
        setExerciseOverlayClickListener(R.id.overlay_plank, PlankActivity.class);

        // 난이도 다시 선택 버튼 리스너 추가
        findViewById(R.id.reset_difficulty_button).setOnClickListener(v -> showDifficultyDialog());
    }

    private void setButtonClickListener(int buttonId, final Class<?> activityClass) {
        findViewById(buttonId).setOnClickListener(v -> startActivity(new Intent(ShowExerciseActivity.this, activityClass)));
    }

    private void setExerciseOverlayClickListener(int viewId, final Class<?> activityClass) {
        findViewById(viewId).setOnClickListener(v -> {
            Intent intent = new Intent(ShowExerciseActivity.this, activityClass);
            intent.putExtra("difficulty", selectedDifficulty);
            startActivity(intent);
            finish();
        });
    }

    private void showDifficultyDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TransparentDialogTheme);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_exercise_level, null);
        builder.setView(dialogView);

        dialogView.findViewById(R.id.level1).setOnClickListener(v -> setDifficultyAndDismissDialog("easy"));
        dialogView.findViewById(R.id.level2).setOnClickListener(v -> setDifficultyAndDismissDialog("medium"));
        dialogView.findViewById(R.id.level3).setOnClickListener(v -> setDifficultyAndDismissDialog("hard"));

        difficultyDialog = builder.create();
        difficultyDialog.show();
    }

    private void setDifficultyAndDismissDialog(String difficulty) {
        selectedDifficulty = difficulty;
        saveDifficultyPreference();
        difficultyDialog.dismiss();
    }

    private void saveDifficultyPreference() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(DIFFICULTY_KEY, selectedDifficulty);
        editor.apply();
    }

    private void restoreDifficultyPreference() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedDifficulty = preferences.getString(DIFFICULTY_KEY, null);
    }
}
