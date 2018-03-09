package com.example.nathapong.localfood;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 99;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 10;
    private Location myLastLocation;
    private GoogleApiClient myGoogleApiClient;
    private LocationRequest myLocationRequest;

    double latitude, longitude;

    private static int UPDATE_INTERVAL = 3000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private Marker currentLocationMarker;

    TextView txtDistance;
    Button btnGetDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        txtDistance = (TextView)findViewById(R.id.txtDistance);
        btnGetDistance = (Button)findViewById(R.id.btnGetDistance);

        btnGetDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Location desLocation = new Location("dest");
                desLocation.setLatitude(13.877108);
                desLocation.setLongitude(100.411297);

               float distance = getDistance(myLastLocation, desLocation);

                txtDistance.setText(distance + " KM.");
            }
        });


        // Call setUpLocation() inside onMapReady() Method
    }

    @Override
    public void onRequestPermissionsResult
            (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE :
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if (checkPlayService()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }
    }

    private void setUpLocation(){

        if (android.support.v4.app.ActivityCompat.checkSelfPermission(
                MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && android.support.v4.app.ActivityCompat.checkSelfPermission(
                MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestRuntimePermission();
        }
        else {
            if (checkPlayService()){

                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void displayLocation(){
        if (android.support.v4.app.ActivityCompat.checkSelfPermission(
                MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && android.support.v4.app.ActivityCompat.checkSelfPermission(
                MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            return;
        }

        myLastLocation = LocationServices.FusedLocationApi.getLastLocation(myGoogleApiClient);

        if (myLastLocation != null){
            latitude = myLastLocation.getLatitude();
            longitude = myLastLocation.getLongitude();
        }

        if (currentLocationMarker != null){
            currentLocationMarker.remove();
        }

        LatLng currentLocation = new LatLng(latitude, longitude);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLocation);
        markerOptions.title("You are here!");
        markerOptions.snippet("Hello");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        currentLocationMarker = mMap.addMarker(markerOptions);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));

    }

    private void createLocationRequest(){

        myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(UPDATE_INTERVAL);
        myLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        myLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient(){

        myGoogleApiClient = new GoogleApiClient.Builder(MapsActivity.this)
                .addConnectionCallbacks(MapsActivity.this)
                .addOnConnectionFailedListener(MapsActivity.this)
                .addApi(LocationServices.API).build();
        myGoogleApiClient.connect();
    }

    private boolean checkPlayService(){

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable
                (MapsActivity.this);

        if (resultCode != ConnectionResult.SUCCESS){

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){

                GooglePlayServicesUtil.getErrorDialog(resultCode, MapsActivity.this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else {
                Toast.makeText(MapsActivity.this,
                        "This device can't support", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void requestRuntimePermission(){

        android.support.v4.app.ActivityCompat.requestPermissions(MapsActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
    }
    private void startLocationUpdate(){

        if (android.support.v4.app.ActivityCompat.checkSelfPermission(
                MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && android.support.v4.app.ActivityCompat.checkSelfPermission(
                MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates
                (myGoogleApiClient, myLocationRequest, MapsActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setUpLocation();
    }

    @Override
    public void onLocationChanged(Location location) {

        myLastLocation = location;
        displayLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {

        myGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public float getDistance(Location currentLocation, Location desLocation){

        float distance = currentLocation.distanceTo(desLocation)/1000;

        distance = Float.parseFloat(String.format("%.2f", distance));

        return distance;
    }
}
