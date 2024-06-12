package com.gnupr.postureteacher.Databases.EntityClass;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(tableName = "Measure3Datas"/*,
        foreignKeys = @ForeignKey
                (entity = MeasureRoundsEntity.class,
                parentColumns = "MeasureRoundStartTime",
                childColumns = "MeasureRoundStartTimeFK")*/
        //그냥 변수로 저장해놓았다가 하기
)
public class Measure3DatasEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int Measure3DataID;

    @ColumnInfo
    private LocalDateTime Measure3DataStartTime;

    @ColumnInfo
    private LocalDateTime Measure3DataEndTime;

    @ColumnInfo
    private LocalDateTime Measure3RoundStartTimeFK;

    public Measure3DatasEntity() {
    }

    public Measure3DatasEntity(int DB_Measure3DataID, LocalDateTime DB_Measure3DataStartTime, LocalDateTime DB_Measure3DataEndTime, LocalDateTime DB_Measure3RoundStartTimeFK) {
        this.Measure3DataID = DB_Measure3DataID;
        this.Measure3DataStartTime = DB_Measure3DataStartTime;
        this.Measure3DataEndTime = DB_Measure3DataEndTime;
        this.Measure3RoundStartTimeFK = DB_Measure3RoundStartTimeFK;
    }

    public int getMeasure3DataID() {
        return Measure3DataID;
    }
    public void setMeasure3DataID(int Measure3DataID) {
        this.Measure3DataID = Measure3DataID;
    }

    public LocalDateTime getMeasure3DataStartTime() { return Measure3DataStartTime; }
    public void setMeasure3DataStartTime(LocalDateTime Measure3DataStartTime) {
        this.Measure3DataStartTime = Measure3DataStartTime;
    }

    public LocalDateTime getMeasure3DataEndTime() { return Measure3DataEndTime; }
    public void setMeasure3DataEndTime(LocalDateTime Measure3DataEndTime) {
        this.Measure3DataEndTime = Measure3DataEndTime;
    }

    public LocalDateTime getMeasure3RoundStartTimeFK() { return Measure3RoundStartTimeFK; }
    public void setMeasure3RoundStartTimeFK(LocalDateTime Measure3RoundStartTimeFK) {
        this.Measure3RoundStartTimeFK = Measure3RoundStartTimeFK;
    }
}