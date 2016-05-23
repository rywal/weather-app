package edu.tamu.csce315team11.weatherapp;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ZipActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.tamu.csce315team11.weatherapp.MESSAGE";
    public int gpsZip = 0;
    public boolean reloadActivity = false;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gps_enabled) {
            Intent gps_set = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gps_set);
        }

    }

    public void backWithZip(View view){
        Intent intent= new Intent(this, MainActivity.class);
        EditText code = (EditText) findViewById(R.id.enter_zip);
        int zipcode = 0;

        try {
            zipcode = Integer.parseInt(code.getText().toString());
        } catch(NumberFormatException e) {
            zipcode = 77840;
            System.out.println(e + " is not an integer");
        }

        intent.putExtra(EXTRA_MESSAGE, zipcode);
        startActivity(intent);
    }

    public void makeUseOfNewLocation(Location location) {
        Geocoder geocoder = new Geocoder(this);
        Address address = new Address(Locale.getDefault());
        try {
            List<Address> addr = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addr.size() > 0) {
                address = addr.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Found zip: " + address.getPostalCode());
        gpsZip = Integer.parseInt(address.getPostalCode());

        if (reloadActivity) {
            Intent intent = new Intent(this, MainActivity.class);
            int zipcode = 0;
            try {
                zipcode = gpsZip;
            } catch (NumberFormatException e) {
                System.out.println(e + " is not an integer");
            }

            intent.putExtra(EXTRA_MESSAGE, zipcode);
            startActivity(intent);
        }
        reloadActivity = false;
    }

    public void backWithGPS(View view){
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        reloadActivity = true;

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }

}
