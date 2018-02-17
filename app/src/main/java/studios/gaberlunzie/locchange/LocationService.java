package studios.gaberlunzie.locchange;
import android.Manifest;
import android.app.AppOpsManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;

import static studios.gaberlunzie.locchange.LocationsActivity.isWalking;

public class LocationService extends Service{

    final String MY_GPS_PROVIDER = "BOOM_SHACKA_LA";
    final int ACCESS_FINE = 0;

    LocationManager locationManager;

    CountDownTimer timer;

    boolean needUpdateLocation = false;

    double longitude = -79.969593;
    double latitude = 32.746626;

    int timerLength = 3354;
    float accuracy = 22;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("DBG", "STARTING SERVICE");
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy( Criteria.ACCURACY_FINE );
        final String bestProvider = locationManager.getBestProvider( criteria, true );

        if (!canMockLocation(this)) {
            //show alert
            return;
        } else {
            locationManager.addTestProvider(
           LocationManager.GPS_PROVIDER,           //name
           false,                                  //requiresNetwork
           false,                                  //requiresSatellite
           false,                                  //requiresCell
           false,                                  //hasMonetaryCost
            true,                                   //supportsAltitude
             true,                                   //supportsSpeed
              true,                                   //supportsBearing
               0,                                      //powerRequirement
                1                        //accuracy
                 );
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        }
        initLocation();

        if(isWalking){
            timer = new CountDownTimer(timerLength, 1000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    updateLocation(bestProvider);
                }
            }.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUpdate();
        Log.d("DBG", "STOPPING SERVICE");
    }

    public void updateSavedLocation(){
        if(UserSession.getSavedLocation() != null){
            longitude = UserSession.getSavedLocation().getLongitude();
            latitude = UserSession.getSavedLocation().getLatitude();
        }
    }

    public void stopUpdate() {
        if(timer != null){
            timer.cancel();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

            locationManager.clearTestProviderEnabled(LocationManager.GPS_PROVIDER);
            locationManager.clearTestProviderLocation(LocationManager.GPS_PROVIDER);
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        }
    }

    public void initLocation(){
        Criteria criteria = new Criteria();
        criteria.setAccuracy( Criteria.ACCURACY_FINE );
        String bestProvider = locationManager.getBestProvider( criteria, true );

        if ( bestProvider == null ) {
            Log.e( "DBG", "No location provider found!" );
            return;
        }
        updateSavedLocation();
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {

            if(useBestProvider()){
                locationManager.requestLocationUpdates(bestProvider, 50, 0, gpsLocationListener);
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, gpsLocationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50, 0, wifiLocationListener);
            }

            if(locationManager.getProvider(MY_GPS_PROVIDER) != null){
                locationManager.removeTestProvider(MY_GPS_PROVIDER);
            }

            locationManager.addTestProvider(MY_GPS_PROVIDER, true, false, false, false, true,true, true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
            locationManager.requestLocationUpdates(MY_GPS_PROVIDER, 50, 0, mockLocationListener);


            if(useBestProvider()) {
                Location bestLocation = new Location(bestProvider);

                if(isWalking){
                    bestLocation.setLatitude(latitude);
                    bestLocation.setLongitude(longitude);

                    locationManager.clearTestProviderLocation(MY_GPS_PROVIDER);
                    locationManager.clearTestProviderLocation(bestProvider);

                    latitude = latitude - .0008;
                }else{
                    bestLocation.setLatitude(latitude);
                    bestLocation.setLongitude(longitude);
                }

                bestLocation.setTime(System.currentTimeMillis());
                bestLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                bestLocation.setAccuracy(accuracy);

                locationManager.setTestProviderLocation(MY_GPS_PROVIDER, bestLocation);
                locationManager.setTestProviderLocation(bestProvider, bestLocation);
            }else{
                Location gpsLocation = new Location(LocationManager.GPS_PROVIDER);
                Location wifiLocation = new Location(LocationManager.NETWORK_PROVIDER);

                if(isWalking){
                    gpsLocation.setLatitude(latitude);
                    gpsLocation.setLongitude(longitude);

                    wifiLocation.setLatitude(latitude);
                    wifiLocation.setLongitude(latitude);

                    locationManager.clearTestProviderLocation(MY_GPS_PROVIDER);
                    locationManager.clearTestProviderLocation(LocationManager.GPS_PROVIDER);
                    locationManager.clearTestProviderLocation(LocationManager.NETWORK_PROVIDER);

                    latitude = latitude - .0008;
                }else{
                    gpsLocation.setLatitude(latitude);
                    gpsLocation.setLongitude(longitude);

                    wifiLocation.setLatitude(latitude);
                    wifiLocation.setLongitude(latitude);
                }

                gpsLocation.setTime(System.currentTimeMillis());
                gpsLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                gpsLocation.setAccuracy(accuracy);

                wifiLocation.setTime(System.currentTimeMillis());
                wifiLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                wifiLocation.setAccuracy(accuracy);

                locationManager.setTestProviderLocation(MY_GPS_PROVIDER, gpsLocation);
                locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, gpsLocation);
                locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, wifiLocation);
            }

        }

        if(timer != null){
            Log.d("DBG", "SETTING MOCK");
            timer.cancel();
            timer.start();
        }
    }

    public void updateLocation(String bestProvider){
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED && locationManager.getProvider(MY_GPS_PROVIDER) != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {

           if(useBestProvider()){
               Location bestLocation = new Location(bestProvider);

               if(isWalking){
                   bestLocation.setLatitude(latitude);
                   bestLocation.setLongitude(longitude);

                   latitude = latitude - .0002;
               }else{
                   bestLocation.setLatitude(latitude);
                   bestLocation.setLongitude(longitude);
               }

               bestLocation.setTime(System.currentTimeMillis());
               bestLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
               bestLocation.setAccuracy(accuracy);

               locationManager.setTestProviderLocation(MY_GPS_PROVIDER, bestLocation);
               locationManager.setTestProviderLocation(bestProvider, bestLocation);
           }else {
               Location gpsLocation = new Location(LocationManager.GPS_PROVIDER);
               Location wifiLocation = new Location(LocationManager.NETWORK_PROVIDER);

               if(isWalking){
                   gpsLocation.setLatitude(latitude);
                   gpsLocation.setLongitude(longitude);

                   wifiLocation.setLatitude(latitude);
                   wifiLocation.setLongitude(latitude);
                   latitude = latitude - .0002;
               }else{
                   gpsLocation.setLatitude(latitude);
                   gpsLocation.setLongitude(longitude);

                   wifiLocation.setLatitude(latitude);
                   wifiLocation.setLongitude(latitude);
               }

               gpsLocation.setTime(System.currentTimeMillis());
               gpsLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
               gpsLocation.setAccuracy(accuracy);

               wifiLocation.setTime(System.currentTimeMillis());
               wifiLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
               wifiLocation.setAccuracy(accuracy);

               locationManager.setTestProviderLocation(MY_GPS_PROVIDER, gpsLocation);
               locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, gpsLocation);
               locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, wifiLocation);
           }

        }else{
            initLocation();
        }

        if(timer != null){
            Log.d("DBG", "SETTING MOCK");
            timer.cancel();
            timer.start();
        }
    }

    public boolean useBestProvider(){
        return true;
    }

    /**
     * Check if mock location is enabled on developer options.
     *
     * @return true if mock location is enabled else it returns false.
     */
    public static boolean canMockLocation(@NonNull Context context) {
        boolean isEnabled = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                isEnabled = opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(),
                        BuildConfig.APPLICATION_ID) == AppOpsManager.MODE_ALLOWED;
            } else {
                return !Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
            }
        } catch (Exception e) {
            Log.d("DBG", "Mock location is not enabled.");
        }
        return isEnabled;
    }

    LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("DBG", "GPS LOCATION CHANGE: " + location);
//            if(!isWalking && !location.isFromMockProvider()){
//                updateLocation();
//            }
//            if(!isWalking && location.getAccuracy() > 20){
//                accuracy = location.getAccuracy()/2;
//                updateLocation();
//            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d("DBG", "GPS STATUS CHANGE: " + s);
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    LocationListener wifiLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("DBG", "WIFI LOCATION CHANGE: " + location);
//            if(!isWalking && !location.isFromMockProvider()){
//                updateLocation();
//            }
//            if(!isWalking && location.getAccuracy() > 20){
//                accuracy = location.getAccuracy()/2;
//                updateLocation();
//            }

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d("DBG", "WIFI STATUS CHANGE: " + s);
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    LocationListener mockLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("DBG", "MOCK LOCATION CHANGE: " + location);
//            if(!isWalking && !location.isFromMockProvider()){
//                updateLocation();
//            }
//            if(!isWalking && location.getAccuracy() > 20){
//                accuracy = location.getAccuracy()/2;
//                updateLocation();
//            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d("DBG", "MOCK STATUS CHANGE: " + s);
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
}