package com.gnupr.postureteacher.Databases;

import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.gnupr.postureteacher.Databases.Converters.DateConverters;
import com.gnupr.postureteacher.Databases.DaoClass.Measure2DatasDAO;
import com.gnupr.postureteacher.Databases.DaoClass.Measure2RoundsDAO;
import com.gnupr.postureteacher.Databases.DaoClass.MeasureDatasDAO;
import com.gnupr.postureteacher.Databases.DaoClass.MeasureRoundsDAO;
import com.gnupr.postureteacher.Databases.EntityClass.Measure2DatasEntity;
import com.gnupr.postureteacher.Databases.EntityClass.Measure2RoundsEntity;
import com.gnupr.postureteacher.Databases.EntityClass.MeasureDatasEntity;
import com.gnupr.postureteacher.Databases.EntityClass.MeasureRoundsEntity;
import com.gnupr.postureteacher.Databases.DaoClass.Measure4DatasDAO;
import com.gnupr.postureteacher.Databases.DaoClass.Measure4RoundsDAO;
import com.gnupr.postureteacher.Databases.DaoClass.Measure3DatasDAO;
import com.gnupr.postureteacher.Databases.DaoClass.Measure3RoundsDAO;
import com.gnupr.postureteacher.Databases.EntityClass.Measure4DatasEntity;
import com.gnupr.postureteacher.Databases.EntityClass.Measure4RoundsEntity;
import com.gnupr.postureteacher.Databases.EntityClass.Measure3DatasEntity;
import com.gnupr.postureteacher.Databases.EntityClass.Measure3RoundsEntity;



@Database(version = 2,entities = {MeasureDatasEntity.class, MeasureRoundsEntity.class, Measure2DatasEntity.class, Measure2RoundsEntity.class,Measure3DatasEntity.class, Measure3RoundsEntity.class, Measure4DatasEntity.class, Measure4RoundsEntity.class}, exportSchema = false)
@TypeConverters({DateConverters.class})
public abstract class MeasureRoomDatabase extends RoomDatabase {

    public abstract MeasureDatasDAO getMeasureDatasDao();
    public abstract MeasureRoundsDAO getMeasureRoundsDao();
    public abstract Measure2DatasDAO getMeasure2DatasDao();
    public abstract Measure2RoundsDAO getMeasure2RoundsDao();

    public abstract Measure3DatasDAO getMeasure3DatasDao();
    public abstract Measure3RoundsDAO getMeasure3RoundsDao();
    public abstract Measure4DatasDAO getMeasure4DatasDao();
    public abstract Measure4RoundsDAO getMeasure4RoundsDao();
    public static final int NUMBER_OF_THREADS = 4;
    private static volatile MeasureRoomDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor
            = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static MeasureRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MeasureRoomDatabase.class) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                MeasureRoomDatabase.class, "measure_database")
                        .allowMainThreadQueries() // modified
                        .build();
            }
        }
        return INSTANCE;
    }
}