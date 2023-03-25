package com.example.realquestion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DatabaseReference Database;
    private PollsAdapter adapter;
    private ProgressBar progressBar;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Пользователь уже авторизован, выполните нужные действия
        } else {
            Intent authIntent = new Intent(MainActivity.this, auth.class);
            startActivity(authIntent);
            finish();
        }
        Database = FirebaseDatabase.getInstance("https://sign-12108a-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        List<poll> Polls = new ArrayList<>();
        adapter = new PollsAdapter(Polls);
        ValueEventListener postListen = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Polls.clear();

                for(DataSnapshot ds : snapshot.getChildren()) {
                    poll Poll = ds.getValue(poll.class);
                    Poll.id = ds.getKey();
                    Polls.add(Poll);
                }
                adapter.notifyItemRangeChanged(0,adapter.getItemCount());
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        Database.child("polls").addValueEventListener(postListen);
        recyclerView = findViewById(R.id.rec_list);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar.setVisibility(View.INVISIBLE);
    }
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, auth.class);
            startActivity(intent);
            finish();
        }
    }

}

