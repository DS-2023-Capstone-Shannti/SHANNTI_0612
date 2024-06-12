package com.gnupr.postureteacher.Databases.EntityClass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.*;
import java.time.LocalDateTime;


@Entity(tableName = "Measure2Rounds")
public class Measure2RoundsEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int Measure2RoundID;

    @ColumnInfo
    private LocalDateTime Measure2RoundStartTime;

    @ColumnInfo
    private LocalDateTime Measure2RoundEndTime;

    @ColumnInfo
    private int Measure2RoundTargetCount;

    @ColumnInfo
    private int Measure2RoundCurrentCount;

    @ColumnInfo
    private int Measure2RoundSquatTimer;


    public Measure2RoundsEntity() {
    }

    public Measure2RoundsEntity(int DB_Measure2RoundID, LocalDateTime DB_Measure2RoundStartTime, LocalDateTime DB_Measure2RoundEndTime,
                                int DB_Measure2RoundTargetCount, int DB_Measure2RoundCurrentCount, int DB_Measure2RoundSquatTimer) {
        this.Measure2RoundID = DB_Measure2RoundID;
        this.Measure2RoundStartTime = DB_Measure2RoundStartTime;
        this.Measure2RoundEndTime = DB_Measure2RoundEndTime;
        this.Measure2RoundTargetCount = DB_Measure2RoundTargetCount;
        this.Measure2RoundCurrentCount = DB_Measure2RoundCurrentCount;
        this.Measure2RoundSquatTimer = DB_Measure2RoundSquatTimer;
    }

    public int getMeasure2RoundID() {
        return Measure2RoundID;
    }
    public void setMeasure2RoundID(int Measure2RoundID) {
        this.Measure2RoundID = Measure2RoundID;
    }

    public LocalDateTime getMeasure2RoundStartTime() { return Measure2RoundStartTime; }
    public void setMeasure2RoundStartTime(LocalDateTime Measure2RoundStartTime) {
        this.Measure2RoundStartTime = Measure2RoundStartTime;
    }

    public LocalDateTime getMeasure2RoundEndTime() { return Measure2RoundEndTime; }
    public void setMeasure2RoundEndTime(LocalDateTime Measure2RoundEndTime) {
        this.Measure2RoundEndTime = Measure2RoundEndTime;
    }

    public int getMeasure2RoundTargetCount() { return Measure2RoundTargetCount; }
    public void setMeasure2RoundTargetCount(int Measure2RoundTargetCount) {
        this.Measure2RoundTargetCount = Measure2RoundTargetCount;
    }

    public int getMeasure2RoundCurrentCount() { return Measure2RoundCurrentCount; }
    public void setMeasure2RoundCurrentCount(int Measure2RoundCurrentCount) {
        this.Measure2RoundCurrentCount = Measure2RoundCurrentCount;
    }

    public int getMeasure2RoundSquatTimer() { return Measure2RoundSquatTimer; }
    public void setMeasure2RoundSquatTimer(int Measure2RoundSquatTimer) {
        this.Measure2RoundSquatTimer = Measure2RoundSquatTimer;
    }
}