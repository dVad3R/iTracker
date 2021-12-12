package com.example.itracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.itracker.databinding.ActivityMapsBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate variables
        com.example.itracker.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)     {
        mMap = googleMap;
        // Get instance of database
        FirebaseFirestore.getInstance()
                // Access events collection
                .collection("events")
                // Get collection's snapshot
                .get()
                // Add on complete listener
                .addOnCompleteListener(task ->
                {
                    // If retrieving the data was successful
                    if(task.isSuccessful())
                    {

                            // Fetch all events from collection
                            List<DocumentSnapshot> events = task.getResult().getDocuments();
                            // For every event
                            for(DocumentSnapshot event : events)
                            {
                                // Retrieve user id
                                String userID = (String) event.get("user_id");
                                // Retrieve event's time
                                String eventTime = (String) event.get("timestamp");
                                // Retrieve event's speed
                                Double eventSpeed = Objects.requireNonNull((Double) event.get("current_speed"));
                                // Retrieve event's acceleration
                                Double acceleration = Objects.requireNonNull((Double) event.get("acceleration"));
                                // Retrieve event's coordinate
                                LatLng eventCoordinates = new LatLng(Objects.requireNonNull((Double) event.get("latitude")),
                                        Objects.requireNonNull((Double) event.get("longitude")));
                                // If event is ACCELERATION, make marker RED
                                if(acceleration > 20)
                                {
                                    // Add new marker
                                    mMap.addMarker(new MarkerOptions()
                                            // Add marker's coordinates
                                            .position(eventCoordinates)
                                            // Add marker's title
                                            .title("Acceleration" )
                                            // Add marker's snippet
                                            .snippet("Acceleration: " + String.format(Locale.US,"%.02f", acceleration) +
                                                    "\nVehicle's speed : " + String.format(Locale.US,"%.02f", eventSpeed) +
                                                    "\nEvent's time : " + eventTime +
                                                    "\nUser : " + userID)
                                            // Add marker's icon
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                }
                                // If event is DECELERATION, make marker GREEN
                                else if(acceleration < -20)
                                {
                                    // Add new marker
                                    mMap.addMarker(new MarkerOptions()
                                            // Add marker's coordinates
                                            .position(eventCoordinates)
                                            // Add marker's title
                                            .title("Deceleration")
                                            // Add marker's snippet
                                            .snippet("Deceleration: " + String.format(Locale.US,"%.02f", acceleration) +
                                                    "\nVehicle's speed : " + String.format(Locale.US,"%.02f", eventSpeed) +
                                                    "\nEvent's time : " + eventTime +
                                                    "\nUser : " + userID)
                                            // Add marker's icon
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                }
                            }
                            // Set custom window adapter
                            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
                            {
                                // Method for setting up window contents
                                @NonNull
                                @Override
                                public View getInfoContents(@NonNull Marker marker)
                                {
                                    // Initialize variables
                                    Context context = getApplicationContext();
                                    LinearLayout info = new LinearLayout(context);
                                    // Set orientation
                                    info.setOrientation(LinearLayout.VERTICAL);
                                    // Set title
                                    TextView title = new TextView(context);
                                    // Set title's text color
                                    title.setTextColor(Color.BLACK);
                                    // Set title's gravity
                                    title.setGravity(Gravity.CENTER);
                                    // Set title's typeface
                                    title.setTypeface(null, Typeface.BOLD);
                                    // Set title's text
                                    title.setText(marker.getTitle());
                                    // Initialize snippet
                                    TextView snippet = new TextView(context);
                                    // Set snippet's text color
                                    snippet.setTextColor(Color.GRAY);
                                    // Set snippet's text
                                    snippet.setText(marker.getSnippet());
                                    // Add title to layout
                                    info.addView(title);
                                    // Add snippet to layout
                                    info.addView(snippet);
                                    // Return info
                                    return info;
                                }

                                // Method for retrieving info window
                                @Nullable
                                @Override
                                public View getInfoWindow(@NonNull Marker marker)
                                {
                                    // Return null
                                    return null;
                                }
                            });
                        }
                });
    }
}