package com.gnupr.postureteacher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class MainRankingActivity extends AppCompatActivity {

    private Switch rankSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_main);

        rankSwitch = findViewById(R.id.rank_switch);

        rankSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    navigateToActivity(ShootingGameRanking.class);
                } else {
                    navigateToActivity(HeroGameRanking.class);
                }
            }
        });
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(MainRankingActivity.this, targetActivity);
        startActivity(intent);
    }
}
