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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationdHolder> {
    private ArrayList<User> list;
    private Context context;
    private LayoutInflater inflater;
    private ProgressDialog progressDialog;
    private APIService apiService;
    private boolean notify = false;

    public NotificationAdapter(ArrayList<User> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        progressDialog = new ProgressDialog(context);
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);
    }

    public void setList(ArrayList<User> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public NotificationdHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_notifi_friend, parent, false);
        return new NotificationdHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationdHolder holder, int position) {
        if (!list.get(position).avata.equals("default")){
            holder.profile.setImageBitmap(ImageUtils.getBitmap(list.get(position).avata));
        }
        holder.tvtName.setText(list.get(position).name);
        holder.tvtEmail.setText(list.get(position).email);

        holder.btnThemBB.setOnClickListener(v -> {
            notify = true;
            addFriend(list.get(position).id, position);
        });
        holder.btnHuyBB.setOnClickListener(v -> {
            cancelFriend(position);
        });
    }

    private void cancelFriend(int position) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Request Friend");
        databaseReference.child(StaticConfig.UID).child(list.get(position).id).removeValue();
        databaseReference.child(list.get(position).id).child(StaticConfig.UID).removeValue();
        list.remove(position);
        notifyDataSetChanged();
    }

    private void addFriend(String idFriend, int position) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Request Friend");
        databaseReference.child(StaticConfig.UID)
                .child(idFriend).setValue("true")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setMessage("Xác nhận bạn bè thành công !!!")
                                .setPositiveButton("OK", (dialog, which) -> {
                                });
                        builder.create().show();
                        notificationAddFriend(idFriend);
                        list.remove(position);
                        notifyDataSetChanged();
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
        databaseReference.child(idFriend).child(StaticConfig.UID).setValue("true");
    }

    private void notificationAddFriend(String idFriend) {
        final String msg = "Đã chấp nhận lời mời kết bạn";
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(idFriend, user.name, msg, "Lời mời kết bạn !!!");
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

    @Override
    public int getItemCount() {
        return list.size();
    }
}

class NotificationdHolder extends RecyclerView.ViewHolder{
    public CircleImageView profile;
    public TextView tvtName;
    public TextView tvtEmail;
    public Button btnThemBB;
    public Button btnHuyBB;

    public NotificationdHolder(@NonNull View itemView) {
        super(itemView);
        profile = (CircleImageView) itemView.findViewById(R.id.profile);
        tvtName = (TextView) itemView.findViewById(R.id.tvt_name);
        tvtEmail = (TextView) itemView.findViewById(R.id.tvt_email);
        btnThemBB = (Button) itemView.findViewById(R.id.btnThemBB);
        btnHuyBB = (Button) itemView.findViewById(R.id.btnHuyBB);
    }
}
