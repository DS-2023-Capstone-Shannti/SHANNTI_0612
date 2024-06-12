package com.gnupr.postureteacher.Databases.DaoClass;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gnupr.postureteacher.Databases.EntityClass.Measure3DatasEntity;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface Measure3DatasDAO {
    @Insert
    void insert(Measure3DatasEntity measurementTableEntity);

    @Update
    void update(Measure3DatasEntity measurementTableEntity);

    @Delete
    void delete(Measure3DatasEntity measurementTableEntity);

    //Select All Data
    @Query("SELECT * FROM Measure3Datas")
    List<Measure3DatasEntity> getAllData();

    @Query("DELETE FROM Measure3Datas")
    void deleteAll();

    //Select Date Data
    @Query("SELECT * FROM Measure3Datas WHERE Measure3RoundStartTimeFK = :Measure3RoundStartTime")
    List<Measure3DatasEntity> getTimeData(LocalDateTime Measure3RoundStartTime);
}