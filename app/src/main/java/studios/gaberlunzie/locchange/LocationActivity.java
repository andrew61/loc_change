package studios.gaberlunzie.locchange;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import studios.gaberlunzie.locchange.models.SavedLocation;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    final int ACCESS_LOCATIONS = 0;
    final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    final static String[] PERMISSIONS =  {  android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION  };

    private final Context context = this;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    };

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

        LatLng marker = new LatLng(32.746723, -79.969213);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 15.0f));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.setOnMapClickListener(mapClickListener);
        mMap.setOnMarkerClickListener(markerClickListener);

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, PERMISSIONS,
                    ACCESS_LOCATIONS );
        }else {
            initLocation();
        }

        List<SavedLocation> savedLocations = UserSession.getSavedLocations(AppDatabase.getAppDatabase(this));
        for (SavedLocation location:savedLocations
                ) {
            mMap.addMarker(new MarkerOptions().position(location.getLatLng()).title(location.getLocationName()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mMap != null){
            mMap.clear();
            List<SavedLocation> savedLocations = UserSession.getSavedLocations(AppDatabase.getAppDatabase(this));
            for (SavedLocation location:savedLocations
                    ) {
                mMap.addMarker(new MarkerOptions().position(location.getLatLng()).title(location.getLocationName()));
            }
            updateLocation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.location_toolbar, menu);

        // Configure the search info and add any event listeners...

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_search:
                findPlace();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    // A place has been received; use requestCode to track the request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 25.0f));
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("DBG", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public void findPlace() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
            e.printStackTrace();
        }
    }

    public void updateLocation(){
        if(UserSession.getSavedLocation() != null){
            Intent intent = new Intent(this, LocationService.class);
            stopService(intent);
            startService(intent);

//            LatLng marker = new LatLng(UserSession.getSavedLocation().getLatitude(), UserSession.getSavedLocation().getLongitude());
//            mMap.addMarker(new MarkerOptions().position(marker).title("Marker"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
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
                    mMap.addMarker(new MarkerOptions().position(savedLocation.getLatLng()).title(savedLocation.getLocationName()));
                }
            });

            alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.setNeutralButton("Go", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String value = "Go";
                    SavedLocation savedLocation = new SavedLocation(value, latLng);
                    UserSession.setSavedLocation(savedLocation);
                    updateLocation();
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
            alert.setNeutralButton("Share", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context); // Need to change the build to API 19

                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, marker.getPosition().toString());

                    if (defaultSmsPackageName != null){
                        sendIntent.setPackage(defaultSmsPackageName);
                    }
                    startActivity(sendIntent);
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

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case ACCESS_LOCATIONS:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initLocation();
                }
            }
            default:break;
        }
    }
}
