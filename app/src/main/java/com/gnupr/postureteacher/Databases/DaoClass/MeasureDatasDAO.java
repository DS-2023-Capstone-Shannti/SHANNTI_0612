package com.gnupr.postureteacher.Databases.DaoClass;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gnupr.postureteacher.Databases.EntityClass.MeasureDatasEntity;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface MeasureDatasDAO {
    @Insert
    void insert(MeasureDatasEntity measurementTableEntity);

    @Update
    void update(MeasureDatasEntity measurementTableEntity);

    @Delete
    void delete(MeasureDatasEntity measurementTableEntity);

    //Select All Data
    @Query("SELECT * FROM MeasureDatas")
    List<MeasureDatasEntity> getAllData();

    @Query("DELETE FROM MeasureDatas")
    void deleteAll();

    //Select Date Data
    @Query("SELECT * FROM MeasureDatas WHERE MeasureRoundStartTimeFK = :MeasureRoundStartTime")
    List<MeasureDatasEntity> getTimeData(LocalDateTime MeasureRoundStartTime);
}