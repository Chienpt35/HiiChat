package com.example.hiichat.Fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hiichat.Adapter.FindFragmentAdapter;
import com.example.hiichat.Adapter.MyArrayAdapter;
import com.example.hiichat.Data.SharedPreferenceHelper;
import com.example.hiichat.Data.StaticConfig;
import com.example.hiichat.Model.FindFriend;
import com.example.hiichat.Model.Type;
import com.example.hiichat.Model.User;
import com.example.hiichat.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.sephiroth.android.library.rangeseekbar.RangeSeekBar;

public class FindFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fab;
    private DatabaseReference db ;
    private FirebaseUser firebaseUser;
    private double myLat, myLong;
    private static final String TAG = "FindFriend";
    LinearLayout linearLayout;
    RecyclerView listView;
    Button btnFind;
    ArrayList<User> arr =  new ArrayList<>();
    String gender;
    double latUser, lngUser;

   FindFragmentAdapter findFragmentAdapter;
    public void getListFriend(){
        arr.removeAll(arr);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        User user = item.getValue(User.class);
                        arr.add(user);
                    }

                    String email = SharedPreferenceHelper.getInstance(getActivity()).getUserInfo().email;
                    for (int i = 0; i < arr.size(); i++) {
                        if (email.equals(arr.get(i).email)) {
                            arr.remove(i);
                        }
                    }
                    //ánh xạ list ra đây
                    findFragmentAdapter.setArrayList(arr);
                    findFragmentAdapter.notifyDataSetChanged();
                }
                }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        db = FirebaseDatabase.getInstance().getReference().child("user");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.find_friend_fragment, container, false);

        builderAlertDialog();
        initView(view);
        getListFriend();
        btnFind = view.findViewById(R.id.add_friend_find);
        getLocationUser();
        return view;
    }

    private void initView(View view) {
        db = FirebaseDatabase.getInstance().getReference().child("user");
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        listView = view.findViewById(R.id.ff_listView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        listView.setLayoutManager(linearLayoutManager);
        findFragmentAdapter = new FindFragmentAdapter(arr, getContext());
        listView.setAdapter(findFragmentAdapter);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builderAlertDialog().show();
            }
        });
    }
    private AlertDialog builderAlertDialog(){

        ArrayList<User> arrFind = new ArrayList<>();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater1 = getLayoutInflater();
        View view = inflater1.inflate(R.layout.dialog_find_friend, null);
        builder.setView(view);

        Spinner spinnerGioiTinh;
        TextView tvtOldBegin;
        TextView tvtOldEnd;
        RangeSeekBar rangeSeekBarOld;
        TextView tvtPossitionEnd;
        Button btnHuy;
        Button btnFind;
        SeekBar seekBar;
        spinnerGioiTinh = (Spinner) view.findViewById(R.id.spinnerGioiTinh);
        tvtOldBegin = (TextView) view.findViewById(R.id.tvt_oldBegin);
        tvtOldEnd = (TextView) view.findViewById(R.id.tvt_oldEnd);
        rangeSeekBarOld = (RangeSeekBar) view.findViewById(R.id.rangeSeekBarOld);
        tvtPossitionEnd = (TextView) view.findViewById(R.id.tvt_PossitionEnd);
        btnHuy = (Button) view.findViewById(R.id.btn_Huy);
        btnFind = (Button) view.findViewById(R.id.btnFind);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);



        final AlertDialog alertDialog = builder.create();
            int minimumValue = 1;
            //location
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    tvtPossitionEnd.setText(String.valueOf(i));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if(seekBar.getProgress() < minimumValue)
                        seekBar.setProgress(minimumValue);
                }
            });
            // age
            rangeSeekBarOld.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                @Override
                public void onProgressChanged(RangeSeekBar rangeSeekBar, int i, int i1, boolean b) {
                    tvtOldBegin.setText(String.valueOf(i));
                    tvtOldEnd.setText(String.valueOf(i1));
                }

                @Override
                public void onStartTrackingTouch(RangeSeekBar rangeSeekBar) {

                }

                @Override
                public void onStopTrackingTouch(RangeSeekBar rangeSeekBar) {

                }
            });
            btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        btnFind.setOnClickListener(new View.OnClickListener() {
            int index = 0;

            @Override
            public void onClick(View v) {
                for (int i = 0; i < arr.size() ; i ++) {
                   if( arr.get(i).getGioiTinh().equals(gender)
                           &&(  Integer.parseInt(arr.get(i).getTuoi()) > Integer.parseInt(tvtOldBegin.getText().toString())
                           &&  Integer.parseInt(arr.get(i).getTuoi()) < Integer.parseInt(tvtOldEnd.getText().toString()))
                           && ((CalculationByDistance(latUser, arr.get(i).latitude, lngUser, arr.get(i).longitude) < Double.parseDouble(tvtPossitionEnd.getText().toString()))))
                   {
                       arrFind.add(arr.get(i));
                       findFragmentAdapter.setArrayList(arrFind);
                       findFragmentAdapter.notifyDataSetChanged();
                       alertDialog.dismiss();
                       index+=1;
                       Toast.makeText(getActivity(), "Có: " + arrFind.size() + "  Người bạn muốn tìm " , Toast.LENGTH_SHORT).show();
                   }
                }
                if (index == 0){
                    Toast.makeText(getActivity(), "Không có người nào phù hợp....hãy mở lòng hơn nhé ", Toast.LENGTH_SHORT).show();
                }else if(tvtPossitionEnd.getText().toString().equals("0")){
                    Toast.makeText(getActivity(), "Bạn muốn tìm kiếm trong khoảng .... ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setDataSpinner(spinnerGioiTinh);
        return alertDialog;
    }
    public void getLocationUser() {
        FirebaseDatabase.getInstance().getReference("user").child(StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null){
                    User user = dataSnapshot.getValue(User.class);
                    latUser = user.latitude;
                    lngUser = user.longitude;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public double CalculationByDistance(double latitude1, double latitude2, double longitude1, double longitude2) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = latitude1;
        double lat2 = latitude2;
        double long1 = longitude1;
        double long2 = longitude2;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(long2 - long1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        double kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        double meterInDec = Integer.valueOf(newFormat.format(meter));
        return kmInDec;
    }

    private void setDataSpinner(final Spinner spinner){
        Type type = new Type("1", "Nam");
        Type type2 = new Type("0", "Nữ");
        final List<Type> list = new ArrayList<>();
        list.add(type);
        list.add(type2);

        MyArrayAdapter myArrayAdapter = new MyArrayAdapter(list, getContext());
        spinner.setAdapter(myArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gender = list.get(position).getNameType();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
               
            }
        });
    }
}




