package com.example.hiichat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
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
import com.example.hiichat.UI.VideoChatActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.opentok.android.Session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private Location location;
    private FloatingActionButton floatButton;
    private ViewPagerAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private BottomNavigationView bottomNavigation;
    private DatabaseReference mdata;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int ACCESS_FINE_LOCATION_PERM = 678;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mdata = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);

        floatButton = (FloatingActionButton) findViewById(R.id.fab);
        initBottom();
        initFirebase();

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateToken(task.getResult().getToken());
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(ACCESS_FINE_LOCATION_PERM)
    private void requestPermissions() {
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {

            getLocation();


        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, " Vui lòng cho phép truy cập vị trí !!!",
                    ACCESS_FINE_LOCATION_PERM, perms);
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            Location location = task.getResult();
            if (location != null){
                try {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    mdata.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("latitude") && dataSnapshot.hasChild("longitude")){
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
                }catch (Exception e){
                    Log.e(TAG, "getLocation: " + e.getMessage() );
                }
            }
        });
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

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        ServiceUtils.stopServiceFriendChat(getApplicationContext(), false);
    }

    @Override
    protected void onStop() {
        super.onStop();
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

}
