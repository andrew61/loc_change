package studios.gaberlunzie.locchange.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Andrew on 1/21/2018.
 */

@Entity(tableName = "SavedLocation")
public class SavedLocation {
    @PrimaryKey(autoGenerate = true)
    private int slid;

    @ColumnInfo(name = "location_name")
    private String locationName;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    @ColumnInfo(name = "latitude")
    private Double latitude;

    public SavedLocation(){

    }

    public SavedLocation(String name, LatLng location){
        setLocationName(name);
        setLatitude(location.latitude);
        setLongitude(location.longitude);
    }

    public int getSlid() {
        return slid;
    }

    public void setSlid(int slid) {
        this.slid = slid;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public LatLng getLatLng(){
        return new LatLng(getLatitude(), getLongitude());
    }
}
