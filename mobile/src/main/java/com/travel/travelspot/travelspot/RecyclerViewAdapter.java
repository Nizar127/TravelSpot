package com.travel.travelspot.travelspot;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{

    private Context mContext;
    private List<tour_list> mData;

    public RecyclerViewAdapter(Context mContext, List<tour_list> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view1;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view1 = mInflater.inflate(R.layout.tour_item,parent,false);

        return new MyViewHolder(view1);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.country_name.setText(mData.get(position).getCountry());
        holder.img_country.setImageResource(mData.get(position).getThumbnail());
        holder.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PackageListActivity.class);
                mContext.startActivity(intent);
               //putExtra() is used to pass data

            }
        });
        //set onclick listener

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView country_name;
        ImageView img_country;
        Button details;

        public MyViewHolder(View itemView){
            super(itemView);

            country_name = (TextView)itemView.findViewById(R.id.tour_title_id);
            img_country = (ImageView)itemView.findViewById(R.id.tourList_id);
            details = (Button)itemView.findViewById(R.id.tourListMore);
        }
    }
}
