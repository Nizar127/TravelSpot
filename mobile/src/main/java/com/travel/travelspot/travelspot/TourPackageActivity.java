package com.travel.travelspot.travelspot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;


import java.util.*;
import java.util.List;
public class TourPackageActivity extends AppCompatActivity {


    private List<TourPackageItem> tourPackageItemList = null;
    private Button mWish, mBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_package);

        setTitle("TravelSpot Tour Package");

        initializeTourPackageItem();

        //create the recyclerView
        RecyclerView tourRecycleView = (RecyclerView) findViewById(R.id.tourPackage);
        ;

        //create GRID LAYOUT
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        //set layout manager
        tourRecycleView.setLayoutManager(gridLayoutManager);


        //create tour recycler view data adapter with tour item listL
        TourRecyclerViewDataAdapter tourDataAdapter = new TourRecyclerViewDataAdapter(tourPackageItemList);
        tourRecycleView.setAdapter(tourDataAdapter);

    }

    private void initializeTourPackageItem() {
        if (tourPackageItemList == null) {
            tourPackageItemList = new ArrayList<TourPackageItem>();
            tourPackageItemList.add(new TourPackageItem("LE TOUR DE FRANCE", R.drawable.common_full_open_on_phone, "Eiffel Tower,<br/>Chateau"));
        }
    }
}