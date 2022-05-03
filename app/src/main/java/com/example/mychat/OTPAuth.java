package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OTPAuth extends AppCompatActivity {
    private TextView mchangeNumber;
    private EditText mgetOTP;
    private android.widget.Button mverifyotp;
    private String enterotp;
    private FirebaseAuth firebaseAuth;
    private ProgressBar mprogressBarofOtpAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpauth);
        mchangeNumber=findViewById(R.id.changeNumber);
        mverifyotp=findViewById(R.id.verifyOtp);
        mgetOTP=findViewById(R.id.getOTP);
        mprogressBarofOtpAuth=findViewById(R.id.progressBarOfOTPAuth);
        firebaseAuth=FirebaseAuth.getInstance();
        mchangeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(OTPAuth.this,MainActivity.class);
                startActivity(intent);
            }
        });
        mverifyotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterotp=mgetOTP.getText().toString();
                if(enterotp.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter Your OTP First", Toast.LENGTH_SHORT).show();
                }
                else{
                    mprogressBarofOtpAuth.setVisibility(View.VISIBLE);
                    String codeReceived= getIntent().getStringExtra("otp");
                    PhoneAuthCredential credential= PhoneAuthProvider.getCredential(codeReceived,enterotp);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mprogressBarofOtpAuth.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Login Successful !", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OTPAuth.this, SetProfile.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                        mprogressBarofOtpAuth.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Login Failed !", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}