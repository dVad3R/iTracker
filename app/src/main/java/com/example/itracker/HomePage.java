package com.example.itracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import in.unicodelabs.kdgaugeview.KdGaugeView;

public class HomePage extends AppCompatActivity implements LocationListener{

    private LocationManager lm;
    private static final int reqCode = 23;
    private double prevSpeed;
    private KdGaugeView accelerometer;
    private String user_id;
    private long oldTime;
    private Button maps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        accelerometer = findViewById(R.id.speedMeter);
        maps = findViewById(R.id.mapsBtn);

        maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    public void beginTracking(android.view.View v) {
        // If permission is not granted
        if (ActivityCompat.checkSelfPermission(HomePage.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Ask for permission
            ActivityCompat.requestPermissions(HomePage.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, reqCode);
        }
        // If permission is granted
        else
        {
            // Request location updates
            lm.requestLocationUpdates(lm.GPS_PROVIDER, 0, 50, HomePage.this);
        }


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // convert speed to km/h
        double newSpeed = location.getSpeed() * 3.6;
        // get current time
        long newTime = System.currentTimeMillis();
        // calc distance/time
        long dt = (newTime - oldTime) / 3600;
        // set accelerometer speed
        accelerometer.setSpeed((float) newSpeed);
        // calculate acceleration
        double acceleration = calculateAcceleration((newSpeed - prevSpeed), dt);

        // recognize acceleration above 20 km/h
        if(acceleration >= 20)
        {
            // insert event in db
            updateDatabase("Acceleration", acceleration, new Timestamp(System.currentTimeMillis()).toString(),
                    location.getLatitude(), location.getLongitude(), newSpeed, user_id);

            // get new variables
            prevSpeed = newSpeed;
            oldTime = newTime;
        }
        // recognize deceleration
        else if (acceleration < -20)
        {
            // Register event
            updateDatabase("Deceleration", acceleration, new Timestamp(System.currentTimeMillis()).toString(),
                    location.getLatitude(), location.getLongitude(), newSpeed, user_id);
            // get new variables
            prevSpeed = newSpeed;
            oldTime = newTime;
        }
        else
        {
            // Update variables
            prevSpeed = newSpeed;
            oldTime = newTime;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // check if permission is granted
        if (requestCode == reqCode && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            // check again
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                // request for location updates
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 50, HomePage.this);
            }
        }
    }

    // update db and insert new events
    public void updateDatabase(String event_type, double acceleration, String timestamp, double latitude, double longitude, double currentSpeed, String user_id)
    {
        // events collection
        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("events");
        // create hashmap object to register events
        Map<String, Object> event = new HashMap<>();

        event.put("user_id", user_id);

        event.put("event_type", event_type);

        event.put("acceleration", acceleration);

        event.put("timestamp", timestamp);

        event.put("latitude", latitude);

        event.put("longitude", longitude);

        event.put("current_speed", currentSpeed);

        collectionReference.add(event);
    }

    // method to calculate acceleration
    public double calculateAcceleration(double speedDiff,double timeDiff)
    {
        if(speedDiff == 0 || timeDiff == 0)
        {
            // Return 0
            return 0.0;
        }
        return speedDiff / timeDiff;
    }
}