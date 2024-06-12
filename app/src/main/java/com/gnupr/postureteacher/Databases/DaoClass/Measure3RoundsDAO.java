package com.gnupr.postureteacher.Databases.DaoClass;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gnupr.postureteacher.Databases.EntityClass.Measure3RoundsEntity;

import java.util.List;

@Dao
public interface Measure3RoundsDAO {
    @Insert
    void insert(Measure3RoundsEntity measurementTableEntity);

    @Update
    void update(Measure3RoundsEntity measurementTableEntity);

    @Delete
    void delete(Measure3RoundsEntity measurementTableEntity);

    //Select All Data
    @Query("SELECT * FROM Measure3Rounds")
    List<Measure3RoundsEntity> getAllData();

    @Query("DELETE FROM Measure3Rounds")
    void deleteAll();

    //Select id = i Data
    @Query("SELECT * FROM Measure3Rounds WHERE Measure3RoundID = :i")
    Measure3RoundsEntity getRoundData(int i);
}