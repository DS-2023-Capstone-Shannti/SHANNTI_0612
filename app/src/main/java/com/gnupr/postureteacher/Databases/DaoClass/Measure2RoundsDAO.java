package com.gnupr.postureteacher.Databases.DaoClass;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gnupr.postureteacher.Databases.EntityClass.Measure2RoundsEntity;
import com.gnupr.postureteacher.Databases.EntityClass.MeasureRoundsEntity;

import java.util.List;

@Dao
public interface Measure2RoundsDAO {
    @Insert
    void insert(Measure2RoundsEntity measurementTableEntity);

    @Update
    void update(Measure2RoundsEntity measurementTableEntity);

    @Delete
    void delete(Measure2RoundsEntity measurementTableEntity);

    //Select All Data
    @Query("SELECT * FROM Measure2Rounds")
    List<Measure2RoundsEntity> getAllData();

    @Query("DELETE FROM Measure2Rounds")
    void deleteAll();

    //Select id = i Data
    @Query("SELECT * FROM Measure2Rounds WHERE Measure2RoundID = :i")
    Measure2RoundsEntity getRoundData(int i);
}