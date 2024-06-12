package com.gnupr.postureteacher.Databases.EntityClass;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(tableName = "MeasureDatas"/*,
        foreignKeys = @ForeignKey
                (entity = MeasureRoundsEntity.class,
                parentColumns = "MeasureRoundStartTime",
                childColumns = "MeasureRoundStartTimeFK")*/
        //그냥 변수로 저장해놓았다가 하기
)
public class MeasureDatasEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int MeasureDataID;

    @ColumnInfo
    private LocalDateTime MeasureDataStartTime;

    @ColumnInfo
    private LocalDateTime MeasureDataEndTime;

    @ColumnInfo
    private LocalDateTime MeasureRoundStartTimeFK;

    public MeasureDatasEntity() {
    }

    public MeasureDatasEntity(int DB_MeasureDataID, LocalDateTime DB_MeasureDataStartTime, LocalDateTime DB_MeasureDataEndTime, LocalDateTime DB_MeasureRoundStartTimeFK) {
        this.MeasureDataID = DB_MeasureDataID;
        this.MeasureDataStartTime = DB_MeasureDataStartTime;
        this.MeasureDataEndTime = DB_MeasureDataEndTime;
        this.MeasureRoundStartTimeFK = DB_MeasureRoundStartTimeFK;
    }

    public int getMeasureDataID() {
        return MeasureDataID;
    }
    public void setMeasureDataID(int MeasureDataID) {
        this.MeasureDataID = MeasureDataID;
    }

    public LocalDateTime getMeasureDataStartTime() { return MeasureDataStartTime; }
    public void setMeasureDataStartTime(LocalDateTime MeasureDataStartTime) {
        this.MeasureDataStartTime = MeasureDataStartTime;
    }

    public LocalDateTime getMeasureDataEndTime() { return MeasureDataEndTime; }
    public void setMeasureDataEndTime(LocalDateTime MeasureDataEndTime) {
        this.MeasureDataEndTime = MeasureDataEndTime;
    }

    public LocalDateTime getMeasureRoundStartTimeFK() { return MeasureRoundStartTimeFK; }
    public void setMeasureRoundStartTimeFK(LocalDateTime MeasureRoundStartTimeFK) {
        this.MeasureRoundStartTimeFK = MeasureRoundStartTimeFK;
    }
}