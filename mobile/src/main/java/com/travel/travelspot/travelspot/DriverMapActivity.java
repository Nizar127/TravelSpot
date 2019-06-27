package com.travel.travelspot.travelspot;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;
import java.io.BufferedInputStream;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    //oogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button mLogout,mSettings, mRideStatus, mHistory;;
    private Switch mWorkingSwitch;
    private int status = 0;
    private String customerId = "", destination;
    private LatLng destinationLatLng, pickupLatLng;
    private float rideDistance;
    private Boolean isLoggingOut = false;

    private SupportMapFragment mapFragment;
    private LinearLayout mCustomerInfo;
    private CircleImageView mCustomerProfileImage;
    private TextView mCustomerName, mCustomerPhoneNum, mCustomerPackageChosen, mCustomerTourPeriod, mCustomerTourPlan, mCustomerTourPrice, mCustomerDestination;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[](android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE);
//        }else{
//                mapFragment.getMapAsync(this);
//                //this comment line below are probable solution for FusedLocationApi underneath as it is deprecated
//                //but since it shows no error, it could be logic error
//            }
        /*mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);*/

        mCustomerInfo = (LinearLayout) findViewById(R.id.customerInfo);
        mCustomerProfileImage = (CircleImageView) findViewById(R.id.customerProfileImage);
        mCustomerName = (TextView) findViewById(R.id.customerName);
        mCustomerPhoneNum = (TextView) findViewById(R.id.customerPhoneNum);
        mCustomerPackageChosen = (TextView) findViewById(R.id.customerPackageChosen);
        mCustomerTourPeriod = (TextView) findViewById(R.id.customerTourPeriod);
        mCustomerTourPlan = (TextView) findViewById(R.id.customerTourPlan);
        mCustomerTourPrice = (TextView) findViewById(R.id.customerTourPrice);
        mCustomerDestination = (TextView) findViewById(R.id.customerDestination);
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    connectDriver();
                } else {
                    disconnectDriver();
                }
            }
        });
        mSettings = (Button) findViewById(R.id.guiderSettings);

        //button for settings
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMapActivity.this, DriverSettingsActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        //for request
        mLogout = (Button) findViewById(R.id.Logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingOut = true;
                disconnectGuider();
                FirebaseAuth.getInstance().signOut();  //signout
                //then go back to main activity
                Intent intent = new Intent(DriverMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mRideStatus = (Button) findViewById(R.id.rideStatus);
        mHistory = (Button) findViewById(R.id.history);
        mRideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (status) {
                    case 1:
                        status = 2;
                        erasePolylines();
                        if (destinationLatLng.latitude != 0.0 && destinationLatLng.longitude != 0.0) {
                            getRouteToMarker(destinationLatLng);
                        }
                        mRideStatus.setText("drive completed");

                        break;
                    case 2:
                        recordRide();
                        endRide();
                        break;
                }
            }
        });
        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMapActivity.this, HistoryActivity.class);
                intent.putExtra("customerOrDriver", "Drivers");
                startActivity(intent);
                return;
            }
        });
        getAssignedCustomer();
    }
       //underneath is the code for instance mFusedLocationClient


//       mFusedLocationClient.getLastLocation()
//               .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                   @Override
//                   public void onSuccess(Location location) {
//                       // Got last known location. In some rare situations, this can be null.
//                       if (location != null) {
//                           // Logic to handle location object
//                       }
//                   }
//               });
     //  getAssignedCustomer();


    private void getAssignedCustomer() {
        String guiderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Guider").child(guiderId).child("customerRequest").child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    status = 1;
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                    getAssignedCustomerDestination();
                    getAssignedCustomerInfo();
//                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
//                    if(map.get("CustomerRideId") != null);{
//                        customerId = map.get("CustomerRideId").toString(); //get the customer id that going to ride
//                        getAssignedCustomerPickupLocation();
//                    }
                } else {
                    endRide();
                }

//                    customerId = ""; // set to null once request cancel
//                    if(pickupMarker != null){
//                        pickupMarker.remove();
//                    }
//                    if(assignedCustomerPickupLocationRefListener != null) {
//                        assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
//                    }
//                    mCustomerInfo.setVisibility(View.GONE);
//                    mCustomerName.setText("");
//                    mCustomerPhoneNum.setText("");
//                    mCustomerPackageChosen.setText("");
//                    mCustomerTourPlan.setText("");
//                    mCustomerTourPeriod.setText("");
//                    mCustomerTourPrice.setText("");
//                    mCustomerDestination.setText("Destination: --");
//                    mCustomerProfileImage.setImageResource(R.mipmap.user);
//                }
//            }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    Marker pickupMarker;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;
    private void getAssignedCustomerPickupLocation() {
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("1");
        assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){ //once obtain the latlng, change it string
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat, locationLng);
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.parking_sensor)));
                    getRouteToMarker(pickupLatLng);
                    }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRouteToMarker(LatLng pickupLatLng) {
        if (pickupLatLng != null && mLastLocation != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                    .build();
            routing.execute();
        }
    }


    private void getAssignedCustomerDestination() {
        String guiderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Guider").child(guiderId).child("CustomerRequest").child("destination");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("destination") != null) {
                        destination = map.get("destination").toString();
                        mCustomerDestination.setText("Destination: " + destination);
//
                    } else {
                        mCustomerDestination.setText("Destination: ");
                    }

                    Double destinationLat = 0.0;
                    Double destinationLng = 0.0;
                    if (map.get("destinationLat") != null) {
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if (map.get("destinationLng") != null) {
                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
                        destinationLatLng = new LatLng(destinationLat, destinationLng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    private void getAssignedCustomerInfo(){
        //change linearlayout info to visible as initially it has been set to gone
        mCustomerInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mCustomerName.setText(map.get("name").toString());


                    }
                    if(map.get("phone")!=null){
                        mCustomerPhoneNum.setText(map.get("phone").toString());

                    }
                    if(map.get("period")!=null){
                        mCustomerTourPeriod.setText(map.get("period").toString());

                    }
                    if(map.get("Tour_Plan")!=null){
                        mCustomerTourPlan.setText(map.get("Tour_Plan").toString());

                    }
                    if(map.get("package")!=null){
                        mCustomerPackageChosen.setText(map.get("package").toString());

                    }
                    if(map.get("price")!=null){
                        mCustomerTourPrice.setText(map.get("price").toString());

                    }
                    if(map.get("profileImageUrl")!=null){
                        Glide.with(getApplication()).load(map.get("mProfileImageUrl").toString()).into(mCustomerProfileImage);


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void endRide(){
        mRideStatus.setText("picked customer");
        erasePolylines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
        driverRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerId);
        customerId="";
        rideDistance = 0;

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (assignedCustomerPickupLocationRefListener != null){
            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
        }
        mCustomerInfo.setVisibility(View.GONE);
        mCustomerName.setText("");
        mCustomerPhoneNum.setText("");
        mCustomerDestination.setText("Destination: --");
        mCustomerTourPeriod.setText(" " + "hours");
        mCustomerTourPlan.setText("");
        mCustomerPackageChosen.setText("Package: ");
        mCustomerProfileImage.setImageResource(R.mipmap.ic_default_user);
        mCustomerTourPrice.setText("Price:");
    }

    private void recordRide(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("history");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        String requestId = historyRef.push().getKey();
        driverRef.child(requestId).setValue(true);
        customerRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("driver", userId);
        map.put("customer", customerId);
        map.put("rating", 0);
        map.put("timestamp", getCurrentTimestamp());
        map.put("destination", destination);
        map.put("location/from/lat", pickupLatLng.latitude);
        map.put("location/from/lng", pickupLatLng.longitude);
        map.put("location/to/lat", destinationLatLng.latitude);
        map.put("location/to/lng", destinationLatLng.longitude);
        map.put("distance", rideDistance);
        historyRef.child(requestId).updateChildren(map);
    }

    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                checkLocationPermission();
            }
        }
    }

//    LocationCallback mLocationCallback = new LocationCallback(){
//
//        @Override
//        public void onLocationResult(LocationResult locationResult) {
//            for(Location location : locationResult.getLocations()) {
//                if (getApplicationContext() != null) {
//
//                    if (!customerId.equals("") && mLastLocation != null && location != null) {
//                        rideDistance += mLastLocation.distanceTo(location) / 1000;
//                    }
//                    mLastLocation = location;
//                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
//
//                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
//                    DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
//                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
//                    GeoFire geoFireWorking = new GeoFire(refWorking);
//
//                    switch (customerId) {
//                        case "":
//                            geoFireWorking.removeLocation(userId);
//                            geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
//                            break;
//
//                        default:
//                            geoFireAvailable.removeLocation(userId);
//                            geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
//                            break;
//                    }
//                }
//
//            }
//
//    };

        LocationCallback mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location : locationResult.getLocations()){
                    if(getApplicationContext()!=null){

                        if(!customerId.equals("") && mLastLocation!=null && location != null){
                            rideDistance += mLastLocation.distanceTo(location)/1000;
                        }
                        mLastLocation = location;


                        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
                        DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
                        GeoFire geoFireAvailable = new GeoFire(refAvailable);
                        GeoFire geoFireWorking = new GeoFire(refWorking);

                        switch (customerId){
                            case "":
                                geoFireWorking.removeLocation(userId);
                                geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                                break;

                            default:
                                geoFireAvailable.removeLocation(userId);
                                geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                                break;
                        }
                    }
                }
            }
        };

        private void checkLocationPermission(){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    new AlertDialog.Builder(this)
                            .setTitle("give permission")
                            .setMessage("give permission message")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                                }
                            })
                            .create()
                            .show();
                }
                else{
                    ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        }

                        @Override
                        public void onRequestPermissionsResult ( int requestCode,
                        @NonNull String[] permissions, @NonNull int[] grantResults){
                            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                            switch (requestCode) {
                                case 1: {
                                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                                            mMap.setMyLocationEnabled(true);
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                                    }
                                    break;
                                }
                            }
                        }



                        private void connectDriver () {
                            checkLocationPermission();
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                            mMap.setMyLocationEnabled(true);
                        }

                        private void disconnectDriver () {
                            if (mFusedLocationClient != null) {
                                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                            }
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("guidersAvailable");

                            GeoFire geoFire = new GeoFire(ref);
                            geoFire.removeLocation(userId);
                        }


                        private List<Polyline> polylines;
                        private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
                        //@Override
                        public void onRoutingFailure (RouteException e){
                            if (e != null) {
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                        //@Override
                        public void onRoutingStart () {
                        }
                        //@Override
                        public void onRoutingSuccess (ArrayList < Route > route,
                        int shortestRouteIndex){
                            if (polylines.size() > 0) {
                                for (Polyline poly : polylines) {
                                    poly.remove();
                                }
                            }

                            polylines = new ArrayList<>();
                            //add route(s) to the map.
                            for (int i = 0; i < route.size(); i++) {

                                //In case of more than 5 alternative routes
                                int colorIndex = i % COLORS.length;

                                PolylineOptions polyOptions = new PolylineOptions();
                                polyOptions.color(getResources().getColor(COLORS[colorIndex]));
                                polyOptions.width(10 + i * 3);
                                polyOptions.addAll(route.get(i).getPoints());
                                Polyline polyline = mMap.addPolyline(polyOptions);
                                polylines.add(polyline);

                                Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
                            }

                        }
                        @Override
                        public void onRoutingCancelled () {
                        }
                        private void erasePolylines () {
                            for (Polyline line : polylines) {
                                line.remove();
                            }
                            polylines.clear();
                        }

                    }

