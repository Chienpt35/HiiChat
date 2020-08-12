package com.example.hiichat.Fragment;

import android.app.AlertDialog;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
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
//    private FindFriendAdapter findFriendAdapter ;
    LinearLayout linearLayout;

    RecyclerView listView;
    Button btnFind;
    ArrayList<User> arr =  new ArrayList<>();
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String,String>> save = new ArrayList<HashMap<String, String>>();

    String gender;

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
        return view;
    }


    private void initView(View view) {
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
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "abc" + tvtPossitionEnd.getText()  + "tuoi tu : "  +  tvtOldBegin.getText()
                        + "-" +tvtOldEnd.getText() + "gioi tinh: "  + gender  , Toast.LENGTH_SHORT).show();
            }
        });

        setDataSpinner(spinnerGioiTinh);

        return alertDialog;
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
//                Log.e("onItemSelected", type1.getType() + " " +  type1.getNameType());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
               
            }
        });
    }
}




