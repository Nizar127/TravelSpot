package com.travel.travelspot.travelspot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverSettingsActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField, mTourPeriod, mCarField, mTourPlanProvide, mPackageProvide;

    private TextView  mTourPrice;

    private Button mBack, mConfirm;

    private CircleImageView mProfileimage;

    private String data;

    private FirebaseAuth mAuth;

    private DatabaseReference mDriverDatabase;

    private String userID;
    private String mName;
    private String mPhone;
    private String mPrice;
    private String mPeriod;
    private String mPackage;
    private String mPlan;
    private String mCar;
    private String mProfileImageUrl;
    private Uri resultUri;
    //private Boolean mTourPlan = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_settings);

        mProfileimage = (CircleImageView)findViewById(R.id.guiderProfileImage);

        mNameField = (EditText)findViewById(R.id.custname);
        mPhoneField = (EditText)findViewById(R.id.phone);
        mTourPeriod = (EditText) findViewById(R.id.tourPeriod);
        mCarField = (EditText)findViewById(R.id.car);

        mPackageProvide = (EditText)findViewById(R.id.packageProvide);
        mTourPlanProvide = (EditText)findViewById(R.id.tourPlanedProvide);

        mTourPrice = (TextView)findViewById(R.id.tourPrice);

        mBack = (Button)findViewById(R.id.back);
        mConfirm = (Button)findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid(); //get current user
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);

        getUserInfo();

        mProfileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK); // set an image
                intent.setType("image/*"); //set the type that only image can be set to this profile
                startActivityForResult(intent, 1); //this is for taking image from gallery

            }

        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });


    }
    private void getUserInfo(){
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mName = map.get("name").toString();
                        mNameField.setText(mName);

                    }
                    if(map.get("phone")!=null){
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);

                    }
                    if(map.get("car")!=null){
                        mCar = map.get("car").toString();
                        mCarField.setText(mCar);

                    }
                    if(map.get("period")!=null){
                        mPeriod = map.get("period").toString();
                        mTourPeriod.setText(mPeriod);

                    }
                    if(map.get("package")!=null){
                        mPackage = map.get("package").toString();
                        mPackageProvide.setText(mPackage);

                    }
                    if(map.get("Tour_Plan")!=null){
                        mPlan = map.get("Tour_Plan").toString();
                        mTourPlanProvide.setText(mPlan);

                    }
                    if(map.get("price")!=null){
                        mPrice = map.get("price").toString();
                        mTourPrice.setText(mPrice);

                    }
                    if(map.get("profileImageUrl")!=null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(mProfileimage);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void saveUserInformation() {
        mName = mNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        mPrice = mTourPrice.getText().toString();

        Map userInfo = new HashMap(); //save it to map
        userInfo.put("name",mName);
        userInfo.put("phone",mPhone);
        userInfo.put("period",mPeriod);
        userInfo.put("car",mCar);
        userInfo.put("price",mPrice);
        mDriverDatabase.updateChildren(userInfo);

        if(resultUri != null) {
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("Profile_Images").child(userID); //store the image of the user in here // soon will be store in AWS S3
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //this code is part to upload it from memory
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            final UploadTask uploadTask = filePath.putBytes(data); //later changes to putFile() or putStream();

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //below this wrong code, it is deprecated
                    //it is line to download the photo uploaded

                    //Uri downloadUrl = uploadTask.getResult().getMetadata().getReference().getDownloadUrl().toString();  ///fix this soon
                    //returns this once it has been uploaded..we going to do it differently
                    taskSnapshot.getMetadata(); //to obtain MIME type of image


                    //if above not working..use this code below
                    //get download url

                    final StorageReference ref = filePath.child("Profile_Images");
                    //final UploadTask data = data.child("");
                    uploadTask = ref.putBytes(data); //this is need to be fix

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return ref.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {//put it into map
                                Uri downloadUri = task.getResult();
                                Map newImage = new HashMap();
                                newImage.put("profileImageUrl", downloadUri.toString()); //to download n store image on circleimageview
                                mDriverDatabase.updateChildren(newImage);

                                finish();
                                return;
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    });
                }
            });
        }else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileimage.setImageURI(resultUri);
        }
    }
}
