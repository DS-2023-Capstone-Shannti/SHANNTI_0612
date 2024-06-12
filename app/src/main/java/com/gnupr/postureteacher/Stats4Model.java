package com.gnupr.postureteacher;

public class Stats4Model {
    private int id;
    private int cycletime;
    private String percent;
    private String laps;

    public String getId() {
        String stid = Integer.toString(id);
        return stid;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCycletime() {
        return cycletime;
    }

    public void setCycletime(int cycletime) {
        this.cycletime = cycletime;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public String getLaps() {
        return laps;
    }

    public void setLaps(String laps) {
        this.laps = laps;
    }


    public Stats4Model(){
    }
    public Stats4Model(int id, int cycletime, String percent, String unstable) {
        this.id = id;
        this.cycletime = cycletime;
        this.percent = percent;
        this.laps = laps;
    }
}
