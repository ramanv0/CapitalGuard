package com.example.capitalguard;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SecuritiesArrayListAdapter extends ArrayAdapter<SecuritiesActivityObject> {
    private Context mContext;
    int mResource;

    public SecuritiesArrayListAdapter(@NonNull Context context, int resource,
                                      @NonNull ArrayList<SecuritiesActivityObject> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String name = getItem(position).getName();
        String symbol = getItem(position).getSymbol();
        String shares = getItem(position).getShares();
        String price = getItem(position).getPrice();
        String totalReturn = getItem(position).getTotalReturn();
        String equity = getItem(position).getEquity();

        if (!price.equals("Price")) {
            Integer sizePrice = sizeDouble(price);
            if (sizePrice >= 7) {
                double doublePrice = Double.parseDouble(price);
                price = String.valueOf(round(doublePrice, 1));
            }
        }
        if (!equity.equals("Equity")) {
            Integer sizeEquity = sizeDouble(equity);
            if (sizeEquity >= 7) {
                double doubleEquity = Double.parseDouble(equity);
                equity = String.valueOf(round(doubleEquity, 1));
            }
        }

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView textViewName = (TextView) convertView.findViewById(R.id.security_name);
        TextView textViewSymbol = (TextView) convertView.findViewById(R.id.security_symbol);
        TextView textViewShares = (TextView) convertView.findViewById(R.id.security_shares);
        TextView textViewPrice = (TextView) convertView.findViewById(R.id.security_price);
        TextView textViewReturn = (TextView) convertView.findViewById(R.id.security_return);
        TextView textViewEquity = (TextView) convertView.findViewById(R.id.security_equity);

        textViewName.setText(name);
        textViewSymbol.setText(symbol);
        textViewShares.setText(shares);
        textViewPrice.setText(price);
        textViewReturn.setText(totalReturn);
        textViewEquity.setText(equity);

        if (!totalReturn.equals("Return")) {
            double determineColor = Double.parseDouble(totalReturn);
            if (determineColor >= 0) {
                textViewReturn.setTextColor(Color.parseColor("#00C805"));
            } else {
                textViewReturn.setTextColor(Color.parseColor("#F95004"));
            }
        }
        return convertView;
    }

    private Integer sizeDouble(String str) {
        String[] splitter = str.split("\\.");
        Integer beforeDecimal = splitter[0].length();
        Integer afterDecimal = splitter[1].length();
        return beforeDecimal + afterDecimal;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
