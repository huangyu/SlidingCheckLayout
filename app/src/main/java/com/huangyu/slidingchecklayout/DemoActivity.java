package com.huangyu.slidingchecklayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.huangyu.library.SlidingCheckLayout;

import java.util.ArrayList;
import java.util.List;

public class DemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SlidingCheckLayout scl = findViewById(R.id.scl);
        RecyclerView rlv = findViewById(R.id.rlv);

        final GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        rlv.setLayoutManager(layoutManager);

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, scl);
        rlv.setAdapter(adapter);

        List<String> dataList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            dataList.add(String.valueOf(i));
        }
        adapter.setData(dataList);
    }


}
