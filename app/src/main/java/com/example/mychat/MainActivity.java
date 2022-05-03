package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private EditText mgetPhoneNumber;
    private android.widget.Button msendotp;
    private CountryCodePicker mcountryCodePicker;
    private String countryCode;
    private String phoneNumber;
    private FirebaseAuth firebaseAuth;
    private ProgressBar mprogressBar;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String codeSent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mcountryCodePicker=findViewById(R.id.countryCodePicker);
        msendotp=findViewById(R.id.sendOTPButton);
        mgetPhoneNumber=findViewById(R.id.getPhoneNumber);
        mprogressBar=findViewById(R.id.progressBarofMain);
        firebaseAuth=FirebaseAuth.getInstance();
        countryCode=mcountryCodePicker.getSelectedCountryCodeWithPlus();
        mcountryCodePicker.setOnCountryChangeListener(() -> countryCode=mcountryCodePicker.getSelectedCountryCodeWithPlus());
        msendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number;
                number= mgetPhoneNumber.getText().toString();
                if(number.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please Enter Your Mobile Number", Toast.LENGTH_SHORT).show();
                }
                else if(number.length()<10){
                    Toast.makeText(getApplicationContext(), "Please Enter A Valid Mobile Number", Toast.LENGTH_SHORT).show();
                }
                else{
                    mprogressBar.setVisibility(View.VISIBLE);
                    phoneNumber=countryCode+number;
                    PhoneAuthOptions options= PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(MainActivity.this)
                            .setCallbacks(mCallbacks)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });
        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                //how to automatically fetch code from OTP
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(getApplicationContext(), "OTP is Sent", Toast.LENGTH_SHORT).show();
                mprogressBar.setVisibility(View.INVISIBLE);
                codeSent=s;
                Intent intent= new Intent(MainActivity.this, OTPAuth.class);
                intent.putExtra("otp", codeSent);
                startActivity(intent);
            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            Intent intent = new Intent(MainActivity.this,ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}