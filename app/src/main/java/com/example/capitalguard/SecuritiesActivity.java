package com.example.capitalguard;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class SecuritiesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_securities);
        ListView securitiesListView = (ListView) findViewById(R.id.securities_list_view);

        ArrayList<SecuritiesActivityObject> securitiesArrayList =
                (ArrayList<SecuritiesActivityObject>) getIntent()
                        .getSerializableExtra("securitiesActivityObjects");

        SecuritiesArrayListAdapter adapter = new SecuritiesArrayListAdapter(this,
                R.layout.adapter_view_securities_layout, securitiesArrayList);

        securitiesListView.setAdapter(adapter);
    }
}
