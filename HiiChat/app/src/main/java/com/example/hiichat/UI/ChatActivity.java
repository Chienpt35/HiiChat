package com.example.hiichat.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.example.hiichat.BuildConfig;
import com.example.hiichat.Data.SharedPreferenceHelper;
import com.example.hiichat.Data.StaticConfig;
import com.example.hiichat.Model.Consersation;
import com.example.hiichat.Model.Message;
import com.example.hiichat.Model.User;
import com.example.hiichat.Notification.Client;
import com.example.hiichat.Notification.Data;
import com.example.hiichat.Notification.MyResponse;
import com.example.hiichat.Notification.Sender;
import com.example.hiichat.Notification.Token;
import com.example.hiichat.R;
import com.example.hiichat.Service.APIService;
import com.example.hiichat.util.TypeNotification;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerChat;
    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    private ListMessageAdapter adapter;
    private String roomId, nameFriend;
    private ArrayList<CharSequence> idFriend;
    private Consersation consersation;
    private ImageButton btnSend;
    private EmojiconEditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;
    public static HashMap<String, Bitmap> bitmapAvataFriend;
    public Bitmap bitmapAvataUser;
    private LinearLayout linearLayout2, linearlayout3;
    private RelativeLayout r1;
    private View rootView;
    private ImageView imgCamera, imgImage, imgMicro, imgSmile, imgResultCamera, imgVideoCall;
    private EmojIconActions emojIcon;
    private static final int REQUEST_CAMERA = 6789;
    private static final int REQUEST_IMAGE = 1102;
    private CardView frameLayout;
    private Toolbar toolbar;
    private Uri fileUri;
    private String myUrl = "";
    private StorageTask uploadTask;
    private ProgressDialog progressDialog;
    private String sub = "", subStart = "", mCurrentPhotoPath = "", microFileName;
    private File filePathMicro = null;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1, REQUEST_MICROPHONE = 2;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private File photoFile;
    private MediaRecorder recorder;
    private RelativeLayout layoutMicro;
    private TextView tvtTime;
    private ImageView imgMicroOn;
    private CountDownTimer countDownTimer;
    private APIService apiService;
    private boolean notify = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
        animatorEditText();

        Intent intentData = getIntent();
        idFriend = intentData.getCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID);
        roomId = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID);
        nameFriend = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND);

        String base64AvataUser = SharedPreferenceHelper.getInstance(this).getUserInfo().avata;
        if (!base64AvataUser.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            byte[] decodedString = Base64.decode(base64AvataUser, Base64.DEFAULT);
            bitmapAvataUser = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } else {
            bitmapAvataUser = null;
        }

        getListMassage();

        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);
    }

    private void getListMassage() {
        if (idFriend != null && nameFriend != null && toolbar != null) {
            toolbar.setTitle(nameFriend);
            toolbar.setTitleTextColor(getResources().getColor(R.color.colorIndivateTab));
            linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerChat = (RecyclerView) findViewById(R.id.recyclerChat);
            recyclerChat.setLayoutManager(linearLayoutManager);
            adapter = new ListMessageAdapter(ChatActivity.this, consersation, bitmapAvataFriend, bitmapAvataUser);
            FirebaseDatabase.getInstance().getReference().child("message/" + roomId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue() != null) {
                        HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                        Message message = new Message();
                        message.idSender = (String) mapMessage.get("idSender");
                        message.idReceiver = (String) mapMessage.get("idReceiver");
                        message.text = (String) mapMessage.get("text");
                        message.type = (String) mapMessage.get("type");
                        message.timestamp = (long) mapMessage.get("timestamp");
                        consersation.getListMessageData().add(message);
                        adapter.notifyItemChanged(consersation.getListMessageData().size() - 1);
                        recyclerChat.scrollToPosition(recyclerChat.getAdapter().getItemCount() - 1);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            recyclerChat.setAdapter(adapter);
        }
    }

    private void animatorEditText() {
        editWriteMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                TransitionManager.beginDelayedTransition(r1);
                TransitionManager.beginDelayedTransition(linearLayout2);
                TransitionManager.beginDelayedTransition(linearlayout3);
                editWriteMessage.setHint("Nhập tin nhắn...");
                linearlayout3.setVisibility(View.GONE);
                linearLayout2.setVisibility(View.VISIBLE);
                r1.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                r1.requestLayout();
                subStart = editWriteMessage.getText().toString().trim();
                editWriteMessage.setText(subStart);
                editWriteMessage.setSelection(editWriteMessage.getText().length());
            }
        });
        editWriteMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(r1);
                TransitionManager.beginDelayedTransition(linearLayout2);
                TransitionManager.beginDelayedTransition(linearlayout3);
                editWriteMessage.setHint("Nhập tin nhắn...");
                linearlayout3.setVisibility(View.GONE);
                linearLayout2.setVisibility(View.VISIBLE);
                r1.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                r1.requestLayout();
                subStart = editWriteMessage.getText().toString().trim();
                editWriteMessage.setText(subStart);
                editWriteMessage.setSelection(editWriteMessage.getText().length());
            }
        });
        linearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(r1);
                TransitionManager.beginDelayedTransition(linearLayout2);
                TransitionManager.beginDelayedTransition(linearlayout3);
                editWriteMessage.setHint("Aa");
                linearlayout3.setVisibility(View.VISIBLE);
                linearLayout2.setVisibility(View.GONE);
                r1.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                r1.requestLayout();
                sub = editWriteMessage.getText().toString().trim();
                subStart = editWriteMessage.getText().toString().trim();
                Log.e("sub", sub);
                focusText(sub);
            }
        });
    }

    private void focusText(String sub) {
        if (sub.length() >= 16) {
            editWriteMessage.setText(sub.substring(0, 3) + "...");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        progressDialog = new ProgressDialog(this);
        linearLayout2 = findViewById(R.id.linearlayout2);
        r1 = findViewById(R.id.r1);
        linearlayout3 = findViewById(R.id.linearlayout3);
        consersation = new Consersation();
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        findViewById(R.id.imgBack).setSelected(true);
        findViewById(R.id.imgBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar = findViewById(R.id.toolbar);
        tvtTime = (TextView) findViewById(R.id.tvt_time);
        imgMicroOn = (ImageView) findViewById(R.id.imgMicroOn);
        imgCamera = findViewById(R.id.imgCamera);
        imgImage = findViewById(R.id.imgImage);
        imgMicro = findViewById(R.id.imgMicro);
        layoutMicro = (RelativeLayout) findViewById(R.id.layoutMicro);

        setCountDownTimer();


        imgMicro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //verifyMicroPermissions();
                requestAudioPermissions();
            }
        });
        imgMicroOn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startRecording();
                    Log.e("Recording", " Recording start...");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stopRecording();
                    Log.e("Recording", " Recording stop...");
                }
                return true;
            }
        });


        imgSmile = findViewById(R.id.imgSmile);
        imgResultCamera = findViewById(R.id.imgResultCamera);
        frameLayout = findViewById(R.id.frameLayout);

        imgVideoCall = findViewById(R.id.imgVideoCall);
        imgVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, CallingActivity.class);
                startActivity(intent);
            }
        });

        editWriteMessage = findViewById(R.id.editWriteMessage);

        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyStoragePermissions();
            }
        });

        imgImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent, "Select Image"), REQUEST_IMAGE);
            }
        });

        rootView = findViewById(R.id.r);
        emojIcon = new EmojIconActions(ChatActivity.this, rootView, editWriteMessage, imgSmile);
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
        imgSmile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojIcon.ShowEmojIcon();
            }
        });
    }

    private void startRecording() {
        //create file micro
        File outputFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MediaMaster/Dub/");
        filePathMicro = new File(outputFolder.getAbsolutePath() + "out" + new Date().getTime() + ".3gpp");
        Log.e("filePathMicro", filePathMicro.getAbsolutePath());
        Log.e("outputFolder", outputFolder + "");
        countDownTimer.onTick(60000);
        countDownTimer.start();
        if (recorder == null) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(128000);
            recorder.setAudioSamplingRate(96000);
            recorder.setOutputFile(filePathMicro.getAbsolutePath());
            try {
                recorder.prepare();
                recorder.start();
            } catch (IOException e) {
                Log.e("LOG_TAG", "prepare() failed" + e.getMessage());
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording() {
        layoutMicro.setVisibility(View.GONE);
        countDownTimer.cancel();
        try {
            recorder.stop();
            recorder.reset();
            recorder.release();
        } catch (RuntimeException stopException) {
            //handle cleanup here
            Log.d("TAG", " message derreure " + stopException.getMessage());
        }
        recorder = null;
        uploadAudio();
    }

    private void uploadAudio() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        microFileName = timeStamp + "_recorder.3gp";
        final StorageReference fii = FirebaseStorage.getInstance().getReference().child("Recording File").child(microFileName);
        Uri uri = Uri.fromFile(new File(filePathMicro.getAbsolutePath()));
        fii.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fii.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String myUrl2 = uri.toString();

                        Message newMessage4 = new Message();
                        newMessage4.text = myUrl2;
                        newMessage4.idSender = StaticConfig.UID;
                        newMessage4.idReceiver = roomId;
                        newMessage4.type = "media";
                        newMessage4.timestamp = System.currentTimeMillis();
                        FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage4);
                    }
                });
            }
        });
    }


    //thời gian đếm ngược
    private void setCountDownTimer() {
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String v = String.format("%02d", millisUntilFinished / 60000);
                int va = (int) ((millisUntilFinished % 60000) / 1000);
                tvtTime.setText(v + ":" + String.format("%02d", va));
            }

            @Override
            public void onFinish() {
                tvtTime.setText("done!");
                stopRecording();
            }
        };
    }

    private void verifyMicroPermissions() {
        if (ContextCompat.checkSelfPermission(ChatActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ChatActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_MICROPHONE);
        } else {
            //gọi hàm hiển thị view micro
            showOrHideViewMicro();
        }
    }

    private void showOrHideViewMicro() {
        TransitionManager.beginDelayedTransition(layoutMicro);
        if (layoutMicro.getVisibility() == View.GONE) {
            countDownTimer.onTick(60000);
            layoutMicro.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Nhấn giữ để ghi âm, thả để gửi !", Toast.LENGTH_LONG).show();
        }
    }

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_MICROPHONE);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_MICROPHONE);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
            showOrHideViewMicro();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            frameLayout.setVisibility(View.VISIBLE);
            final StorageReference storageReference1 = FirebaseStorage.getInstance().getReference().child("Image File");
            final StorageReference storageReference2 = storageReference1.child(mCurrentPhotoPath);
            final Uri photoURI = FileProvider.getUriForFile(ChatActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile);
            imgResultCamera.setImageURI(photoURI);
            frameLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendImage(storageReference2, photoURI);
                    frameLayout.setVisibility(View.GONE);
                }
            });
        }

        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK && data != null) {
            progressDialog.setTitle("Sending Image");
            progressDialog.setMessage("Please wait, we are sending that file...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            fileUri = data.getData();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image File");
            final StorageReference filePath = storageReference.child(StaticConfig.UID + System.currentTimeMillis() + "." + "jpg");

            uploadTask = filePath.putFile(fileUri);
            uploadTask.continueWithTask((Continuation) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()) {
                    Uri downloadUrl = (Uri) task.getResult();
                    myUrl = downloadUrl.toString();

                    Message newMessage2 = new Message();
                    newMessage2.text = myUrl;
                    newMessage2.idSender = StaticConfig.UID;
                    newMessage2.idReceiver = roomId;
                    newMessage2.type = "image";
                    newMessage2.timestamp = System.currentTimeMillis();
                    FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage2);
                    progressDialog.dismiss();

                    final String msg = "Đã gửi hình ảnh";
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if (notify) {
                                sendNotification(idFriend.get(0).toString(), user.name, msg, "Tin nhắn mới !!!");
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
        }


    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.e("storageDir", storageDir + "");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = imageFileName + "camera.jpg";
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent();
        takePictureIntent.setAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    private void sendImage(final StorageReference storageReference, Uri imageUri) {
        progressDialog.show();
        progressDialog.setTitle("Sending Image");
        progressDialog.setMessage("Please wait, we are sending that file...");
        StorageTask storageTask = storageReference.putFile(imageUri);
        storageTask.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String myUrl1 = uri.toString();

                    Message newMessage3 = new Message();
                    newMessage3.text = myUrl1;
                    newMessage3.idSender = StaticConfig.UID;
                    newMessage3.idReceiver = roomId;
                    newMessage3.type = "image";
                    newMessage3.timestamp = System.currentTimeMillis();
                    FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage3);
                    progressDialog.dismiss();

                    final String msg = "Đã gửi hình ảnh";
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if (notify) {
                                sendNotification(idFriend.get(0).toString(), user.name, msg, "Tin nhắn mới !!!");
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                });
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(ChatActivity.this, "Tải Lên Lỗi !!!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent result = new Intent();
            result.putExtra("idFriend", idFriend.get(0));
            setResult(RESULT_OK, result);
            this.finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra("idFriend", idFriend.get(0));
        setResult(RESULT_OK, result);
        this.finish();
    }

    @Override
    public void onClick(View view) {
        notify = true;
        if (view.getId() == R.id.btnSend) {
            String content = editWriteMessage.getText().toString().trim();
            if (content.length() > 0) {
                editWriteMessage.setText("");
                Message newMessage1 = new Message();
                newMessage1.text = content;
                newMessage1.idSender = StaticConfig.UID;
                newMessage1.idReceiver = roomId;
                newMessage1.type = "text";
                newMessage1.timestamp = System.currentTimeMillis();
                FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage1);

//                String dataJson = new Gson().toJson(newMessage1);
//
//                FCMModel fcmModel = FCMModel.init()
//                        .setData(dataJson)
//                        .setTitle("ABCD")
//                        .setMessage("Tin nhắn mới")
//                        .setTo(idFriend.get(0).toString())
//                        .setType(Type.MESSAGE.toString());

                //FCMSubSend.send(fcmModel, ChatActivity.this);

                final String msg = content;
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (notify) {
                            sendNotification(idFriend.get(0).toString(), user.name, msg, "Tin nhắn mới !!!");
                        }
                        notify = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void sendNotification(String receiver, String username, String message, String type) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference().child("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Token token = dataSnapshot1.getValue(Token.class);
                    Data data = new Data(StaticConfig.UID, R.drawable.iconfinder_message, username + ": " + message, type,
                            receiver, username, roomId, TypeNotification.MESSAGE);
                    Sender sender = new Sender(data, token.getToken());


                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success != 1) {
                                    Toast.makeText(ChatActivity.this, "Failed !!!", Toast.LENGTH_SHORT).show();
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

    public void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(ChatActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    ChatActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        } else {
            // we already have permission, lets go ahead and call camera intent
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "Bạn cần cấp quyền truy cập máy ảnh cho ứng dụng !!!", Toast.LENGTH_SHORT).show();
                }
                break;
//            case REQUEST_MICROPHONE:
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted
//                    //gọi hàm hiển thị view micro
//                    showOrHideViewMicro();
//                }else {
//                    Toast.makeText(this, "Bạn cần cấp quyền truy cập micro cho ứng dụng !!!", Toast.LENGTH_SHORT).show();
//                }
//                break;
            case REQUEST_MICROPHONE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    showOrHideViewMicro();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}

class ListMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private Consersation consersation;
    private HashMap<String, Bitmap> bitmapAvata;
    private HashMap<String, DatabaseReference> bitmapAvataDB;
    private Bitmap bitmapAvataUser;

    public ListMessageAdapter(Context context, Consersation consersation, HashMap<String, Bitmap> bitmapAvata, Bitmap bitmapAvataUser) {
        this.context = context;
        this.consersation = consersation;
        this.bitmapAvata = bitmapAvata;
        this.bitmapAvataUser = bitmapAvataUser;
        bitmapAvataDB = new HashMap<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ChatActivity.VIEW_TYPE_FRIEND_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_friend, parent, false);
            return new ItemMessageFriendHolder(view);
        } else if (viewType == ChatActivity.VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_user, parent, false);
            return new ItemMessageUserHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ItemMessageFriendHolder) {
            if (consersation.getListMessageData().get(position).type.equals("text")) {
                ((ItemMessageFriendHolder) holder).txtContent.setText(consersation.getListMessageData().get(position).text);
                    try{
                        Bitmap currentAvata = bitmapAvata.get(consersation.getListMessageData().get(position).idSender);
                        if (currentAvata != null) {
                            ((ItemMessageFriendHolder) holder).avata.setImageBitmap(currentAvata);
                        } else {
                            final String id = consersation.getListMessageData().get(position).idSender;
                            if (bitmapAvataDB.get(id) == null) {
                                bitmapAvataDB.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id + "/avata"));
                                bitmapAvataDB.get(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() != null) {
                                            String avataStr = (String) dataSnapshot.getValue();
                                            if (!avataStr.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                                                byte[] decodedString = Base64.decode(avataStr, Base64.DEFAULT);
                                                ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                                            } else {
                                                ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avata));
                                            }
                                            notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }catch (Exception e){

                    }
            } else if (consersation.getListMessageData().get(position).type.equals("image")) {
                ((ItemMessageFriendHolder) holder).txtContent.setVisibility(View.GONE);
                ((ItemMessageFriendHolder) holder).imgImageFriend.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(consersation.getListMessageData().get(position).text)
                        .into(((ItemMessageFriendHolder) holder).imgMessageFriend);
            } else if (consersation.getListMessageData().get(position).type.equals("media")) {
                ((ItemMessageFriendHolder) holder).txtContent.setVisibility(View.GONE);
                ((ItemMessageFriendHolder) holder).voicePlayerView.setVisibility(View.VISIBLE);
                ((ItemMessageFriendHolder) holder).voicePlayerView.setAudio(consersation.getListMessageData().get(position).text);
            }
        } else if (holder instanceof ItemMessageUserHolder) {
            if (consersation.getListMessageData().get(position).type.equals("text")) {
                ((ItemMessageUserHolder) holder).txtContent.setText(consersation.getListMessageData().get(position).text);
//                if (bitmapAvataUser != null) {
//                    ((ItemMessageUserHolder) holder).avata.setImageBitmap(bitmapAvataUser);
//                }
            } else if (consersation.getListMessageData().get(position).type.equals("image")) {
                ((ItemMessageUserHolder) holder).txtContent.setVisibility(View.GONE);
                ((ItemMessageUserHolder) holder).imgImageUser.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(consersation.getListMessageData().get(position).text)
                        .into(((ItemMessageUserHolder) holder).imgMessageUser);
            } else if (consersation.getListMessageData().get(position).type.equals("media")) {
                ((ItemMessageUserHolder) holder).txtContent.setVisibility(View.GONE);
                ((ItemMessageUserHolder) holder).voicePlayerView.setVisibility(View.VISIBLE);
                ((ItemMessageUserHolder) holder).voicePlayerView.setAudio(consersation.getListMessageData().get(position).text);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return consersation.getListMessageData().get(position).idSender.equals(StaticConfig.UID) ? ChatActivity.VIEW_TYPE_USER_MESSAGE : ChatActivity.VIEW_TYPE_FRIEND_MESSAGE;
    }

    @Override
    public int getItemCount() {
        return consersation.getListMessageData().size();
    }
}

class ItemMessageUserHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public CircleImageView avata;
    public CardView imgImageUser;
    public ImageView imgMessageUser;
    public ProgressBar progressBar;
    public VoicePlayerView voicePlayerView;


    public ItemMessageUserHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentUser);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView2);
        imgImageUser = itemView.findViewById(R.id.imgImageUser);
        imgMessageUser = (ImageView) itemView.findViewById(R.id.imgMessageUser);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        voicePlayerView = itemView.findViewById(R.id.voicePlayerView);
    }
}

class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public CircleImageView avata;
    public CardView imgImageFriend;
    public ImageView imgMessageFriend;
    public ProgressBar progressBar;
    public VoicePlayerView voicePlayerView;

    public ItemMessageFriendHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentFriend);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView3);
        imgImageFriend = itemView.findViewById(R.id.imgImageFriend);
        imgMessageFriend = (ImageView) itemView.findViewById(R.id.imgMessageFriend);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        voicePlayerView = itemView.findViewById(R.id.voicePlayerView);
    }
}
