package examples.aaronhoskins.com.mapandlocaton;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.internal.ClientSettings;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        PermissionManager.IPermissionManager {

    private GoogleMap mMap;
    private EditText etAddress;
    private EditText etLatLng;
    private PermissionManager manager;
    private FusedLocationProviderClient locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        bindViews();
        manager = new PermissionManager(this);
        manager.checkForPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        manager.permissionResult(requestCode, permissions, grantResults);
    }

    private void bindViews() {
        etAddress = findViewById(R.id.etAddress);
        etLatLng = findViewById(R.id.etLatLng);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // 5 different map types {none, normal, terrain, satellite, hybrid}
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMinZoomPreference(17.0f);
        LatLng sydney = new LatLng(-34, 151); //double for lat, double for longitude
        displayLocationOnMap(sydney, "Somewhere");
    }

    //Method adds a marker to the map, and moves focus to position
    private void displayLocationOnMap(final LatLng latLng, final String title) {
        mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }


    /**
     * GEOCODING
     *      2Types:
     *          Geocoding - use addresses (or unique id of places) to gain lat/lng of location
     *          Reverse Geocoding - using a lat/lng to get address (and some other info)
     */

    //Geocoing
    public LatLng getLocationByAddress(final String address) throws IOException {
        Geocoder geocoder = new Geocoder(this);
        Address addressResult = geocoder.getFromLocationName(address, 1).get(0);
        return new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
    }

    //Reverse Geocoding
    public String getAddressUsingLatLng(LatLng latLng) throws IOException {
        Geocoder geocoder = new Geocoder(this);
        Address addressResult = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
        return addressResult.getAddressLine(0);
    }

    public void onClick(View view) {
        final String address = etAddress.getText().toString();
        final String latLng = etLatLng.getText().toString();
        LatLng retrievedLatLng;
        try {
            if (!address.isEmpty()) {
                retrievedLatLng = getLocationByAddress(address);
                displayLocationOnMap(retrievedLatLng, getAddressUsingLatLng(retrievedLatLng));
            } else if (!latLng.isEmpty()) {
                String[] splitLatLngString = latLng.split(",");
                retrievedLatLng
                        = new LatLng(Double.valueOf(splitLatLngString[0]), Double.valueOf(splitLatLngString[1]));
                final String addressRetrieved = getAddressUsingLatLng(retrievedLatLng);
                displayLocationOnMap(retrievedLatLng, addressRetrieved);
            }
        }catch (IOException e) {
            Log.e("TAG", "onClick: ", e);
        }
    }

    //Getting Devices Last Know Location
    // NOTE: The return could be null IF no request for location
    //          has been executed.
    private void getLastKnownLocation() {
        locationProvider.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    final double lat = location.getLatitude();
                    final double lng = location.getLongitude();
                    displayLocationOnMap(new LatLng(lat, lng), "Last Known Location");
                } else {
                    getLocationUpdates(1);
                    getLastKnownLocation();
                }
            }
        });

    }

    private void getLocationUpdates(int numOfUpdates) {
        final LocationRequest locationRequest = getLocationRequest(numOfUpdates);
        LocationSettingsRequest settingsRequest =
                new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        SettingsClient settingsClient = new SettingsClient(this);
        settingsClient.checkLocationSettings(settingsRequest);
        locationProvider.requestLocationUpdates(locationRequest,new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLocations().get(0);
                final double lat = location.getLatitude();
                final double lng = location.getLongitude();
                Log.d("TAG_UPDATE", "onLocationResult: " + lat + " " + lng );
            }

        }, Looper.myLooper());
    }

    private LocationRequest getLocationRequest(int numOfRequest) {
        LocationRequest request = new LocationRequest();
        request.setMaxWaitTime(5);
        request.setInterval(3);
        //request.setSmallestDisplacement()
        request.setNumUpdates(numOfRequest);
        return request;
    }

    @Override
    public void onPermissionResult(boolean isGranted) {
        Toast.makeText(this,
                isGranted ? "Permission Granted" : "Permission Denied",
                Toast.LENGTH_LONG).show();
        if(isGranted) {
            locationProvider = new FusedLocationProviderClient(this);
            getLastKnownLocation();
            getLocationUpdates(100);
        }

    }


}
