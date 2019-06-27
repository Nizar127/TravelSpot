package com.travel.travelspot.travelspot;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback{
// GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    //GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private Button mRequest, mSettings, mHistory, mLogout;

    private LatLng pickuplocation;

    private Boolean requestCancel = false;

    private Marker pickupMarker;

    private SupportMapFragment mapFragment;

    private String destination, requestService;

    private LatLng destinationLatLng;

    private LinearLayout mGuiderInfo;

    private CircleImageView mGuiderProfileImage;


   // private ImageView mTourGuiderProfileImage;

    private TextView mGuiderName, mGuiderPhone, mGuiderCar;

    //private String destination;

    private RadioGroup mRadioGroup;

    private RatingBar mRatingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        destinationLatLng = new LatLng(0.0,0.0);

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[][android.Manifest.permission.ACCESS_FINE_LOCATION], LOCATION_REQUEST_CODE);
//        }else {
//            mapFragment.getMapAsync(this);
//        }
//        //do logout here
//        //better automation

        mGuiderInfo = (LinearLayout)findViewById(R.id.guiderInfo);
        mGuiderProfileImage =(CircleImageView) findViewById(R.id.guiderProfileImage);
        mGuiderName = (TextView)findViewById(R.id.guiderName);
        mGuiderPhone = (TextView)findViewById(R.id.guiderPhone);
        mGuiderCar = (TextView)findViewById(R.id.guiderCar);

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);

        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.sedan);

        mLogout = (Button) findViewById(R.id.logout);

        //this code below support part of cancelling tour guide
        mRequest = (Button) findViewById(R.id.Request);
        mSettings = (Button)findViewById(R.id.Settings);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestCancel) {
                    //this script will remove all request
                    requestCancel = false;
                    endRide();
                } else {
                    int selectId = mRadioGroup.getCheckedRadioButtonId();

                    final RadioButton radioButton = (RadioButton) findViewById(selectId);

                    if (radioButton.getText() == null) {
                        return;
                    }

                    requestService = radioButton.getText().toString();

                    requestCancel = true;

                   String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                   //underneath this is create customer request table that request driver based on location
                   DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");
                   GeoFire geoFire = new GeoFire(ref);
                   //get distance of the driver from onlocatiochanged function
                   geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                   String user_Id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                   DatabaseReference refer = FirebaseDatabase.getInstance().getReference("DriverAvailable");
                   GeoFire geoFireLocation = new GeoFire(refer);
                   geoFireLocation.setLocation(user_Id, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                   pickuplocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                   pickupMarker = mMap.addMarker(new MarkerOptions().position(pickuplocation).title("Pickup Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.parking_sensor)));

                   mRequest.setText("Getting Your Tour Guider");
                   //FirebaseAuth.getInstance().signout(); // for logout

                   //Intent intent = new Intent(CustomerMapActivity.this, TourGuideActivity.class);
                   //startActivity(intent);
                   //finish();
                   //we will do closest driver; first, then it will check availability
                   getClosestGuider();
               }
            }
        });
mSettings.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(CustomerMapActivity.this, CustomerSettingsActivity.class);
        startActivity(intent); //no finish
        return;
    }
});

mHistory.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(CustomerMapActivity.this, HistoryActivity.class);
        intent.putExtra("customerOrDriver", "Customers");
        startActivity(intent);
        return;
    }
});
// Initialize the AutocompleteSupportFragment.
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

// Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.

            }
        });


    }
    private int radius = 1;
    private Boolean GuiderFound = false;
    private String GuiderFoundID;

    GeoQuery geoQuery;
    private void getClosestGuider() {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("GuiderAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(pickuplocation.latitude, pickuplocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!GuiderFound && requestCancel) {
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (GuiderFound) {
                                    return;
                                }

                                if (driverMap.get("service").equals(requestService)) {
                                    GuiderFound = true;
                                    GuiderFoundID = dataSnapshot.getKey();

                                    DatabaseReference guiderRef = FirebaseDatabase.getInstance().getReference().child("Customer").child("Guider").child(GuiderFoundID).child("customerRequest");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("CustomerRideId", customerId);
                                    map.put("destination", destination);
                                    map.put("destinationLat", destinationLatLng.latitude);
                                    map.put("destinationLng", destinationLatLng.longitude);
                                    guiderRef.updateChildren(map);

                                    getGuiderLocation();
                                    getGuiderInfo();
                                    getHasTripEnded();
                                    mRequest.setText("Looking For Your Guider");
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }


            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!GuiderFound) {
                    //this will move on to more km to find guider
                    radius++;
                    getClosestGuider();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    //getGuiderLocation
    //Get's most updated driver location and it's always checking for movements.

    private Marker mGuiderMarker;
    private DatabaseReference guiderLocationRef;
    private ValueEventListener guiderLocationRefListener;
    private void getGuiderLocation() {
        guiderLocationRef = FirebaseDatabase.getInstance().getReference().child("guiderWorking").child(GuiderFoundID).child("1");
        guiderLocationRefListener = guiderLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestCancel) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    mRequest.setText("Yay! Found Your Tour Guide. Cheers");
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng guiderLatLng = new LatLng(locationLat, locationLng);
                    if (mGuiderMarker != null) {
                        mGuiderMarker.remove();
                    }
                    //calculate distance here
                    //loc1 is for driver.
                    //loc2 is for customer
                    //or vice versa
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickuplocation.latitude);
                    loc1.setLongitude(pickuplocation.longitude);


                    Location loc2 = new Location("");
                    loc2.setLatitude(guiderLatLng.latitude);
                    loc2.setLongitude(guiderLatLng.longitude);

                    //this is for calculate distance
                    //distance is the rdius of circumference
                    float distance = loc1.distanceTo(loc2);
                    //create and notify customer here
                    //if guider reach within 100 meters, it will mention arrival
                    if (distance < 100) {
                        mRequest.setText("Yoohoo! Your Guider's here. Let's Go");
                    } else {
                        mRequest.setText("Tour Guide Has Been Found. Standby: " + String.valueOf(distance));
                    }
                    //mention here
                    mRequest.setText("Driver Found: " + String.valueOf(distance));
                    mGuiderMarker = mMap.addMarker(new MarkerOptions().position(guiderLatLng).title("Your Tour Guider").icon(BitmapDescriptorFactory.fromResource(R.mipmap.van)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

//getGuiderInfo
    //get all user info that we can get from the user's database

    private void getGuiderInfo(){
        mGuiderInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(GuiderFoundID);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("name")!=null){
                        mGuiderName.setText(dataSnapshot.child("name").getValue().toString());
                    }
                    if(dataSnapshot.child("phone")!=null){
                        mGuiderPhone.setText(dataSnapshot.child("phone").getValue().toString());
                    }
                    if(dataSnapshot.child("car")!=null){
                        mGuiderCar.setText(dataSnapshot.child("car").getValue().toString());
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        Glide.with(getApplication()).load(dataSnapshot.child("profileImageUrl").getValue().toString()).into(mGuiderProfileImage);
                    }

                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        mRatingBar.setRating(ratingsAvg);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private void getHasTripEnded(){
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(GuiderFoundID).child("customerRequest").child("customerRideId");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide(){
        requestCancel = false;
        geoQuery.removeAllListeners();
        guiderLocationRef.removeEventListener(guiderLocationRefListener);
        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);

        if (GuiderFoundID != null){
            DatabaseReference guiderRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(GuiderFoundID).child("customerRequest");
            guiderRef.removeValue();
            GuiderFoundID = null;

        }
        GuiderFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (mGuiderMarker != null){
            mGuiderMarker.remove();
        }
        mRequest.setText("call Uber");

        mGuiderInfo.setVisibility(View.GONE);
        mGuiderName.setText("");
        mGuiderPhone.setText("");
        mGuiderCar.setText("Destination: --");
        mGuiderProfileImage.setImageResource(R.mipmap.ic_default_user);
    }


    //find and update user's location
    //update interval is 1000ms and accuracy is set to PRIORITY HIGH ACCURACY
    //set it to lower value if battery draining too fast
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();

                mLastLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            }
        }


        LocationCallback mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location : locationResult.getLocations()){
                    if(getApplicationContext()!=null){
                        mLastLocation = location;

                        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                        if(!getGuidersAroundStarted)
                            getGuidersAround();
                    }
                }
            }
        };


        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
       // buildGoogleApiClient(); // above setMyLocation
        //mMap.setMyLocationEnabled(true);
    }

    //get permission for this app if they did not exist before
    ///requestCode: the number assigned to the request that we've made. Each
    //             request has it's own unique request code.
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    boolean getGuidersAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();
    private void getGuidersAround() {
        getGuidersAroundStarted = true;
        DatabaseReference guiderLocation = FirebaseDatabase.getInstance().getReference().child("guiderAvailable");

        GeoFire geoFire = new GeoFire(guiderLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLongitude(), mLastLocation.getLatitude()), 999999999);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for (Marker markerIt : markers) {
                    if (markerIt.getTag().equals(key))
                        return;
                }

                LatLng guiderLocation = new LatLng(location.latitude, location.longitude);

                Marker mGuiderMarker = mMap.addMarker(new MarkerOptions().position(guiderLocation).title(key).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                mGuiderMarker.setTag(key);

                markers.add(mGuiderMarker);
            }

            @Override
            public void onKeyExited(String key) {
                for (Marker markerIt : markers) {
                    if (markerIt.getTag().equals(key)) {
                        markerIt.remove();
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker markerIt : markers) {
                    if (markerIt.getTag().equals(key)) {
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}