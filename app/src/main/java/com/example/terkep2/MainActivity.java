package com.example.terkep2;

import android.Manifest;
import android.content.LocusId;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    SupportMapFragment supportMapFragment;

    FusedLocationProviderClient fusedLocationProviderClient;

    List<LatLng> busStopLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        fusedLocationProviderClient = (FusedLocationProviderClient) LocationServices.getFusedLocationProviderClient(this);

        busStopLocations = new ArrayList<>();
        busStopLocations.add(new LatLng(46.523367, 24.598844));
        busStopLocations.add(new LatLng(46.5434832, 24.5338982));
        busStopLocations.add(new LatLng(46.5461918, 24.5531005));
        busStopLocations.add(new LatLng(46.5395414, 24.5447104));
        busStopLocations.add(new LatLng(46.537588, 24.5474658));
        busStopLocations.add(new LatLng(46.5329167, 24.5177478));
        busStopLocations.add(new LatLng(46.533441, 24.5314163));
        busStopLocations.add(new LatLng(46.5334121, 24.5281571));
        busStopLocations.add(new LatLng(46.5376421, 24.4692577));
        busStopLocations.add(new LatLng(46.5347231, 24.546086));

        fusedLocationPermissionCheck();

        SearchView searchView = findViewById(R.id.mapSearch);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Keresési logika
                Geocoder geocoder = new Geocoder(MainActivity.this);
                try {
                    List<Address> addressList = geocoder.getFromLocationName(query, 1);
                    if (addressList != null && !addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        LatLng location = new LatLng(address.getLatitude(), address.getLongitude());

                        // Helyezd el a térképen a megtalált helyet
                        placeMarker(location, "Searched Location");
                    } else {
                        Toast.makeText(MainActivity.this, "Nem található ilyen hely", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Keresési szöveg változásának kezelése
                return true;
            }
        });


        Dexter.withContext(getApplicationContext()).withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        getCurrentLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
        showBusStopLocation();
    }

    private void placeMarker(LatLng location, String searchedLocation) {
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .title(searchedLocation)  // A helyes változót használd itt
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                googleMap.addMarker(markerOptions);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            }
        });
    }

    private void showBusStopLocation() {
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                int busStopCounter = 1;  // Ezt használjuk a buszmegállók számozásához
                for (LatLng busStopLocation : busStopLocations) {
                    String busStopTitle = "Bus Stop " + busStopCounter;
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(busStopLocation)
                            .title(busStopTitle)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    googleMap.addMarker(markerOptions);
                    busStopCounter++;
                }
            }
        });
    }

    private void fusedLocationPermissionCheck() {
        // Check for location permission
        ActivityCompat.requestPermissions(
                this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                1
        );

        getCurrentLocation();
        showBusStopLocation();
    }

    public void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d("MyApp", "Location requested");
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull GoogleMap googleMap) {
                        if(location != null){
                            Log.d("MyApp", "Location received: " + location.getLatitude() + ", " + location.getLongitude());
                            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current Location !");
                            googleMap.addMarker(markerOptions);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                        }
                        else{
                            Log.d("MyApp", "Location is null");
                            Toast.makeText(MainActivity.this,"Please on your Location App Permissions",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}