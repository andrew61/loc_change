package studios.gaberlunzie.locchange;
import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class LocationService extends Service{

    final String MY_GPS_PROVIDER = "BOOM_SHACKA_LA";
    final int ACCESS_FINE = 0;

    CountDownTimer timer;

    boolean isWalking = false;
    boolean alternate = false;

    double longitude = -79.969593;
    double latitude = 32.746626;

    int timerLength = 1800000;
    float accuracy = 5;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("DBG", "STARTING SERVICE");

        initLocation();

        timer = new CountDownTimer(timerLength, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                initLocation();
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUpdate();
        Log.d("DBG", "STOPPING SERVICE");
    }

    public void updateLocation(){
        if(UserSession.getSavedLocation() != null){
            longitude = UserSession.getSavedLocation().getLongitude();
            latitude = UserSession.getSavedLocation().getLatitude();
        }
    }

    public void stopUpdate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

            if(locationManager.getProvider(MY_GPS_PROVIDER) != null){
                locationManager.removeTestProvider(MY_GPS_PROVIDER);
            }
        }
    }

    public void initLocation(){
        updateLocation();
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, locationListener);

            if(locationManager.getProvider(MY_GPS_PROVIDER) != null){
                locationManager.removeTestProvider(MY_GPS_PROVIDER);
            }

            locationManager.addTestProvider(MY_GPS_PROVIDER, true, false, false, false, true,true, true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);

            Location location = new Location(LocationManager.GPS_PROVIDER);
            if(isWalking){
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                latitude = latitude - .0008;
            }else{
                location.setLatitude(latitude);
                location.setLongitude(longitude);
            }

            location.setTime(System.currentTimeMillis());
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            if(alternate){
                location.setAccuracy(accuracy + 2);
                alternate = false;
            }else{
                location.setAccuracy(accuracy);
                alternate = true;
            }

            locationManager.setTestProviderLocation(MY_GPS_PROVIDER, location);
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
        }

        if(timer != null){
            Log.d("DBG", "SETTING MOCK");
            timer.cancel();
            timer.start();
        }
    }

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
}