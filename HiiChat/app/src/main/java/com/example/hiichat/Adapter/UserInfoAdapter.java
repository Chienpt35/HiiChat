package com.example.hiichat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hiichat.Data.SharedPreferenceHelper;
import com.example.hiichat.Model.Configuration;
import com.example.hiichat.Model.User;
import com.example.hiichat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;

import java.util.List;

public class UserInfoAdapter extends RecyclerView.Adapter<UserInfoAdapter.ViewHolder> {
    TextView tvUserName;
    ImageView avatar;
    private DatabaseReference userDB;
    private FirebaseAuth mAuth;
    private User myAccount;
    private Context context;

    private List<Configuration> profileConfig;

    public UserInfoAdapter(Context context, List<Configuration> profileConfig){
        this.profileConfig = profileConfig;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_info_item_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Configuration config = profileConfig.get(position);
        holder.label.setText(config.getLabel());
        holder.value.setText(config.getValue());
        holder.icon.setImageResource(config.getIcon());

    }
    /**
     * Cập nhật username mới vào SharedPreference và thay đổi trên giao diện
     */
    private void changeUserName(String newName){
        userDB.child("name").setValue(newName);


        myAccount.name = newName;
        SharedPreferenceHelper prefHelper = SharedPreferenceHelper.getInstance(context);
        prefHelper.saveUserInfo(myAccount);

        tvUserName.setText(newName);
        //setupArrayListInfo(myAccount);
    }

    void resetPassword(final String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        new LovelyInfoDialog(context) {
                            @Override
                            public LovelyInfoDialog setConfirmButtonText(String text) {
                                findView(com.yarolegovich.lovelydialog.R.id.ld_btn_confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dismiss();
                                    }
                                });
                                return super.setConfirmButtonText(text);
                            }
                        }
                                .setTopColorRes(R.color.colorPrimary)
                                .setIcon(R.drawable.ic_pass_reset)
                                .setTitle("Password Recovery")
                                .setMessage("Sent email to " + email)
                                .setConfirmButtonText("Ok")
                                .show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        new LovelyInfoDialog(context) {
                            @Override
                            public LovelyInfoDialog setConfirmButtonText(String text) {
                                findView(com.yarolegovich.lovelydialog.R.id.ld_btn_confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dismiss();
                                    }
                                });
                                return super.setConfirmButtonText(text);
                            }
                        }
                                .setTopColorRes(R.color.colorAccent)
                                .setIcon(R.drawable.ic_pass_reset)
                                .setTitle("False")
                                .setMessage("False to sent email to " + email)
                                .setConfirmButtonText("Ok")
                                .show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return profileConfig.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView label, value;
        public ImageView icon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            label = (TextView)itemView.findViewById(R.id.tv_title);
            value = (TextView)itemView.findViewById(R.id.tv_detail);
            icon = (ImageView)itemView.findViewById(R.id.img_icon);
        }
    }
}
