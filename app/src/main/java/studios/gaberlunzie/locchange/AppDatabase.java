package studios.gaberlunzie.locchange;

import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.content.Context;

import java.util.List;

import studios.gaberlunzie.locchange.models.SavedLocation;
import studios.gaberlunzie.locchange.models.SavedLocationDao;

/**
 * Created by Andrew on 1/21/2018.
 */
@Database(entities = {SavedLocation.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase{

    private static AppDatabase INSTANCE;

    public abstract SavedLocationDao savedLocationDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "user-database")
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
