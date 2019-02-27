package com.travel.travelspot.travelspot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private Button mLogout,mSettings;
    private String customerId = "";
    private Boolean isLoggingOut  = false;
    private SupportMapFragment mapFragment;
    private LinearLayout mCustomerInfo;
    private CircleImageView mCustomerProfileImage;
    private TextView mCustomerName, mCustomerPhoneNum, mCustomerPackageChosen, mCustomerTourPeriod, mCustomerTourPlan, mCustomerTourPrice, mCustomerDestination;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[](android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE);
        }else{
                mapFragment.getMapAsync(this);
                //this comment line below are probable solution for FusedLocationApi underneath as it is deprecated
                //but since it shows no error, it could be logic error
            }
        /*mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);*/

        mCustomerInfo = (LinearLayout) findViewById(R.id.customerInfo);
        mCustomerProfileImage = (CircleImageView)findViewById(R.id.customerProfileImage);
        mCustomerName = (TextView)findViewById(R.id.customerName);
        mCustomerPhoneNum = (TextView)findViewById(R.id.customerPhoneNum);
        mCustomerPackageChosen = (TextView)findViewById(R.id.customerPackageChosen);
        mCustomerTourPeriod = (TextView)findViewById(R.id.customerTourPeriod);
        mCustomerTourPlan = (TextView)findViewById(R.id.customerTourPlan);
        mCustomerTourPrice = (TextView)findViewById(R.id.customerTourPrice);
        mCustomerDestination = (TextView)findViewById(R.id.customerDestination);
        mSettings = (Button)findViewById(R.id.guiderSettings);

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
       mLogout = (Button)findViewById(R.id.Logout);
       mLogout.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v){
               isLoggingOut = true;
               disconnectGuider();
               FirebaseAuth.getInstance().signOut();  //signout
               //then go back to main activity
               Intent intent = new Intent(DriverMapActivity.this, MainActivity.class);
               startActivity(intent);
               finish();
           }
       });

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
       getAssignedCustomer();
    }

    private void getAssignedCustomer() {
        String guiderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Guider").child(guiderId).child("CustomerRequest").child("CustomerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                    getAssignedCustomerDestination();
                    getAssignedCustomerInfo();
//                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
//                    if(map.get("CustomerRideId") != null);{
//                        customerId = map.get("CustomerRideId").toString(); //get the customer id that going to ride
//                        getAssignedCustomerPickupLocation();
//                    }
                }
                else{
                    customerId = ""; // set to null once request cancel
                    if(pickupMarker != null){
                        pickupMarker.remove();
                    }
                    if(assignedCustomerPickupLocationRefListener != null) {
                        assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
                    }
                    mCustomerInfo.setVisibility(View.GONE);
                    mCustomerName.setText("");
                    mCustomerPhoneNum.setText("");
                    mCustomerPackageChosen.setText("");
                    mCustomerTourPlan.setText("");
                    mCustomerTourPeriod.setText("");
                    mCustomerTourPrice.setText("");
                    mCustomerDestination.setText("Destination: --");
                    mCustomerProfileImage.setImageResource(R.mipmap.user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAssignedCustomerDestination() {
        String guiderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Guider").child(guiderId).child("CustomerRequest").child("destination");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String destination = dataSnapshot.getValue().toString();
                    mCustomerDestination.setText("Destination: " + destination);
//
                }
                else{
                    mCustomerDestination.setText("Destination: " );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Marker pickupMarker;
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
                    LatLng guiderLatLng = new LatLng(locationLat, locationLng);

                    mMap.addMarker(new MarkerOptions().position(guiderLatLng).title("Pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.parking_sensor)));
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        buildGoogleApiClient(); // above setMyLocation
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        mGoogleApiClient.connect();

    }

    @Override // for current location
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {

            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("DriverAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("DriverWorking");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);

            switch (customerId){
                case "" :
                    geoFireWorking.removeLocation(userId);
                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireWorking.removeLocation(userId);
                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }
            ;

        }

    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[](android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE);
            return;
        }
        ///please somehow changed it to FusedLocationProviderApi
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this); // fusedLocationApi is depracated..but it shows no error..help please

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void disconnectGuider(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("GuiderAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }

    //requesting permission to access GPS location
    final int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                } else {
                    Toast.makeText(getApplicationContext(), "Please Provide The Permission To Access Location: ", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();//below part is  for logging out in which if the guider logour, it will disconnect
        if(!isLoggingOut){
           disconnectGuider();
        }

    }
}
