package com.example.hiichat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;

import com.example.hiichat.Adapter.ViewPagerAdapter;
import com.example.hiichat.Fragment.FriendsFragment;
import com.example.hiichat.Fragment.GroupFragment;
import com.example.hiichat.Fragment.MusicFragment;
import com.example.hiichat.Fragment.UserProfileFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private ViewPager viewPager;
    private TabLayout tabLayout = null;
    public static String STR_MUSIC_FRAGMENT = "MUSIC";
    public static String STR_FRIEND_FRAGMENT = "FRIEND";
    public static String STR_GROUP_FRAGMENT = "GROUP";
    public static String STR_INFO_FRAGMENT = "INFO";

    private FloatingActionButton floatButton;
    private ViewPagerAdapter adapter;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("HiiChat");
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        floatButton = (FloatingActionButton) findViewById(R.id.fab);
        initTab();
        initFirebase();
    }
    public void initTab(){
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorIndivateTab));
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

    }
    private void setupTabIcons() {
        int[] tabIcons = {
                R.drawable.ic_tab_music,
                R.drawable.ic_tab_person,
                R.drawable.ic_tab_group,
                R.drawable.ic_tab_infor
        };

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new MusicFragment(), STR_MUSIC_FRAGMENT);
        adapter.addFrag(new FriendsFragment(), STR_FRIEND_FRAGMENT);
        adapter.addFrag(new GroupFragment(), STR_GROUP_FRAGMENT);
        adapter.addFrag(new UserProfileFragment(), STR_INFO_FRAGMENT);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);
//        floatButton.setOnClickListener(((FriendsFragment) adapter.getItem(0)).onClickFloatButton.getInstance(this));
//        viewPager.setAdapter(adapter);
//        viewPager.setOffscreenPageLimit(3);
//        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                //ServiceUtils.stopServiceFriendChat(MainActivity.this.getApplicationContext(), false);
//                if (adapter.getItem(position) instanceof FriendsFragment) {
//                    floatButton.setVisibility(View.VISIBLE);
//                    floatButton.setOnClickListener(((FriendsFragment) adapter.getItem(position)).onClickFloatButton.getInstance(MainActivity.this));
//                    floatButton.setImageResource(R.drawable.plus);
//                } else if (adapter.getItem(position) instanceof GroupFragment) {
//                    floatButton.setVisibility(View.VISIBLE);
//                    floatButton.setOnClickListener(((GroupFragment) adapter.getItem(position)).onClickFloatButton.getInstance(MainActivity.this));
//                    floatButton.setImageResource(R.drawable.ic_float_add_group);
//                } else {
//                    floatButton.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
    }
    public void initFirebase(){

    }
}
