package com.gnupr.postureteacher.Databases.EntityClass;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(tableName = "Measure4Datas"/*,
        foreignKeys = @ForeignKey
                (entity = Measure2RoundsEntity.class,
                parentColumns = "Measure2RoundStartTime",
                childColumns = "Measure2RoundStartTimeFK")*/
        //그냥 변수로 저장해놓았다가 하기
)
public class Measure4DatasEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int Measure4DataID;

    @ColumnInfo
    private LocalDateTime Measure4DataStartTime;

    @ColumnInfo
    private LocalDateTime Measure4DataEndTime;

    @ColumnInfo
    private LocalDateTime Measure4RoundStartTimeFK;

    @ColumnInfo
    private int Measure4DataDetectCount;

    public Measure4DatasEntity() {
    }

    public Measure4DatasEntity(int DB_Measure4DataID, LocalDateTime DB_Measure4DataStartTime, LocalDateTime DB_Measure4DataEndTime, LocalDateTime DB_Measure4RoundStartTimeFK, int DB_Measure4DataDetectCount) {
        this.Measure4DataID = DB_Measure4DataID;
        this.Measure4DataStartTime = DB_Measure4DataStartTime;
        this.Measure4DataEndTime = DB_Measure4DataEndTime;
        this.Measure4RoundStartTimeFK = DB_Measure4RoundStartTimeFK;
        this.Measure4DataDetectCount = DB_Measure4DataDetectCount;
    }

    public int getMeasure4DataID() {
        return Measure4DataID;
    }
    public void setMeasure4DataID(int Measure4DataID) {
        this.Measure4DataID = Measure4DataID;
    }

    public LocalDateTime getMeasure4DataStartTime() { return Measure4DataStartTime; }
    public void setMeasure4DataStartTime(LocalDateTime Measure4DataStartTime) {
        this.Measure4DataStartTime = Measure4DataStartTime;
    }

    public LocalDateTime getMeasure4DataEndTime() { return Measure4DataEndTime; }
    public void setMeasure4DataEndTime(LocalDateTime Measure4DataEndTime) {
        this.Measure4DataEndTime = Measure4DataEndTime;
    }

    public LocalDateTime getMeasure4RoundStartTimeFK() { return Measure4RoundStartTimeFK; }
    public void setMeasure4RoundStartTimeFK(LocalDateTime Measure4RoundStartTimeFK) {
        this.Measure4RoundStartTimeFK = Measure4RoundStartTimeFK;
    }

    public int getMeasure4DataDetectCount() { return Measure4DataDetectCount; }
    public void setMeasure4DataDetectCount(int Measure4DataDetectCount) {
        this.Measure4DataDetectCount = Measure4DataDetectCount;
    }
}