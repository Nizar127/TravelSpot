package com.travel.travelspot.travelspot;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class TourRecyclerViewDataAdapter extends RecyclerView.Adapter<TourRecyclerViewItemHolder>{
    @NonNull
    private List<TourPackageItem> tourPackageItemList;
    private ViewGroup parent;

    public TourRecyclerViewDataAdapter(@NonNull List<TourPackageItem> tourPackageItemList) {
        this.tourPackageItemList = tourPackageItemList;
    }

    @Override
    public TourRecyclerViewItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //get layoutinflater object
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        //inflate the recycler view item layout xml
        View tourItemView = layoutInflater.inflate(R.layout.tour_item, parent, false);

        //get tour title text view object
        final TextView tourTitleView = (TextView)tourItemView.findViewById(R.id.packageItemTitle);
        //get tour image
        final ImageView tourImageView = (ImageView)tourItemView.findViewById(R.id.packageItem);
        //get tour description
        final TextView tourTitleDesc = (TextView)tourItemView.findViewById(R.id.packageItemDesc);
        //when click the button
        final Button tourBookNow = (Button)tourItemView.findViewById(R.id.bookNow);
        //when click the button, initiate onclicklistener
        tourBookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });

    }

    @Override
    public void onBindViewHolder(@NonNull TourRecyclerViewItemHolder tourRecyclerViewItemHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
