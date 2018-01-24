package studios.gaberlunzie.locchange;

import android.arch.persistence.room.Room;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import studios.gaberlunzie.locchange.models.SavedLocation;
import studios.gaberlunzie.locchange.models.SavedLocationDao;

/**
 * Created by Andrew on 1/21/2018.
 */

public class UserSession {

    public static SavedLocation savedLocation;

    public UserSession(){

    }

    public static SavedLocation addSavedLocation(final AppDatabase db, SavedLocation savedLocation) {
        db.savedLocationDao().insertAll(savedLocation);
        return savedLocation;
    }

    public static void deleteSavedLocation(final AppDatabase db, SavedLocation savedLocation) {
        db.savedLocationDao().delete(savedLocation);
    }

    public static List<SavedLocation> getSavedLocations(final AppDatabase db) {
        return db.savedLocationDao().getAll();
    }

    public static List<SavedLocation> getSavedLocationByLocation(final AppDatabase db, double longitude, double latitude) {
        return db.savedLocationDao().getSavedLocationByLocation(longitude, latitude);
    }

    public static void setSavedLocation(SavedLocation location){
        savedLocation = location;
    }

    public static SavedLocation getSavedLocation(){
        return savedLocation;
    }

}
