package com.example.hiichat.Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hiichat.Data.StaticConfig;
import com.example.hiichat.Model.User;
import com.example.hiichat.Notification.Client;
import com.example.hiichat.Notification.Data;
import com.example.hiichat.Notification.MyResponse;
import com.example.hiichat.Notification.Sender;
import com.example.hiichat.Notification.Token;
import com.example.hiichat.R;
import com.example.hiichat.Service.APIService;
import com.example.hiichat.util.ImageUtils;
import com.example.hiichat.util.TypeNotification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindFragmentAdapter extends RecyclerView.Adapter<FindFragmentHolder>{
    ArrayList<User> list;
    Context context;
    LayoutInflater inflater;
    ArrayList<String> arrayList;
    ProgressDialog progressDialog;
    boolean notify = false;
    APIService apiService;
    double latUser, lngUser;

    public void setArrayList(ArrayList<User> users) {
        this.list = users;
    }

    public FindFragmentAdapter(ArrayList<User> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        arrayList = new ArrayList<>();
        progressDialog = new ProgressDialog(context);
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);
        getLocationUser();
    }

    public void setList(ArrayList<User> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public FindFragmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.rc_item_find_friend, parent, false);
        return new FindFragmentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFragmentHolder holder, int position) {
        if(list.get(position).avata != null){
            if (!list.get(position).avata.equals("default")){
                holder.avatarFind.setImageBitmap(ImageUtils.getBitmap(list.get(position).avata));
            }
        }

        holder.tvNameFind.setText(list.get(position).name);
        holder.tvGenderFind.setText(list.get(position).gioiTinh);
        holder.tvAgeFind.setText(list.get(position).tuoi);
        holder.tvRangeFind.setText(String.valueOf(CalculationByDistance(latUser, list.get(position).latitude, lngUser, list.get(position).longitude)));
        isFriend(position, holder);
        holder.addFriendFind.setOnClickListener(v -> {
            progressDialog.setMessage("Vui lòng chờ ...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            notify = true;
            requestFriend(position, holder);
        });
    }
    public void getLocationUser() {
        FirebaseDatabase.getInstance().getReference("user").child(StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null){
                    User user = dataSnapshot.getValue(User.class);
                    latUser = user.latitude;
                    lngUser = user.longitude;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void requestFriend(int position, FindFragmentHolder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Request Friend")
                .child(list.get(position).id).child(StaticConfig.UID);
        databaseReference.setValue("falsed")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setMessage("Thành công, Vui lòng chờ bạn bè xác nhận !!!")
                                .setPositiveButton("OK", (dialog, which) -> {
                                });
                        builder.create().show();
                        //holder.btnThemBB.setText("Đã gửi lời mời kết bạn");
                        notificationAddFriend(position);
                    }else {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setMessage("Thất bại, Vui lòng thử lại !!!")
                                .setPositiveButton("OK", (dialog, which) -> {
                                });
                        builder.create().show();
                    }
                }).addOnFailureListener(e -> {
        });
        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Request Friend").child(StaticConfig.UID)
                .child(list.get(position).id);
        databaseReference1.setValue("false");
    }
    private void notificationAddFriend(int position) {
        final String msg = "Đã gửi cho bạn lời mời kết bạn";
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(list.get(position).id, user.name, msg, "Lời mời kết bạn !!!");
                }
                notify = false;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void sendNotification(String id, String name, String msg, String s) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference().child("Tokens");
        Query query = tokens.orderByKey().equalTo(id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Token token = dataSnapshot1.getValue(Token.class);
                    Data data = new Data(StaticConfig.UID, R.drawable.iconfinder_add_friend, name + ": " + msg, s,
                            id, name, "", TypeNotification.ADDFRIEND);
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success != 1) {
                                    Toast.makeText(context, "Failed !!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void isFriend(int position, FindFragmentHolder holder){
        if (list.get(position).id != null ){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Request Friend")
                    .child(StaticConfig.UID).child(list.get(position).id);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null){
                        if (dataSnapshot.getValue().toString().equals("true")){
                            holder.addFriendFind.setVisibility(View.GONE);
                            holder.tvt_isFriend.setVisibility(View.VISIBLE);
                        }else if (dataSnapshot.getValue().toString().equals("false")){
                            holder.addFriendFind.setText("Đã gửi lời mời kết bạn");
                            holder.addFriendFind.setClickable(false);
                        }else {
                            holder.addFriendFind.setVisibility(View.VISIBLE);
                            holder.tvt_isFriend.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }
public double CalculationByDistance(double latitude1, double latitude2, double longitude1, double longitude2) {
    int Radius = 6371;// radius of earth in Km
    double lat1 = latitude1;
    double lat2 = latitude2;
    double long1 = longitude1;
    double long2 = longitude2;
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(long2 - long1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
            * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
            * Math.sin(dLon / 2);
    double c = 2 * Math.asin(Math.sqrt(a));
    double valueResult = Radius * c;
    double km = valueResult / 1;
    DecimalFormat newFormat = new DecimalFormat("####");
    double kmInDec = Integer.valueOf(newFormat.format(km));
    double meter = valueResult % 1000;
    double meterInDec = Integer.valueOf(newFormat.format(meter));
    return kmInDec;
}


    @Override
    public int getItemCount() {
        return list.size();
    }
}

class FindFragmentHolder extends RecyclerView.ViewHolder{
    public CircleImageView avatarFind;
    public TextView tvNameFind;
    public TextView tvGenderFind;
    public TextView tvAgeFind;
    public TextView tvRangeFind;
    public TextView tvt_isFriend;
    public Button addFriendFind;


    public FindFragmentHolder(@NonNull View itemView) {
        super(itemView);
        avatarFind = (CircleImageView) itemView.findViewById(R.id.avatar_find);
        tvNameFind = (TextView) itemView.findViewById(R.id.tv_nameFind);
        tvGenderFind = (TextView) itemView.findViewById(R.id.tv_genderFind);
        tvAgeFind = (TextView) itemView.findViewById(R.id.tv_ageFind);
        tvRangeFind = (TextView) itemView.findViewById(R.id.tv_rangeFind);
        addFriendFind = (Button) itemView.findViewById(R.id.add_friend_find);
        tvt_isFriend  = itemView.findViewById(R.id.tvt_isFriend);
    }
}
