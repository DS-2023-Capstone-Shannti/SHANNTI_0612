package com.gnupr.postureteacher.Databases.EntityClass;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;


@Entity(tableName = "Measure3Rounds")
public class Measure3RoundsEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int Measure3RoundID;

    @ColumnInfo
    private LocalDateTime Measure3RoundStartTime;

    @ColumnInfo
    private LocalDateTime Measure3RoundEndTime;

    public Measure3RoundsEntity() {
    }

    public Measure3RoundsEntity(int DB_Measure3RoundID, LocalDateTime DB_Measure3RoundStartTime, LocalDateTime DB_Measure3RoundEndTime) {
        this.Measure3RoundID = DB_Measure3RoundID;
        this.Measure3RoundStartTime = DB_Measure3RoundStartTime;
        this.Measure3RoundEndTime = DB_Measure3RoundEndTime;
    }

    public int getMeasure3RoundID() {
        return Measure3RoundID;
    }
    public void setMeasure3RoundID(int Measure3RoundID) {
        this.Measure3RoundID = Measure3RoundID;
    }

    public LocalDateTime getMeasure3RoundStartTime() { return Measure3RoundStartTime; }
    public void setMeasure3RoundStartTime(LocalDateTime Measure3RoundStartTime) {
        this.Measure3RoundStartTime = Measure3RoundStartTime;
    }

    public LocalDateTime getMeasure3RoundEndTime() { return Measure3RoundEndTime; }
    public void setMeasure3RoundEndTime(LocalDateTime Measure3RoundEndTime) {
        this.Measure3RoundEndTime = Measure3RoundEndTime;
    }
}