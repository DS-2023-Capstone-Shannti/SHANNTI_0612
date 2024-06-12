package com.gnupr.postureteacher.Databases.EntityClass;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(tableName = "Measure2Datas"/*,
        foreignKeys = @ForeignKey
                (entity = Measure2RoundsEntity.class,
                parentColumns = "Measure2RoundStartTime",
                childColumns = "Measure2RoundStartTimeFK")*/
        //그냥 변수로 저장해놓았다가 하기
)
public class Measure2DatasEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int Measure2DataID;

    @ColumnInfo
    private LocalDateTime Measure2DataStartTime;

    @ColumnInfo
    private LocalDateTime Measure2DataEndTime;

    @ColumnInfo
    private LocalDateTime Measure2RoundStartTimeFK;

    @ColumnInfo
    private int Measure2DataDetectCount;

    public Measure2DatasEntity() {
    }

    public Measure2DatasEntity(int DB_Measure2DataID, LocalDateTime DB_Measure2DataStartTime, LocalDateTime DB_Measure2DataEndTime, LocalDateTime DB_Measure2RoundStartTimeFK, int DB_Measure2DataDetectCount) {
        this.Measure2DataID = DB_Measure2DataID;
        this.Measure2DataStartTime = DB_Measure2DataStartTime;
        this.Measure2DataEndTime = DB_Measure2DataEndTime;
        this.Measure2RoundStartTimeFK = DB_Measure2RoundStartTimeFK;
        this.Measure2DataDetectCount = DB_Measure2DataDetectCount;
    }

    public int getMeasure2DataID() {
        return Measure2DataID;
    }
    public void setMeasure2DataID(int Measure2DataID) {
        this.Measure2DataID = Measure2DataID;
    }

    public LocalDateTime getMeasure2DataStartTime() { return Measure2DataStartTime; }
    public void setMeasure2DataStartTime(LocalDateTime Measure2DataStartTime) {
        this.Measure2DataStartTime = Measure2DataStartTime;
    }

    public LocalDateTime getMeasure2DataEndTime() { return Measure2DataEndTime; }
    public void setMeasure2DataEndTime(LocalDateTime Measure2DataEndTime) {
        this.Measure2DataEndTime = Measure2DataEndTime;
    }

    public LocalDateTime getMeasure2RoundStartTimeFK() { return Measure2RoundStartTimeFK; }
    public void setMeasure2RoundStartTimeFK(LocalDateTime Measure2RoundStartTimeFK) {
        this.Measure2RoundStartTimeFK = Measure2RoundStartTimeFK;
    }

    public int getMeasure2DataDetectCount() { return Measure2DataDetectCount; }
    public void setMeasure2DataDetectCount(int Measure2DataDetectCount) {
        this.Measure2DataDetectCount = Measure2DataDetectCount;
    }
}