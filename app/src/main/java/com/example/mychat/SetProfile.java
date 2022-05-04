package com.example.mychat;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SetProfile extends AppCompatActivity {
    private CardView mgetuserimage;
    private ImageView mgetuserimageinimageview;
    private static int PICK_IMAGE=123;
    private Uri imagepath;// use result instead
    private EditText mgetusername;
    private android.widget.Button msaveprofilebtn;
    private FirebaseAuth firebaseAuth;
    private String name;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private String ImageUriAccessToken;
    private FirebaseFirestore firebaseFirestore;
    private ProgressBar mprogressbarofsetprofile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);
        firebaseAuth= FirebaseAuth.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();

        mgetusername=findViewById(R.id.getusername);
        mgetuserimage=findViewById(R.id.getuserimage);
        mgetuserimageinimageview=findViewById(R.id.getuserimageinimageview);
        msaveprofilebtn=findViewById(R.id.saveProfileBtn);
        mprogressbarofsetprofile=findViewById(R.id.progressBarOfSetProfile);



        mgetuserimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
              startActivityForResult(intent,PICK_IMAGE);
            }
        });
        msaveprofilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name= mgetusername.getText().toString();
                if(name.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please Enter Your Name", Toast.LENGTH_SHORT).show();
                }
                else if(imagepath==null){
                    Toast.makeText(getApplicationContext(), "Set Your Image", Toast.LENGTH_SHORT).show();
                }
                else{
                    mprogressbarofsetprofile.setVisibility(View.VISIBLE);
                    sendDataForNewUser();
                    mprogressbarofsetprofile.setVisibility(View.INVISIBLE);
                    Intent intent= new Intent(SetProfile.this,ChatActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }
    //code for firebase
    private void sendDataForNewUser(){
        sendDataToRealTimeDatabase();

    }
    private void sendDataToRealTimeDatabase(){
        name=mgetusername.getText().toString().trim();
        FirebaseDatabase firebaseDatabase= FirebaseDatabase.getInstance();
        DatabaseReference databaseReference=firebaseDatabase.getReference(firebaseAuth.getUid());
        UserProfile userProfile= new UserProfile(name,firebaseAuth.getUid());
        databaseReference.setValue(userProfile);
        Toast.makeText(getApplicationContext(), "User Profile added Successfully !", Toast.LENGTH_SHORT).show();
        sendImagetoStorage();
    }
    private void sendImagetoStorage(){
        StorageReference imageref= storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Picture");
        //Image compression
        Bitmap bitmap = null;
        try{
            bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),imagepath);
        }  catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream);
        byte[] data= byteArrayOutputStream.toByteArray();
        //put image on database storage

        UploadTask uploadTask= imageref.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageUriAccessToken= uri.toString();
                        Toast.makeText(getApplicationContext(), "URI get Success !", Toast.LENGTH_SHORT).show();
                        sendDataToCloudFirestore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "URI get Failed !", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(getApplicationContext(), "Image is Uploaded !", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Image not Uploaded !", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendDataToCloudFirestore() {

        DocumentReference documentReference= firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String,Object>  userdata= new HashMap<>();
        userdata.put("name",name);
        userdata.put("image",ImageUriAccessToken);
        userdata.put("uid",firebaseAuth.getUid());
        userdata.put("status","online");
        documentReference.set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Data on Cloud FireStore", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK){
            assert data != null;
            imagepath= data.getData();
            mgetuserimageinimageview.setImageURI(imagepath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}