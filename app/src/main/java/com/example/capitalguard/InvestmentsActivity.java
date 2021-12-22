package com.example.capitalguard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class InvestmentsActivity extends MainActivityJava {
    public ArrayList<SecuritiesActivityObject> securityListIntent;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investments);

        double investments = round(getIntent().getDoubleExtra("investments",
                0.0), 2);

        TextView valueInvestments = (TextView) findViewById(R.id.amount_investments);
        valueInvestments.setText("$" + investments);
        determineColor(valueInvestments, investments);

        ListView securitiesListView = (ListView) findViewById(R.id.security_type_list_view);

        ArrayList<SecurityType> securitiesList = (ArrayList<SecurityType>) getIntent()
                .getSerializableExtra("securityObjects");
        SecuritiesListAdapter adapter
                = new SecuritiesListAdapter(this, R.layout.adapter_view_layout, securitiesList);
        securitiesListView.setAdapter(adapter);

        securityListIntent = (ArrayList<SecuritiesActivityObject>) getIntent()
                .getSerializableExtra("securitiesActivityObject");
    }

    public void determineColor(TextView view, double value) {
        if (value >= 0) { view.setTextColor(Color.parseColor("#00C805")); }
        else { view.setTextColor(Color.parseColor("#F95004")); }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void openSecuritiesListView(View view) {
        Intent intent = new Intent(InvestmentsActivity.this, SecuritiesActivity.class);
        intent.putExtra("securitiesActivityObjects", securityListIntent);
        startActivity(intent);
    }
}
