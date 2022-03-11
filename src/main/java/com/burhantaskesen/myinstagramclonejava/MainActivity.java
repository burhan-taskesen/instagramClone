package com.burhantaskesen.myinstagramclonejava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import com.burhantaskesen.myinstagramclonejava.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mStr = FirebaseStorage.getInstance();

        if(mAuth.getCurrentUser() != null)
            goToFeedActivity();
        
    }

    public void func_giris_btn(View view){
        mAuth.signInWithEmailAndPassword(binding.etEmail.getText().toString(),binding.etPassword.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        goToFeedActivity();
                        Toast.makeText(MainActivity.this,"Giriş yapıldı.",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this,"Giriş başarısız. \n" + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                        }
                });
    }

    public void func_kaydol_btn(View view){
        mAuth.createUserWithEmailAndPassword(binding.etEmail.getText().toString(),binding.etPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(MainActivity.this,"Kayıt yapıldı.",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Kayıt başarısız. \n" + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    public void goToFeedActivity(){
        startActivity(new Intent(this,FeedActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
}