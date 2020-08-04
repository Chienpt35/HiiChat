package com.example.hiichat.Fragment;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hiichat.Adapter.MyArrayAdapter;
import com.example.hiichat.Data.StaticConfig;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.sephiroth.android.library.rangeseekbar.RangeSeekBar;

public class FindFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recycleListFriend;
    private FloatingActionButton fab;
    private DatabaseReference db ;
    private FirebaseUser firebaseUser;

    private static final String TAG = "FindFriend";

    ArrayList<User> arr = null;





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        db = FirebaseDatabase.getInstance().getReference().child("user");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        builderAlertDialog();
        initView(view);
        getListFriend();

        return view;
    }

    public void getListFriend(){
        arr = new ArrayList<>();

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()){

                    User user = item.getValue(User.class);
                    Log.e(TAG, "onDataChange: "  );

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });




    }
    private void initView(View view) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        recycleListFriend = (RecyclerView) view.findViewById(R.id.recycleListFriend);
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
        TextView tvtPossitionBegin;
        TextView tvtPossitionEnd;
        RangeSeekBar rangeSeekBarPossition;
        Button btnHuy;
        Button btnFind;
        spinnerGioiTinh = (Spinner) view.findViewById(R.id.spinnerGioiTinh);
        tvtOldBegin = (TextView) view.findViewById(R.id.tvt_oldBegin);
        tvtOldEnd = (TextView) view.findViewById(R.id.tvt_oldEnd);
        rangeSeekBarOld = (RangeSeekBar) view.findViewById(R.id.rangeSeekBarOld);
        tvtPossitionBegin = (TextView) view.findViewById(R.id.tvt_PossitionBegin);
        tvtPossitionEnd = (TextView) view.findViewById(R.id.tvt_PossitionEnd);
        rangeSeekBarPossition = (RangeSeekBar) view.findViewById(R.id.rangeSeekBarPossition);
        btnHuy = (Button) view.findViewById(R.id.btn_Huy);
        btnFind = (Button) view.findViewById(R.id.btnFind);

        final AlertDialog alertDialog = builder.create();

        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "abc", Toast.LENGTH_SHORT).show();
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
                Type type1 = list.get(position);
                Log.e("onItemSelected", type1.getType() + " " +  type1.getNameType());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}