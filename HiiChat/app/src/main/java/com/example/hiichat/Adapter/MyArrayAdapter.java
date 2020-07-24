package com.example.hiichat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.hiichat.Model.Type;
import com.example.hiichat.R;

import java.util.List;

public class MyArrayAdapter extends BaseAdapter {

    List<Type> list;
    Context context;
    LayoutInflater inflater;

    public MyArrayAdapter(List<Type> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder holder = null;
        if (convertView == null){
            convertView = inflater.inflate(R.layout.item_spinner, parent, false);
            holder = new MyViewHolder();
            holder.tvtTitle = (TextView) convertView.findViewById(R.id.tvt_title);
            convertView.setTag(holder);
        }else {
            holder = (MyViewHolder) convertView.getTag();
        }

        holder.tvtTitle.setText(list.get(position).getNameType());


        return convertView;
    }
}

class MyViewHolder{
    public TextView tvtTitle;

}
