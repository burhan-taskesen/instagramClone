package com.burhantaskesen.myinstagramclonejava;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.burhantaskesen.myinstagramclonejava.databinding.ActivityFeedBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FeedActivity extends AppCompatActivity {

    private ActivityFeedBinding binding;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;
    FirebaseAuth firebaseAuth;
    StorageReference storageReference;

    ArrayList<Post> arrayList;
    myAdapter recycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        arrayList = new ArrayList<>();

        binding.recycle.setLayoutManager(new LinearLayoutManager(this));
        recycleAdapter = new myAdapter(arrayList);
        binding.recycle.setAdapter(recycleAdapter);
        getData();
    }

    private void getData() {

        firebaseFirestore.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    tost(error.getLocalizedMessage(),1);
                    return;
                }
                arrayList.clear();
                for(DocumentSnapshot documentSnapshot : value.getDocuments()){
                    arrayList.add(new Post((String) documentSnapshot.get("email"),(String) documentSnapshot.get("comment"),(String) documentSnapshot.get("downloadurl")));
                }

                recycleAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_post:
                startActivity(new Intent(this,NewPost.class));
                break;
            case R.id.sign_out:
                firebaseAuth.signOut();
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void tost(Object girdi,int length){
        Toast.makeText(getApplicationContext(),"" + girdi,length).show();
    }
}