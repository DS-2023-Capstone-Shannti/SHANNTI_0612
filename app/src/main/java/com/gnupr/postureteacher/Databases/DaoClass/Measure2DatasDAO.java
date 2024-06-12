package com.gnupr.postureteacher.Databases.DaoClass;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gnupr.postureteacher.Databases.EntityClass.Measure2DatasEntity;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface Measure2DatasDAO {
    @Insert
    void insert(Measure2DatasEntity measurementTableEntity);

    @Update
    void update(Measure2DatasEntity measurementTableEntity);

    @Delete
    void delete(Measure2DatasEntity measurementTableEntity);

    //Select All Data
    @Query("SELECT * FROM Measure2Datas")
    List<Measure2DatasEntity> getAllData();

    @Query("DELETE FROM Measure2Datas")
    void deleteAll();

    //Select Date Data
    @Query("SELECT * FROM Measure2Datas WHERE Measure2RoundStartTimeFK = :Measure2RoundStartTime")
    List<Measure2DatasEntity> getTimeData(LocalDateTime Measure2RoundStartTime);

    //회차단위로 가져오는 쿼리문을 만들어야함 시작시간 + 측정횟수를 가지고
    @Query("SELECT * FROM Measure2Datas WHERE Measure2RoundStartTimeFK = :Measure2RoundStartTime and Measure2DataDetectCount = :DetectCount")
    List<Measure2DatasEntity> getDetectData(LocalDateTime Measure2RoundStartTime, int DetectCount);
}