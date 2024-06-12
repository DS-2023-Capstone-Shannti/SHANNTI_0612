package com.gnupr.postureteacher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShootingGameRanking extends AppCompatActivity {
    ListView listViewRanking;
    List<UserAccount> userList;
    DatabaseReference databaseReference;
    private Switch rankSwitch;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shooting_game_ranking);

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


        listViewRanking = findViewById(R.id.listViewRanking);
        userList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Shannti").child("UserAccount");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserAccount user = snapshot.getValue(UserAccount.class);
                    // Only add users who have Shot_score
                    if (user.getShot_score() != 0) {
                        userList.add(user);
                    }
                }

                // Sort the userList by Shot_score in descending order
                Collections.sort(userList, new Comparator<UserAccount>() {
                    @Override
                    public int compare(UserAccount user1, UserAccount user2) {
                        return user2.getShot_score() - user1.getShot_score();
                    }
                });

                // Now, userList is sorted by Shot_score in descending order
                // Assign ranks to users sequentially starting from 1
                int rank = 1;
                for (int i = 0; i < userList.size(); i++) {
                    userList.get(i).setRank(rank);
                    // Check if there are users with the same score
                    if (i < userList.size() - 1 && userList.get(i).getShot_score() != userList.get(i + 1).getShot_score()) {
                        rank++;
                    }
                }

                // You can use this list to populate your ListView
                // For simplicity, I'm assuming you have a custom adapter to populate the ListView
                // You can create a custom adapter or use ArrayAdapter as per your requirement
                // Here, I'll just demonstrate using ArrayAdapter
                RankingAdapter adapter = new RankingAdapter(ShootingGameRanking.this, userList);
                listViewRanking.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(ShootingGameRanking.this, targetActivity);
        startActivity(intent);
    }

}