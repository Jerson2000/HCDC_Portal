package com.jerson.hcdc_portal.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.jerson.hcdc_portal.model.DashboardModel;

@Database(entities = {DashboardModel.class}, version = 1,exportSchema = false)
public abstract class DatabasePortal extends RoomDatabase
{
    private static DatabasePortal databasePortal;

    public static synchronized DatabasePortal getDatabase(Context context){
        if(databasePortal == null){
            databasePortal = Room.databaseBuilder(
                    context,
                    DatabasePortal.class,
                    "portal-database"
            ).fallbackToDestructiveMigration().build();
        }
        return databasePortal;
    }

    public abstract DatabaseDao databaseDao();
}
