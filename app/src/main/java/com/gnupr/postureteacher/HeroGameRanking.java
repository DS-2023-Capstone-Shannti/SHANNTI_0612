package com.gnupr.postureteacher;


        import android.content.Intent;
        import android.os.Bundle;
        import android.widget.CompoundButton;
        import android.widget.ListView;
        import android.widget.Switch;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;

        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;

        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.List;

public class HeroGameRanking extends AppCompatActivity {
    private DatabaseReference mDatabaseRef;
    private List<UserAccount> userList;
    private HeroRankingAdapter rankingAdapter;
    private FirebaseAuth mFirebaseAuth; // Firebase Authentication
    private Switch rankSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hero_game_ranking);

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


        ListView listView = findViewById(R.id.ranking_list_view);
        userList = new ArrayList<>();
        rankingAdapter = new HeroRankingAdapter(this, userList);
        listView.setAdapter(rankingAdapter);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Shannti").child("UserAccount");

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    UserAccount userAccount = userSnapshot.getValue(UserAccount.class);
                    if (userAccount != null && userAccount.getHero_score() > 0) {
                        userList.add(userAccount);
                    }
                }
                Collections.sort(userList, (u1, u2) -> Integer.compare(u2.getHero_score(), u1.getHero_score()));
                rankingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HeroGameRanking.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(HeroGameRanking.this, targetActivity);
        startActivity(intent);
    }

}
