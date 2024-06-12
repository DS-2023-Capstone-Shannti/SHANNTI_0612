package com.gnupr.postureteacher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class HeroRankingAdapter extends ArrayAdapter<UserAccount> {
    private Context context;
    private List<UserAccount> userList;

    public HeroRankingAdapter(Context context, List<UserAccount> userList) {
        super(context, 0, userList);
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.ranking_list_item, parent, false);
        }

        UserAccount userAccount = userList.get(position);

        TextView userNameTextView = convertView.findViewById(R.id.user_name);
        TextView heroScoreTextView = convertView.findViewById(R.id.hero_score);
        TextView rankingTextView = convertView.findViewById(R.id.ranking);

        userNameTextView.setText(userAccount.getUserName());
        heroScoreTextView.setText(String.valueOf(userAccount.getHero_score()));

        // 순위 계산
        int rank = 1;
        if (position > 0) {
            UserAccount previousUser = userList.get(position - 1);
            if (previousUser.getHero_score() == userAccount.getHero_score()) {
                rank = Integer.parseInt(rankingTextView.getText().toString());
            } else {
                rank = position + 1;
            }
        }
        rankingTextView.setText(String.valueOf(rank));

        return convertView;
    }
}

