package com.example.hiichat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.hiichat.Adapter.ViewPagerAdapter;
import com.example.hiichat.Data.StaticConfig;
import com.example.hiichat.Fragment.FindFragment;
import com.example.hiichat.Fragment.FriendsFragment;
import com.example.hiichat.Fragment.GroupFragment;
import com.example.hiichat.Fragment.NotificationFragment;
import com.example.hiichat.Fragment.UserProfileFragment;
import com.example.hiichat.Model.User;
import com.example.hiichat.Model.mLocation;
import com.example.hiichat.Notification.Token;
import com.example.hiichat.Service.ServiceUtils;
import com.example.hiichat.UI.LoginActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static String TAG = "MainActivity";
    private Location location ;
    private GoogleApiClient gac ;
    private FloatingActionButton floatButton;
    private ViewPagerAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private BottomNavigationView bottomNavigation;
    DatabaseReference mdata ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mdata = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);

        floatButton = (FloatingActionButton) findViewById(R.id.fab);
        initBottom();
        initFirebase();
        if(checkPlayServices()){
            buildGoogleApiClient();
        }
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateToken(task.getResult().getToken());
            }
        });

    }

    public void setSelectedFindFragment(){
        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, new FindFragment()).commit();
    }

    private void updateToken(String token) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Tokens");
        Token token1 = new Token(token);
        databaseReference.child(firebaseUser.getUid()).setValue(token1);
    }

    public void initBottom() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        setUpBottomNavigation();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, new FriendsFragment()).commit();
    }

    private void setUpBottomNavigation() {
        final FindFragment findFragment = new FindFragment();
        final FriendsFragment friendsFragment = new FriendsFragment();
        final GroupFragment groupFragment = new GroupFragment();
        final UserProfileFragment userProfileFragment = new UserProfileFragment();
        final NotificationFragment notificationFragment = new NotificationFragment();


        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.friend:
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, friendsFragment).commit();
                    return true;
                case R.id.group:
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, groupFragment).commit();
                    return true;
                case R.id.findFriend:
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, findFragment).commit();
                    return true;
                case R.id.inFo:
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, userProfileFragment).commit();
                    return true;
                case R.id.notification:
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, notificationFragment).commit();
                    return true;
            }
            return true;
        });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Kiểm tra quyền hạn
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        } else {
            location = LocationServices.FusedLocationApi.getLastLocation(gac);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();


                mdata.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.child("latitude").exists() && !dataSnapshot.child("longitude").exists()){
                            final HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("latitude", latitude);
                            hashMap.put("longitude", longitude);

                            mdata.updateChildren(hashMap).addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    Toast.makeText(MainActivity.this, "Update location success !!!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            } else {
                Toast.makeText(this, "Hay bat dinh vi de tim kiem", Toast.LENGTH_SHORT).show();
            }
        }
    }
    protected synchronized void buildGoogleApiClient() {
        if (gac == null) {
            gac = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }
    }
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1000).show();
            } else {
                Toast.makeText(this, "Thiết bị này không hỗ trợ.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();
        gac.connect();
        mAuth.addAuthStateListener(mAuthListener);
        ServiceUtils.stopServiceFriendChat(getApplicationContext(), false);
    }

    @Override
    protected void onStop() {
        super.onStop();

        gac.disconnect();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onDestroy() {
        ServiceUtils.startServiceFriendChat(getApplicationContext());
        super.onDestroy();
    }

    public void initFirebase() {
        //Khoi tao thanh phan de dang nhap, dang ky
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    StaticConfig.UID = user.getUid();
                } else {
                    MainActivity.this.finish();
                    // User is signed in
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about) {
            Toast.makeText(this, "HiiChat version 1.0", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        gac.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MainActivity.this, "Loi ket noi"+ connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }
}
