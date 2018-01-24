package studios.gaberlunzie.locchange;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import studios.gaberlunzie.locchange.models.SavedLocation;

public class LocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private final Context context = this;

    private GoogleMap mMap;

    final int ACCESS_FINE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LocationsActivity.class);
        startActivity(intent);
        //super.onBackPressed();
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng marker = new LatLng(32.746723, -79.969213);
        mMap.addMarker(new MarkerOptions().position(marker).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 15.0f));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.setOnMapClickListener(mapClickListener);
        mMap.setOnMarkerClickListener(markerClickListener);

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    ACCESS_FINE );
        }else{
            AlertDialog.Builder alert = new AlertDialog.Builder(context);

            alert.setTitle("Set as Mock Location App in Dev Settings before Continuing");

            alert.setMessage("");

            alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    initLocation();
                }
            });

            alert.show();
        }

        List<SavedLocation> savedLocations = UserSession.getSavedLocations(AppDatabase.getAppDatabase(this));
        for (SavedLocation location:savedLocations
             ) {
            mMap.addMarker(new MarkerOptions().position(location.getLatLng()).title(location.getLocationName()));
            Log.d("DBG", location.getLocationName());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateLocation();
    }

    public void updateLocation(){
        if(UserSession.getSavedLocation() != null){
            Intent intent = new Intent(this, LocationService.class);
            stopService(intent);
            startService(intent);

            LatLng marker = new LatLng(UserSession.getSavedLocation().getLatitude(), UserSession.getSavedLocation().getLongitude());
            mMap.addMarker(new MarkerOptions().position(marker).title("Marker"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 15.0f));
        }
    }

    public void initLocation(){
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            mMap.setMyLocationEnabled(true);
        }
    }

    GoogleMap.OnMapClickListener mapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(final LatLng latLng) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);

            final EditText edittext = new EditText(context);
            alert.setMessage("Save This Location?");
            alert.setTitle("Enter Your Title");

            alert.setView(edittext);

            alert.setPositiveButton("Save Location", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = edittext.getText().toString();

                    SavedLocation savedLocation = new SavedLocation(value, latLng);
                    UserSession.addSavedLocation(AppDatabase.getAppDatabase(context), savedLocation);
                }
            });

            alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
        }
    };

    GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker marker) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);

            alert.setTitle("Go Here?");

            alert.setMessage(marker.getTitle());

            alert.setPositiveButton("Go", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    List<SavedLocation> savedLocations = UserSession.getSavedLocationByLocation(AppDatabase.getAppDatabase(context), marker.getPosition().longitude, marker.getPosition().latitude);
                    if(!savedLocations.isEmpty()){
                        UserSession.setSavedLocation(savedLocations.get(0));
                        updateLocation();
                    }
                }
            });

            alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });

            alert.show();
            return false;
        }
    };

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case ACCESS_FINE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initLocation();
                }
            }
            default:break;
        }
    }
}
