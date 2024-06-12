package com.gnupr.postureteacher;
public class UserAccount implements Comparable<UserAccount> {
    private String UserIdToken;
    private String UserId;
    private String UserPw;
    private String UserName;
    private String UserPhone;
    private int Shot_score;
    private int Hero_ranking_score;
    private int Hero_score;

    private int Todaydate;

    private int Time;
    private int Kcal;

    public void setTodaydate(int todaydate) {
        Todaydate = todaydate;
    }

    public void setTime(int time) {
        Time = time;
    }

    public void setKcal(int kcal) {
        Kcal = kcal;
    }

    public int getTodaydate() {
        return Todaydate;
    }

    public int getTime() {
        return Time;
    }

    public int getKcal() {
        return Kcal;
    }

    public void setHero_ranking_score(int hero_ranking_score) {
        Hero_ranking_score = hero_ranking_score;
    }

    public int getHero_ranking_score() {
        return Hero_ranking_score;
    }


    public void setHero_score(int hero_score) {
        Hero_score = hero_score;
    }

    public int getHero_score() {
        return Hero_score;
    }

    private int rank;
    private int rank2;

    public int getRank2() {
        return rank2;
    }

    public void setRank2(int rank2) {
        this.rank2 = rank2;
    }

    public void setShot_score(int shot_score) {
        Shot_score = shot_score;
    }

    public int getShot_score() {
        return Shot_score;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public void setUserPhone(String userPhone) {
        UserPhone = userPhone;
    }

    public String getUserPhone() {
        return UserPhone;
    }

    public UserAccount() {}

    public void setUserIdToken(String userIdToken) {
        UserIdToken = userIdToken;
    }

    public String getUserIdToken() {
        return UserIdToken;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        this.UserId = userId;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserPw(String userPw) {
        UserPw = userPw;
    }

    public String getUserPw() {
        return UserPw;
    }



    @Override
    public int compareTo(UserAccount other) {
        return Integer.compare(other.getHero_ranking_score(), this.Hero_ranking_score);
    }
}
