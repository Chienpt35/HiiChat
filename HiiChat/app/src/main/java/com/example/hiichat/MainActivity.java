package com.example.hiichat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.example.hiichat.Fragment.UserProfileFragment;
import com.example.hiichat.Service.ServiceUtils;
import com.example.hiichat.UI.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";


    private FloatingActionButton floatButton;
    private ViewPagerAdapter adapter;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private BottomNavigationView bottomNavigation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatButton = (FloatingActionButton) findViewById(R.id.fab);
        initBottom();
        initFirebase();
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


        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                }
                return true;
            }
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
