package com.example.hiichat.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hiichat.Data.SharedPreferenceHelper;
import com.example.hiichat.Data.StaticConfig;
import com.example.hiichat.MainActivity;
import com.example.hiichat.Model.User;
import com.example.hiichat.R;
import com.example.hiichat.util.ImageUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class EnterInforActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private EditText edtGioiTinh;
    private EditText edtTuoi;
    private Button btnNext;
    private static final int PICK_IMAGE = 1997;
    private User myAccount;
    private LovelyProgressDialog waitingDialog;
    private DatabaseReference userDB;
    private TextInputLayout tipGioiTinh;
    private TextInputLayout tipTuoi;
    public boolean finish;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_infor);
        userDB = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);
        initView();
    }

    private void initView() {
        imgAvatar = (ImageView) findViewById(R.id.img_avatar);
        edtGioiTinh = (EditText) findViewById(R.id.edt_gioiTinh);
        edtTuoi = (EditText) findViewById(R.id.edt_tuoi);
        btnNext = (Button) findViewById(R.id.btnNext);
        waitingDialog = new LovelyProgressDialog(this);
        tipGioiTinh = (TextInputLayout) findViewById(R.id.tipGioiTinh);
        tipTuoi = (TextInputLayout) findViewById(R.id.tipTuoi);
        myAccount = new User();

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserFirebase();
            }
        });

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAvatarClick();
            }
        });
    }

    private void saveUserFirebase() {
        final String gioiTinh = edtGioiTinh.getText().toString().trim();
        final String tuoi = edtTuoi.getText().toString().trim();

        if (gioiTinh.isEmpty()){
            tipGioiTinh.setError("Không được để trống !!!");
        }else {
            tipGioiTinh.setError("");
        }
        if (tuoi.isEmpty()){
            tipTuoi.setError("Không được để trống !!!");
        }else {
            tipTuoi.setError("");
        }



        if (!gioiTinh.isEmpty() && !tuoi.isEmpty()){
            waitingDialog.setCancelable(false)
                    .setTitle("Loading....")
                    .setTopColorRes(R.color.colorView)
                    .show();

            userDB.child("gioiTinh").setValue(gioiTinh)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                finish = true;
                                myAccount.gioiTinh = gioiTinh;
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Update Users", "failed");
                            waitingDialog.dismiss();
                            new LovelyInfoDialog(EnterInforActivity.this)
                                    .setTopColorRes(R.color.colorView)
                                    .setTitle("False")
                                    .setMessage("False to update gioiTinh")
                                    .show();
                        }
                    });

            userDB.child("tuoi").setValue(tuoi)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                finish = true;
                                myAccount.tuoi = tuoi;
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Update Users", "failed");
                            waitingDialog.dismiss();
                            new LovelyInfoDialog(EnterInforActivity.this)
                                    .setTopColorRes(R.color.colorView)
                                    .setTitle("False")
                                    .setMessage("False to update tuoi")
                                    .show();
                        }
                    });

            userDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.e("dataSnapshot", dataSnapshot.child("gioiTinh").getValue() + "" );
                    if (dataSnapshot.child("gioiTinh").getValue() != null){
                        waitingDialog.dismiss();
                        new AlertDialog.Builder(EnterInforActivity.this)
                                .setCancelable(false)
                                .setTitle("Success")
                                .setMessage("Update Users successfully!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(EnterInforActivity.this, MainActivity.class));
                                        EnterInforActivity.this.finish();
                                    }
                                })
                                .create().show();
                        SharedPreferenceHelper preferenceHelper = SharedPreferenceHelper.getInstance(EnterInforActivity.this);
                        preferenceHelper.saveUserInfo(myAccount);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    private void onAvatarClick(){
        new AlertDialog.Builder(this)
                .setTitle("Avatar")
                .setMessage("Are you sure want to change avatar profile?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_PICK);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());

                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
                imgBitmap = ImageUtils.cropToSquare(imgBitmap);
                InputStream is = ImageUtils.convertBitmapToInputStream(imgBitmap);
                final Bitmap liteImage = ImageUtils.makeImageLite(is,
                        imgBitmap.getWidth(), imgBitmap.getHeight(),
                        ImageUtils.AVATAR_WIDTH, ImageUtils.AVATAR_HEIGHT);

                String imageBase64 = ImageUtils.encodeBase64(liteImage);
                myAccount.avata = imageBase64;

                waitingDialog.setCancelable(false)
                        .setTitle("Avatar updating....")
                        .setTopColorRes(R.color.colorView)
                        .show();

                userDB.child("avata").setValue(imageBase64)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    waitingDialog.dismiss();
                                    SharedPreferenceHelper preferenceHelper = SharedPreferenceHelper.getInstance(EnterInforActivity.this);
                                    preferenceHelper.saveUserInfo(myAccount);
                                    imgAvatar.setImageDrawable(ImageUtils.roundedImage(EnterInforActivity.this, liteImage));

                                    new LovelyInfoDialog(EnterInforActivity.this)
                                            .setTopColorRes(R.color.colorView)
                                            .setTitle("Success")
                                            .setMessage("Update avatar successfully!")
                                            .show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Log.d("Update Avatar", "failed");
                                new LovelyInfoDialog(EnterInforActivity.this)
                                        .setTopColorRes(R.color.colorView)
                                        .setTitle("False")
                                        .setMessage("False to update avatar")
                                        .show();
                            }
                        });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}