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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import java.util.ArrayList;

public class EnterInforActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private EditText edtTuoi;
    private EditText edt_name;
    private Button btnNext;
    private static final int PICK_IMAGE = 1997;
    private User myAccount;
    private LovelyProgressDialog waitingDialog;
    private DatabaseReference userDB;
    private TextInputLayout tipTuoi;
    private TextInputLayout tip_name;
    private Spinner spinnerGioiTinh;
    private String gioiTinh;
    private ArrayList<String> stringArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_infor);
        userDB = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);
        initView();
    }

    private void initView() {
        imgAvatar = (ImageView) findViewById(R.id.img_avatar);
        edtTuoi = (EditText) findViewById(R.id.edt_tuoi);
        edt_name = (EditText) findViewById(R.id.edt_name);
        btnNext = (Button) findViewById(R.id.btnNext);
        waitingDialog = new LovelyProgressDialog(this);
        tipTuoi = (TextInputLayout) findViewById(R.id.tipTuoi);
        tip_name = (TextInputLayout) findViewById(R.id.tip_name);
        spinnerGioiTinh = (Spinner) findViewById(R.id.spinnerGioiTinh);
        stringArrayList = new ArrayList<>();
        setUpSpinner();
        myAccount = new User();

        btnNext.setOnClickListener(v -> saveUserFirebase());

        imgAvatar.setOnClickListener(v -> onAvatarClick());
    }

    private void setUpSpinner() {
        stringArrayList.add("Nam");
        stringArrayList.add("Nữ");
        stringArrayList.add("Khác");
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, stringArrayList);
        spinnerGioiTinh.setAdapter(arrayAdapter);
        spinnerGioiTinh.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gioiTinh = stringArrayList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void saveUserFirebase() {
        final String tuoi = edtTuoi.getText().toString().trim();
        final String name = edt_name.getText().toString().trim();

        if (tuoi.isEmpty()){
            tipTuoi.setError("Không được để trống !!!");
        }else {
            tipTuoi.setError("");
        }
        if (name.isEmpty()){
            tip_name.setError("Không được để trống !!!");
        }else {
            tip_name.setError("");
        }



        if (!tuoi.isEmpty() && !name.isEmpty()){
            waitingDialog.setCancelable(false)
                    .setTitle("Loading....")
                    .setTopColorRes(R.color.colorView)
                    .show();

            userDB.child("name").setValue(name)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            myAccount.name = name;
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Update Users", "failed");
                        waitingDialog.dismiss();
                        new LovelyInfoDialog(EnterInforActivity.this)
                                .setTopColorRes(R.color.colorView)
                                .setTitle("False")
                                .setMessage("False to update gioiTinh")
                                .show();
                    });

            userDB.child("gioiTinh").setValue(gioiTinh)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            myAccount.gioiTinh = gioiTinh;
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Update Users", "failed");
                        waitingDialog.dismiss();
                        new LovelyInfoDialog(EnterInforActivity.this)
                                .setTopColorRes(R.color.colorView)
                                .setTitle("False")
                                .setMessage("False to update gioiTinh")
                                .show();
                    });

            userDB.child("tuoi").setValue(tuoi)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            myAccount.tuoi = tuoi;
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Update Users", "failed");
                        waitingDialog.dismiss();
                        new LovelyInfoDialog(EnterInforActivity.this)
                                .setTopColorRes(R.color.colorView)
                                .setTitle("False")
                                .setMessage("False to update tuoi")
                                .show();
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
                                .setPositiveButton("OK", (dialog, which) -> {
                                    startActivity(new Intent(EnterInforActivity.this, MainActivity.class));
                                    EnterInforActivity.this.finish();
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
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_PICK);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                    dialogInterface.dismiss();
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