package com.gnupr.postureteacher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class RankingAdapter extends ArrayAdapter<UserAccount> {
    private Context context;
    private List<UserAccount> userList;

    public RankingAdapter(Context context, List<UserAccount> userList) {
        super(context, 0, userList);
        this.context = context;
        this.userList = userList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_ranking, parent, false);
        }

        TextView tvRank = convertView.findViewById(R.id.tvRank);
        TextView tvUserName = convertView.findViewById(R.id.tvUserName);
        TextView tvScore = convertView.findViewById(R.id.tvScore);

        UserAccount user = userList.get(position);

        // Displaying rank, user name, and score
        tvRank.setText(String.valueOf(user.getRank()));
        tvUserName.setText(user.getUserName());
        tvScore.setText(String.valueOf(user.getShot_score()));

        return convertView;
    }
}
