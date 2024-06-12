package com.gnupr.postureteacher.Databases.EntityClass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.*;
import java.time.LocalDateTime;


@Entity(tableName = "MeasureRounds")
public class MeasureRoundsEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int MeasureRoundID;

    @ColumnInfo
    private LocalDateTime MeasureRoundStartTime;

    @ColumnInfo
    private LocalDateTime MeasureRoundEndTime;

    public MeasureRoundsEntity() {
    }

    public MeasureRoundsEntity(int DB_MeasureRoundID, LocalDateTime DB_MeasureRoundStartTime, LocalDateTime DB_MeasureRoundEndTime) {
        this.MeasureRoundID = DB_MeasureRoundID;
        this.MeasureRoundStartTime = DB_MeasureRoundStartTime;
        this.MeasureRoundEndTime = DB_MeasureRoundEndTime;
    }

    public int getMeasureRoundID() {
        return MeasureRoundID;
    }
    public void setMeasureRoundID(int MeasureRoundID) {
        this.MeasureRoundID = MeasureRoundID;
    }

    public LocalDateTime getMeasureRoundStartTime() { return MeasureRoundStartTime; }
    public void setMeasureRoundStartTime(LocalDateTime MeasureRoundStartTime) {
        this.MeasureRoundStartTime = MeasureRoundStartTime;
    }

    public LocalDateTime getMeasureRoundEndTime() { return MeasureRoundEndTime; }
    public void setMeasureRoundEndTime(LocalDateTime MeasureRoundEndTime) {
        this.MeasureRoundEndTime = MeasureRoundEndTime;
    }
}