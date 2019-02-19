package com.travel.travelspot.travelspot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CustomerLoginActivity extends AppCompatActivity {
    private EditText mEmail, mPassword;
    private Button mLogin, mRegister;

    //private FirebaseAuth mAuth;
    //private FirebaseAuth.AuthStateListener firebaseAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        //mAuth = FirebaseAuth.getInstance();

        //firebaseAuthListener = new FirebaseAuth.AuthStateListener();
        //@Override
        //public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        //      FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //      if(user != null){
        //          Intent intent = new Intent(CustomerLoginActivity.this, MapActivity.class);
        //          startActivity(intent);
        //            finish();
        //             return;
        //      }
        //  }
        //};
        mEmail = (EditText)findViewById(R.id.email);
        mPassword = (EditText)findViewById(R.id.password);

        mLogin = (Button) findViewById(R.id.login);
        mRegister = (Button) findViewById(R.id.register);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String password = mEmail.getText().toString();
                //mAuth.createAuthWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>(){
                //  @Override
                //  public void onComplete(@NonNull Task<Author> task){
                //      if(!task.isSuccessful()){
                //          Toast.makeText(DriverLoginActivity.this, "Sign up Error", Toast.LENGTH_SHORT.show();
                //      }else{
                //         String user_id = mAuth.getCurrentDate().getUid();
                //         DatabaseReference currnt_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(user_id);
                //         current_user_id.setValue(true);
                //      }
                // }
                //  }
            }
        });
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                //mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>(){
                // @Override
                //  public void OnComplete(@NonNull Task<AuthResult> task){
                //          if(!task.isSuccessful()){
                //          Toast.makeText(DriverLoginActivity.this, "Sign In Error", Toast.LENGTH_SHORT.show();
                //      }else{
                //         String user_id = mAuth.getCurrentDate().getUid();
                //         DatabaseReference currnt_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(user_id);
                //         current_user_id.setValue(true);
                //      }
                // });

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mAuth.addAuthStateListener(firebaseAuthListener);
    }
}


