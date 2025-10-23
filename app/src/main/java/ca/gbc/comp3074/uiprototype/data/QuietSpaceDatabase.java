package ca.gbc.comp3074.uiprototype.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = { PlaceEntity.class }, version = 3, exportSchema = false)
@TypeConverters({ Converters.class })
public abstract class QuietSpaceDatabase extends RoomDatabase {

    private static volatile QuietSpaceDatabase INSTANCE;
    private static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public abstract PlaceDao placeDao();

    public static QuietSpaceDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (QuietSpaceDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            QuietSpaceDatabase.class,
                            "quietspace-db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
