package com.gnupr.postureteacher.Databases.DaoClass;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gnupr.postureteacher.Databases.EntityClass.MeasureDatasEntity;
import com.gnupr.postureteacher.Databases.EntityClass.MeasureRoundsEntity;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface MeasureRoundsDAO {
    @Insert
    void insert(MeasureRoundsEntity measurementTableEntity);

    @Update
    void update(MeasureRoundsEntity measurementTableEntity);

    @Delete
    void delete(MeasureRoundsEntity measurementTableEntity);

    //Select All Data
    @Query("SELECT * FROM MeasureRounds")
    List<MeasureRoundsEntity> getAllData();

    @Query("DELETE FROM MeasureRounds")
    void deleteAll();

    //Select id = i Data
    @Query("SELECT * FROM MeasureRounds WHERE MeasureRoundID = :i")
    MeasureRoundsEntity getRoundData(int i);
}