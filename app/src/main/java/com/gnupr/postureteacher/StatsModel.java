package com.gnupr.postureteacher;

public class StatsModel {
    private int id;
    private String time;
    private String percent;
    private String unstable;

    public String getId() {
        String stid = Integer.toString(id);
        return stid;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public String getUnstable() {
        return unstable;
    }

    public void setUnstable(String unstable) {
        this.unstable = unstable;
    }


    public StatsModel(){
    }
    public StatsModel(int id, String time, String percent, String unstable) {
        this.id = id;
        this.time = time;
        this.percent = percent;
        this.unstable = unstable;
    }
}
