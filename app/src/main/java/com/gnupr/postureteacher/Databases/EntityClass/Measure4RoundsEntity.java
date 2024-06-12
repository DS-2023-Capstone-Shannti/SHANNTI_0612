package com.gnupr.postureteacher.Databases.EntityClass;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;


@Entity(tableName = "Measure4Rounds")
public class Measure4RoundsEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int Measure4RoundID;

    @ColumnInfo
    private LocalDateTime Measure4RoundStartTime;

    @ColumnInfo
    private LocalDateTime Measure4RoundEndTime;

    @ColumnInfo
    private int Measure4RoundTargetCount;

    @ColumnInfo
    private int Measure4RoundCurrentCount;

    @ColumnInfo
    private int Measure4RoundLungeTimer;


    public Measure4RoundsEntity() {
    }

    public Measure4RoundsEntity(int DB_Measure4RoundID, LocalDateTime DB_Measure4RoundStartTime, LocalDateTime DB_Measure4RoundEndTime,
                                int DB_Measure4RoundTargetCount, int DB_Measure4RoundCurrentCount, int DB_Measure4RoundLungeTimer) {
        this.Measure4RoundID = DB_Measure4RoundID;
        this.Measure4RoundStartTime = DB_Measure4RoundStartTime;
        this.Measure4RoundEndTime = DB_Measure4RoundEndTime;
        this.Measure4RoundTargetCount = DB_Measure4RoundTargetCount;
        this.Measure4RoundCurrentCount = DB_Measure4RoundCurrentCount;
        this.Measure4RoundLungeTimer = DB_Measure4RoundLungeTimer;
    }

    public int getMeasure4RoundID() {
        return Measure4RoundID;
    }
    public void setMeasure4RoundID(int Measure4RoundID) {
        this.Measure4RoundID = Measure4RoundID;
    }

    public LocalDateTime getMeasure4RoundStartTime() { return Measure4RoundStartTime; }
    public void setMeasure4RoundStartTime(LocalDateTime Measure4RoundStartTime) {
        this.Measure4RoundStartTime = Measure4RoundStartTime;
    }

    public LocalDateTime getMeasure4RoundEndTime() { return Measure4RoundEndTime; }
    public void setMeasure4RoundEndTime(LocalDateTime Measure4RoundEndTime) {
        this.Measure4RoundEndTime = Measure4RoundEndTime;
    }

    public int getMeasure4RoundTargetCount() { return Measure4RoundTargetCount; }
    public void setMeasure4RoundTargetCount(int Measure4RoundTargetCount) {
        this.Measure4RoundTargetCount = Measure4RoundTargetCount;
    }

    public int getMeasure4RoundCurrentCount() { return Measure4RoundCurrentCount; }
    public void setMeasure4RoundCurrentCount(int Measure4RoundCurrentCount) {
        this.Measure4RoundCurrentCount = Measure4RoundCurrentCount;
    }

    public int getMeasure4RoundLungeTimer() { return Measure4RoundLungeTimer; }
    public void setMeasure4RoundLungeTimer(int Measure4RoundLungeTimer) {
        this.Measure4RoundLungeTimer = Measure4RoundLungeTimer;
    }
}