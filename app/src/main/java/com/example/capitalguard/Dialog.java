package com.example.capitalguard;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.io.IOException;
import java.text.ParseException;

public class Dialog extends AppCompatDialogFragment {
    private EditText setAlert;
    private DialogListener listener;

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        builder.setView(view).setTitle("Enter dollar threshold").setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String limit = setAlert.getText().toString();
                try {
                    listener.applyText(limit);
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        setAlert = view.findViewById(R.id.set_limit);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " implement dialog listener");
        }
    }

    public interface DialogListener {
        void applyText(String limit) throws IOException, ParseException;
    }
}
