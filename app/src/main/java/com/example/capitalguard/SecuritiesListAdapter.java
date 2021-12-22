package com.example.capitalguard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SecuritiesListAdapter extends ArrayAdapter<SecurityType> {
    private Context mContext;
    int mResource;

    public SecuritiesListAdapter(@NonNull Context context, int resource,
                                 @NonNull ArrayList<SecurityType> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String type = getItem(position).getType();
        String quantity = getItem(position).getQuantity();
        String value = getItem(position).getValue();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView typeView = (TextView) convertView.findViewById(R.id.security_type);
        TextView quantityView = (TextView) convertView.findViewById(R.id.security_qty);
        TextView valueView = (TextView) convertView.findViewById(R.id.security_value);

        typeView.setText(type);
        quantityView.setText(quantity);
        valueView.setText(value);

        return convertView;
    }
}
