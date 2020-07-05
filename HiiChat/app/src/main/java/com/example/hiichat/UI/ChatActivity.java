package com.example.hiichat.UI;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.example.hiichat.Data.SharedPreferenceHelper;
import com.example.hiichat.Data.StaticConfig;
import com.example.hiichat.Model.Consersation;
import com.example.hiichat.Model.Message;
import com.example.hiichat.R;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;


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
    //File
    private File filePathImageCamera;
    private CardView frameLayout;
    private Toolbar toolbar;
    private boolean isShow = true;
    private FirebaseStorage firebaseStorage;
    private Uri fileUri;
    private String myUrl = "", abc = "";
    private StorageTask uploadTask;
    private ProgressDialog progressDialog;
    private String sub = "", subStart = "", mCurrentPhotoPath = "";


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


    }


    private void getListMassage() {
        if (idFriend != null && nameFriend != null && toolbar != null) {
            toolbar.setTitleMarginStart(130);
            toolbar.setTitle(nameFriend);
            toolbar.setTitleTextColor(getResources().getColor(R.color.colorIndivateTab));
            linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerChat = (RecyclerView) findViewById(R.id.recyclerChat);
            recyclerChat.setLayoutManager(linearLayoutManager);
            adapter = new ListMessageAdapter(this, consersation, bitmapAvataFriend, bitmapAvataUser);
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
                        adapter.notifyDataSetChanged();
                        linearLayoutManager.scrollToPosition(consersation.getListMessageData().size() - 1);
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
        editWriteMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    TransitionManager.beginDelayedTransition(r1);
                    TransitionManager.beginDelayedTransition(linearLayout2);
                    TransitionManager.beginDelayedTransition(linearlayout3);
                    editWriteMessage.setHint("Nhập tin nhắn...");
                    linearlayout3.setVisibility(View.GONE);
                    linearLayout2.setVisibility(View.VISIBLE);
                    r1.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                    r1.requestLayout();
                    editWriteMessage.setText(subStart);
                    editWriteMessage.setSelection(editWriteMessage.getText().length());
                }
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
                Log.e("sub", sub );
                focusText(sub);
            }
        });
    }
    private void focusText(String sub){
        if (sub.length() >= 16){
                editWriteMessage.setText(sub.substring(0,3) + "...");
        }
    }

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

        imgCamera = findViewById(R.id.imgCamera);
        imgImage = findViewById(R.id.imgImage);
        imgMicro = findViewById(R.id.imgMicro);
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
                dispatchTakePictureIntent();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK && data != null) {

            frameLayout.setVisibility(View.VISIBLE);
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imgResultCamera.setImageBitmap(bitmap);
//            frameLayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Uri imageUri = data.getData();
//
//                    sendImage(imageUri);
//                    frameLayout.setVisibility(View.GONE);
//                }
//            });
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
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                   if (!task.isSuccessful()){
                       throw task.getException();
                   }
                   return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
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
                    }
                }
            });
        }



    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.hiichat.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoPath);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    private void sendImage(Uri imageUri) {
        StorageReference storageReference1 = FirebaseStorage.getInstance().getReference().child("Image File");
        StorageTask storageTask = storageReference1.putFile(imageUri);
        storageTask.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Uri downloadUrl = (Uri) task.getResult();
                myUrl = downloadUrl.toString();

                Message newMessage3 = new Message();
                newMessage3.text = myUrl;
                newMessage3.idSender = StaticConfig.UID;
                newMessage3.idReceiver = roomId;
                newMessage3.type = "image";
                newMessage3.timestamp = System.currentTimeMillis();
                FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage3);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, "Tải Lên Lỗi !!!", Toast.LENGTH_SHORT).show();
            }
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
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemMessageFriendHolder) {
            if (consersation.getListMessageData().get(position).type.equals("text")) {
                ((ItemMessageFriendHolder) holder).txtContent.setText(consersation.getListMessageData().get(position).text);
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
            }else if (consersation.getListMessageData().get(position).type.equals("image")){
                ((ItemMessageFriendHolder) holder).txtContent.setVisibility(View.GONE);

                Picasso.get()
                        .load(consersation.getListMessageData().get(position).text)
                        .into(((ItemMessageFriendHolder) holder).imgMessageFriend, new Callback() {
                            @Override
                            public void onSuccess() {
                                ((ItemMessageFriendHolder) holder).imgImageFriend.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
            }
        } else if (holder instanceof ItemMessageUserHolder) {
            if (consersation.getListMessageData().get(position).type.equals("text")){
                ((ItemMessageUserHolder) holder).txtContent.setText(consersation.getListMessageData().get(position).text);
                if (bitmapAvataUser != null) {
                    ((ItemMessageUserHolder) holder).avata.setImageBitmap(bitmapAvataUser);
                }
            }else if (consersation.getListMessageData().get(position).type.equals("image")){
                ((ItemMessageUserHolder) holder).txtContent.setVisibility(View.GONE);
                Picasso.get()
                        .load(consersation.getListMessageData().get(position).text)
                        .into(((ItemMessageUserHolder) holder).imgMessageUser, new Callback() {
                            @Override
                            public void onSuccess() {
                                ((ItemMessageUserHolder) holder).imgImageUser.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
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




    public ItemMessageUserHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentUser);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView2);
        imgImageUser = itemView.findViewById(R.id.imgImageUser);
        imgMessageUser = (ImageView) itemView.findViewById(R.id.imgMessageUser);
    }
}

class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public CircleImageView avata;
    public CardView imgImageFriend;
    public ImageView imgMessageFriend;




    public ItemMessageFriendHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentFriend);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView3);
        imgImageFriend = itemView.findViewById(R.id.imgImageFriend);
        imgMessageFriend = (ImageView) itemView.findViewById(R.id.imgMessageFriend);
    }
}
