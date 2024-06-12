package com.gnupr.postureteacher.Databases.DaoClass;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gnupr.postureteacher.Databases.EntityClass.Measure4DatasEntity;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface Measure4DatasDAO {
    @Insert
    void insert(Measure4DatasEntity measurementTableEntity);

    @Update
    void update(Measure4DatasEntity measurementTableEntity);

    @Delete
    void delete(Measure4DatasEntity measurementTableEntity);

    //Select All Data
    @Query("SELECT * FROM Measure4Datas")
    List<Measure4DatasEntity> getAllData();

    @Query("DELETE FROM Measure4Datas")
    void deleteAll();

    //Select Date Data
    @Query("SELECT * FROM Measure4Datas WHERE Measure4RoundStartTimeFK = :Measure4RoundStartTime")
    List<Measure4DatasEntity> getTimeData(LocalDateTime Measure4RoundStartTime);

    //회차단위로 가져오는 쿼리문을 만들어야함 시작시간 + 측정횟수를 가지고
    @Query("SELECT * FROM Measure4Datas WHERE Measure4RoundStartTimeFK = :Measure4RoundStartTime and Measure4DataDetectCount = :DetectCount")
    List<Measure4DatasEntity> getDetectData(LocalDateTime Measure4RoundStartTime, int DetectCount);
}