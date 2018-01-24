package studios.gaberlunzie.locchange.models;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 1/21/2018.
 */
@Dao
public interface SavedLocationDao {

    @Query("SELECT * FROM SavedLocation order by location_name asc")
    List<SavedLocation> getAll();

    @Query("SELECT * FROM SavedLocation")
    public SavedLocation[] loadAllSavedLocations();

    @Query("SELECT * FROM SavedLocation WHERE longitude LIKE :longitude AND latitude like :latitude")
    public List<SavedLocation> getSavedLocationByLocation(double longitude, double latitude);

    @Insert
    void insertAll(SavedLocation... savedLocations);

    @Delete
    void delete(SavedLocation savedLocation);
}
