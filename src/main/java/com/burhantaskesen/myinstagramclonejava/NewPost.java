package com.burhantaskesen.myinstagramclonejava;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.burhantaskesen.myinstagramclonejava.databinding.ActivityNewPostBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewPost extends AppCompatActivity {

    private ActivityNewPostBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    ActivityResultLauncher<String> permissionLauncher;
    ActivityResultLauncher<Intent> activityResultLauncher;
    Bitmap selectedImage;
    Uri selectedUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getReference();

        registerLaunchers();
    }

    public void func_selectImage(View view){
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view, "Permission needed",Snackbar.LENGTH_INDEFINITE).setAction("Give", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }
            else{
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        else{
            activityResultLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        }
    }

    public void registerLaunchers(){
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        selectedUri = result.getData().getData();
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                ImageDecoder.Source source = ImageDecoder.createSource(NewPost.this.getContentResolver(),selectedUri);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.ivSelectImage.setImageBitmap(selectedImage);

                            } else {
                                selectedImage = MediaStore.Images.Media.getBitmap(NewPost.this.getContentResolver(),selectedUri);
                                binding.ivSelectImage.setImageBitmap(selectedImage);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    //startActivity(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
                    activityResultLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
                }
            }
        });
    }

    public void goToFeedActivity(){
        startActivity(new Intent(this,FeedActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    public void func_addPost(View view){
        String comment = binding.etEnterComment.getText().toString();


        if(selectedUri == null || comment.equals(""))
            return;

        UUID uuid = UUID.randomUUID();
        final String imageName = "images/" + uuid + ".jpg";

        storageReference.child(imageName).putFile(selectedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                StorageReference newReference = FirebaseStorage.getInstance().getReference(imageName);
                newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        //startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(downloadUrl)));

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String userEmail = firebaseUser.getEmail();

                        HashMap<String, Object> postData = new HashMap<>();
                        postData.put("email",userEmail);
                        postData.put("downloadurl",downloadUrl);
                        postData.put("comment",comment);
                        postData.put("date", FieldValue.serverTimestamp());

                        firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                //Toast.makeText(NewPost.this,documentReference.getId(),Toast.LENGTH_LONG).show();
                                goToFeedActivity();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NewPost.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewPost.this,e.getLocalizedMessage(), Toast.LENGTH_SHORT);
            }
        });
    }

    public void func_deneme(View view){
        /*
        HashMap<String, Object> data = new HashMap<>();
        data.put("first", true);
        data.put("second", "ikinci");
        data.put("third", 3);
        firebaseFirestore.collection("Posts").document("Deneme").set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(NewPost.this,"Başarılı",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewPost.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        });
         */


        /*
        firebaseFirestore.collection("Posts").document("Deneme").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //Toast.makeText(NewPost.this,(String) documentSnapshot.get("second"),Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewPost.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        */

        /*
        firebaseFirestore.collection("Posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                //Toast.makeText(NewPost.this,(String) value.getDocumentChanges().get(0).getDocument().get("second"),Toast.LENGTH_SHORT).show();
                //tost(value.getDocuments().size(),0);

                tost(value.getDocumentChanges().get(0).getDocument().getId(),0);
            }
        });
         */


        /*
        firebaseFirestore.collection("Posts").document("33").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(),"silindi",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                tost(e.getLocalizedMessage(),0);
            }
        });
         */
    }

    public void tost(Object girdi,int length){
        Toast.makeText(getApplicationContext(),"" + girdi,length).show();
    }
}