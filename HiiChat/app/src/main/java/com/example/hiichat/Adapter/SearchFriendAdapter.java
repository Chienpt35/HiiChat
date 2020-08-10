package com.example.hiichat.Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
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
import com.example.hiichat.UI.ChatActivity;
import com.example.hiichat.util.ImageUtils;
import com.example.hiichat.util.TypeNotification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFriendAdapter extends RecyclerView.Adapter<SearchFriendHolder>{
    ArrayList<User> list;
    Context context;
    LayoutInflater inflater;
    ArrayList<String> arrayList;
    ProgressDialog progressDialog;
    boolean notify = false;
    APIService apiService;

    public SearchFriendAdapter(ArrayList<User> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        arrayList = new ArrayList<>();
        progressDialog = new ProgressDialog(context);
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);
    }

    public void setList(ArrayList<User> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public SearchFriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_search_friend, parent, false);
        return new SearchFriendHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchFriendHolder holder, int position) {
        if (!list.get(position).avata.equals("default")){
            holder.profile.setImageBitmap(ImageUtils.getBitmap(list.get(position).avata));
        }
        holder.tvtName.setText(list.get(position).name);
        holder.tvtEmail.setText(list.get(position).email);
        isFriend(position, holder);
        holder.btnThemBB.setOnClickListener(v -> {
            progressDialog.setMessage("Vui lòng chờ ...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            notify = true;
            requestFriend(position, holder);
        });
    }

    private void requestFriend(int position, SearchFriendHolder holder) {
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
                        holder.btnThemBB.setText("Đã gửi lời mời kết bạn");
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

    private void isFriend(int position, SearchFriendHolder holder){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Request Friend")
                .child(StaticConfig.UID).child(list.get(position).id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null){
                    if (dataSnapshot.getValue().toString().equals("true")){
                        holder.btnThemBB.setVisibility(View.GONE);
                        holder.tvt_isFriend.setVisibility(View.VISIBLE);
                    }else if (dataSnapshot.getValue().toString().equals("false")){
                        holder.btnThemBB.setText("Đã gửi lời mời kết bạn");
                        holder.btnThemBB.setClickable(false);
                    }else {
                        holder.btnThemBB.setVisibility(View.VISIBLE);
                        holder.tvt_isFriend.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
}

class SearchFriendHolder extends RecyclerView.ViewHolder{
    public CircleImageView profile;
    public TextView tvtName;
    public TextView tvtEmail;
    public TextView tvt_isFriend;
    public Button btnThemBB;

    public SearchFriendHolder(@NonNull View itemView) {
        super(itemView);
        profile = (CircleImageView) itemView.findViewById(R.id.profile);
        tvtName = (TextView) itemView.findViewById(R.id.tvt_name);
        tvtEmail = (TextView) itemView.findViewById(R.id.tvt_email);
        tvt_isFriend = (TextView) itemView.findViewById(R.id.tvt_isFriend);
        btnThemBB = (Button) itemView.findViewById(R.id.btnThemBB);
    }
}
