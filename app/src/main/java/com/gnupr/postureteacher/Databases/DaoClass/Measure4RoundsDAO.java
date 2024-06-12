package com.gnupr.postureteacher.Databases.DaoClass;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gnupr.postureteacher.Databases.EntityClass.Measure4RoundsEntity;

import java.util.List;

@Dao
public interface Measure4RoundsDAO {
    @Insert
    void insert(Measure4RoundsEntity measurementTableEntity);

    @Update
    void update(Measure4RoundsEntity measurementTableEntity);

    @Delete
    void delete(Measure4RoundsEntity measurementTableEntity);

    //Select All Data
    @Query("SELECT * FROM Measure4Rounds")
    List<Measure4RoundsEntity> getAllData();

    @Query("DELETE FROM Measure4Rounds")
    void deleteAll();

    //Select id = i Data
    @Query("SELECT * FROM Measure4Rounds WHERE Measure4RoundID = :i")
    Measure4RoundsEntity getRoundData(int i);
}