package com.example.hiichat.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.hiichat.Adapter.SearchFriendAdapter;
import com.example.hiichat.Data.SharedPreferenceHelper;
import com.example.hiichat.Data.StaticConfig;
import com.example.hiichat.Model.User;
import com.example.hiichat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFriendActivity extends AppCompatActivity {
    private ImageView imgBack;
    private SearchView searchView;
    private RecyclerView recycleView;
    private DatabaseReference databaseReference;
    private ArrayList<User> list;
    private SearchFriendAdapter adapter;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        initView();
    }

    private void initView() {
        imgBack = (ImageView) findViewById(R.id.img_back);
        imgBack.setOnClickListener(v -> finish());
        searchView = (SearchView) findViewById(R.id.search_view);
        recycleView = (RecyclerView) findViewById(R.id.recycleView);
        list = new ArrayList<>();
        user = SharedPreferenceHelper.getInstance(this).getUserInfo();


        recycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new SearchFriendAdapter(list, this);


        databaseReference = FirebaseDatabase.getInstance().getReference().child("user");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null){
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                        list.add(dataSnapshot1.getValue(User.class));
                    }
                    for (int i = 0; i < list.size(); i++){
                        if (user.email.equals(list.get(i).email)){
                            list.remove(i);
                        }
                    }
                    makeListUsers(list);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void makeListUsers(ArrayList<User> list) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e("query", query);
                sortList(query, list);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void sortList(String query, ArrayList<User> list) {
        ArrayList<User> users = new ArrayList<>();
            for (int i = 0; i < list.size(); i++){
                User user1 = list.get(i);
                if (user1.email.equals(query)){
                    users.add(user1);
                }
            }
        if (users.size() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("Không tìm thấy bạn bè, vui lòng thử lại")
                    .setPositiveButton("OK", (dialog, which) -> {
                    });
            builder.create().show();
        }
        adapter.setList(users);
        adapter.notifyDataSetChanged();
        recycleView.setAdapter(adapter);
    }
}