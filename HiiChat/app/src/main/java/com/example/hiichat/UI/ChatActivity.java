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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hiichat.Data.SharedPreferenceHelper;
import com.example.hiichat.Data.StaticConfig;
import com.example.hiichat.Model.Consersation;
import com.example.hiichat.Model.Message;
import com.example.hiichat.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;

import java.io.File;
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
    private LinearLayout linearLayout2, r3;
    private RelativeLayout r1;
    private View rootView;
    private ImageView imgCamera, imgImage, imgMicro, imgSmile, imgResultCamera;
    private EmojIconActions emojIcon;
    private static final int REQUEST_CAMERA = 6789;
    private static final int REQUEST_IMAGE = 1102;
    //File
    private File filePathImageCamera;
    private FrameLayout frameLayout;
    private Toolbar toolbar;
    private boolean isShow = true;
    private FirebaseStorage firebaseStorage;
    private Uri fileUri;
    private String myUrl = "", abc = "";
    private StorageTask uploadTask;
    private ProgressDialog progressDialog;


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
                        Message newMessage = new Message();
                        newMessage.idSender = (String) mapMessage.get("idSender");
                        newMessage.idReceiver = (String) mapMessage.get("idReceiver");
                        newMessage.text = (String) mapMessage.get("text");
                        newMessage.type = (String) mapMessage.get("type");
                        newMessage.timestamp = (long) mapMessage.get("timestamp");
                        consersation.getListMessageData().add(newMessage);
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
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        final LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        editWriteMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editWriteMessage.setHint("Nhập tin nhắn...");
                    imgCamera.setVisibility(View.GONE);
                    imgImage.setVisibility(View.GONE);
                    imgMicro.setVisibility(View.GONE);
                    linearLayout2.setVisibility(View.VISIBLE);
                    r1.setGravity(RelativeLayout.CENTER_VERTICAL);
                    r1.setLayoutParams(params);
                    r1.requestLayout();
                }
            }
        });
        editWriteMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editWriteMessage.setHint("Nhập tin nhắn...");
                imgCamera.setVisibility(View.GONE);
                imgImage.setVisibility(View.GONE);
                imgMicro.setVisibility(View.GONE);
                linearLayout2.setVisibility(View.VISIBLE);
                r1.setGravity(RelativeLayout.CENTER_VERTICAL);
                r1.setLayoutParams(params);
                r1.requestLayout();
            }
        });
        linearLayout2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                editWriteMessage.setHint("Aa");
                imgCamera.setVisibility(View.VISIBLE);
                imgImage.setVisibility(View.VISIBLE);
                imgMicro.setVisibility(View.VISIBLE);
                linearLayout2.setVisibility(View.GONE);
                r1.setGravity(RelativeLayout.CENTER_VERTICAL);
                r1.setLayoutParams(params1);
                r1.requestLayout();
            }
        });
    }

    private void init() {
        progressDialog = new ProgressDialog(this);
        linearLayout2 = findViewById(R.id.linearlayout2);
        r1 = findViewById(R.id.r1);
        r3 = findViewById(R.id.r3);
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

        editWriteMessage = findViewById(R.id.editWriteMessage);

        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoCameraIntent();
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

    private void photoCameraIntent() {
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(it, REQUEST_CAMERA);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK && data != null) {
            frameLayout.setVisibility(View.VISIBLE);
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imgResultCamera.setImageBitmap(bitmap);
            imgResultCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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

                        Message newMessage = new Message();
                        newMessage.text = myUrl;
                        newMessage.idSender = StaticConfig.UID;
                        newMessage.idReceiver = roomId;
                        newMessage.type = "image";
                        newMessage.timestamp = System.currentTimeMillis();
                        FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage);
                        progressDialog.dismiss();
                    }
                }
            });
        }


        super.onActivityResult(requestCode, resultCode, data);
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
                Message newMessage = new Message();
                newMessage.text = content;
                newMessage.idSender = StaticConfig.UID;
                newMessage.idReceiver = roomId;
                newMessage.type = "text";
                newMessage.timestamp = System.currentTimeMillis();
                FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage);
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
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
                ((ItemMessageFriendHolder) holder).imgImageFriend.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(consersation.getListMessageData().get(position).text)
                        .into(((ItemMessageFriendHolder) holder).imgImageFriend);
            }
        } else if (holder instanceof ItemMessageUserHolder) {
            if (consersation.getListMessageData().get(position).type.equals("text")){
                ((ItemMessageUserHolder) holder).txtContent.setText(consersation.getListMessageData().get(position).text);
                if (bitmapAvataUser != null) {
                    ((ItemMessageUserHolder) holder).avata.setImageBitmap(bitmapAvataUser);
                }
            }else if (consersation.getListMessageData().get(position).type.equals("image")){
                ((ItemMessageUserHolder) holder).txtContent.setVisibility(View.GONE);
                ((ItemMessageUserHolder) holder).imgImageUser.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(consersation.getListMessageData().get(position).text)
                        .into(((ItemMessageUserHolder) holder).imgImageUser);
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
    public ImageView imgImageUser;

    public ItemMessageUserHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentUser);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView2);
        imgImageUser = itemView.findViewById(R.id.imgImageUser);
    }
}

class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public CircleImageView avata;
    public ImageView imgImageFriend;

    public ItemMessageFriendHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentFriend);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView3);
        imgImageFriend = itemView.findViewById(R.id.imgImageFriend);
    }
}
