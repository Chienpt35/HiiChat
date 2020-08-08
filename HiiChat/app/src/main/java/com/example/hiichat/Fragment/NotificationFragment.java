package com.example.hiichat.Fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hiichat.Adapter.NotificationAdapter;
import com.example.hiichat.Data.SharedPreferenceHelper;
import com.example.hiichat.Data.StaticConfig;
import com.example.hiichat.Model.User;
import com.example.hiichat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationFragment extends Fragment {
    private DatabaseReference databaseReference;
    private RecyclerView recycleListGroup;
    private ArrayList<String> arrayList;
    private ArrayList<User> userArrayList;
    private NotificationAdapter notificationAdapter;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recycleListGroup = (RecyclerView) view.findViewById(R.id.recycleListGroup);
        recycleListGroup.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        notificationAdapter = new NotificationAdapter(userArrayList, getContext());

        arrayList = new ArrayList<>();
        userArrayList = new ArrayList<>();
        progressDialog = new ProgressDialog(getContext());

        getListAddFriend();
        return view;
    }

    private void getListAddFriend() {
        progressDialog.setMessage("Lấy danh sách lời mời kết bạn");
        progressDialog.show();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Request Friend").child(StaticConfig.UID);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null){
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                        if (dataSnapshot1.getValue().toString().equals("falsed")){
                            arrayList.add(dataSnapshot1.getKey());
                        }
                    }
                    makeListStringKey(arrayList);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("databaseError", databaseError.getMessage() );
            }
        });
    }

    private void makeListStringKey(ArrayList<String> arrayList) {
        if (arrayList.size() > 0){



            for (int i = 0; i < arrayList.size(); i++) {
                databaseReference = FirebaseDatabase.getInstance().getReference("user").child(arrayList.get(i));
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        userArrayList.add(user);
                        makeListUser(userArrayList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void makeListUser(ArrayList<User> userArrayList) {
        if (userArrayList.size() > 0){
            notificationAdapter.setList(userArrayList);
            notificationAdapter.notifyDataSetChanged();
            recycleListGroup.setAdapter(notificationAdapter);
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setMessage("Không có lời mời kết bạn nào !!!")
                    .setPositiveButton("OK", (dialog, which) -> {
                    });
            builder.create().show();
        }
    }
}