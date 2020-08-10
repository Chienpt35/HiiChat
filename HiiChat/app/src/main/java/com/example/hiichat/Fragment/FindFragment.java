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
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hiichat.Adapter.MyArrayAdapter;
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

    ListView listView;
    ArrayList<User> arr =  new ArrayList<>();
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String,String>> save = new ArrayList<HashMap<String, String>>();


    private FindFriend findFriend = new FindFriend();


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
    public void getListFriend(){
        arr.removeAll(arr);
        arrayList.removeAll(arrayList);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "dataSnapshot: " + dataSnapshot.getChildren());
                for (DataSnapshot item : dataSnapshot.getChildren()){
                    User user = item.getValue(User.class);
                    arr.add(user);
                }

                String email =  SharedPreferenceHelper.getInstance(getActivity()).getUserInfo().email;
                for(int i = 0; i < arr.size(); i++){
                    if(email.equals(arr.get(i).email)){
                        myLat = arr.get(i).latitude;
                        myLong = arr.get(i).longitude;
                    }

                }

                for (int i=0;i<arr.size();i++){
                    HashMap<String,String> hashMap=new HashMap<String, String>();//create a hashmap to store the data in key value pair
                    hashMap.put("avatar",arr.get(i).avata);
                    hashMap.put("name", arr.get(i).name);
                    hashMap.put("gender",arr.get(i).gioiTinh);
                    hashMap.put("yearOld",arr.get(i).tuoi);
                    hashMap.put("range", Double.toString(CalculationByDistance(myLat, arr.get(i).latitude, myLong , arr.get(i).longitude)) ) ;
                    Log.e(TAG, "Arr: " + arr.get(i).longitude +  " ^^ " + arr.get(i).latitude  + " ^^ " + arr.get(i).name);

                    if(!email.equals(arr.get(i).email)){
                        arrayList.add(hashMap);//add the hashmap into arrayList
                    }
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
        View view = inflater.inflate(R.layout.fragment_find_friend, container, false);

        builderAlertDialog();
        initView(view);
        getListFriend();

        
        return view;
    }


        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot item : dataSnapshot.getChildren()){
                    User user = item.getValue(User.class);
                    arr.add(user);
                }

                Log.e(TAG, "onDataChange: " + arr.get(0));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void initView(View view) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
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
                Type type1 = list.get(position);
//                Log.e("onItemSelected", type1.getType() + " " +  type1.getNameType());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
               
            }
        });
    }
}


//
//
//class FindFriendAdapter extends  RecyclerView.Adapter<FindFriendAdapter.MyViewHolder>{
//
//    private List<User> arrList ;
//    private LayoutInflater layoutInflater;
//    private Context context;
//
//    public FindFriendAdapter(List<User> arrList, Context context) {
//        this.arrList = arrList;
//        this.context = context;
//        layoutInflater = LayoutInflater.from(context);
//
//    }
//
//    public class  MyViewHolder extends RecyclerView.ViewHolder{
//        public TextView name, gender, age, range;
//        public ImageView avatar ;
//
//        public MyViewHolder(@NonNull View itemView) {
//            super(itemView);
//            name = (TextView) itemView.findViewById(R.id.tv_nameFind);
//            gender = (TextView) itemView.findViewById(R.id.tv_genderFind);
//            age = (TextView) itemView.findViewById(R.id.tv_ageFind);
////            range = (TextView) itemView.findViewById(R.id.tv_rangeFind);
//
//            avatar = (ImageView) itemView.findViewById(R.id.avatar_find);
//
//
//
//        }
//    }
//
//
//    @NonNull
//    @Override
//    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View item = layoutInflater.inflate(R.layout.rc_item_find_friend, parent, false);
//        return new MyViewHolder(item);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull FindFriendAdapter.MyViewHolder holder, int position) {
//        User user = arrList.get(position);
//        holder.name.setText(user.getName());
//        holder.gender.setText(user.getGioiTinh());
//        holder.age.setText(user.getTuoi());
////            holder.range.setText(user.get);
//        holder.avatar.setImageResource(Integer.parseInt(user.avata));
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return arrList.size();
//    }
//}


//class FindFriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    private List listFindFriend;
//
//    private Context context;
//    private List listFindFriend_ ;
//    public FindFriendAdapter(List listFindFriend, Context context){
//            this.listFindFriend = listFindFriend;
//            this.context = context;
//
//
//    }
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_find_friend, parent, false);
//        return new ItemFriendViewHolder(context, view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
////        final String name = listFindFriend.getListFriend().get(position).name;
////        final String id = listFindFriend.getListFriend().get(position).id;
////        final String idRoom = listFindFriend.getListFriend().get(position).idRoom;
////        final String avata = listFindFriend.getListFriend().get(position).avata;
//
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return 0;
//    }
//}

