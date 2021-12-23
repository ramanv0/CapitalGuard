package com.example.capitalguard;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.AssetReportCreateRequest;
import com.plaid.client.request.AssetReportPdfGetRequest;
import com.plaid.client.request.IdentityGetRequest;
import com.plaid.client.response.AssetReportCreateResponse;
import com.plaid.client.response.IdentityGetResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AssetReportPDF extends MainActivityJava {
    private EditText editTextTo;
    private EditText editTextSubject;
    private EditText editTextMessage;
    public String name;
    public String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assets_report_pdf);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sending your asset report to yourself?")
                .setPositiveButton("No", dialogClickListener)
                .setNegativeButton("Yes", dialogClickListener).show();

        editTextTo = findViewById(R.id.edit_text_to);
        editTextSubject = findViewById(R.id.edit_text_subject);
        editTextMessage = findViewById(R.id.edit_text_message);

        FirebaseDatabase.getInstance().getReference().child("test user").addListenerForSingleValueEvent(
                new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = snapshot.child("name").getValue().toString();
                email = snapshot.child("email").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("dbError", "loadPost:onCancelled");
            }
        });

        Button buttonSend = findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String accessToken = getIntent().getStringExtra("accessToken");
                    sendMail(accessToken);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        ActivityCompat.requestPermissions(this, new String[]
                {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        StrictMode.VmPolicy.Builder builder_ = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder_.build());
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    FirebaseDatabase.getInstance().getReference().child("users")
                            .child("retrieveName").setValue("");
                    EditText recipient = findViewById(R.id.edit_text_to);
                    recipient.setText(email);
                    break;
            }
        }
    };

    /**
    * Create a request to and receive a response from the Plaid API Assets product endpoint in
    * order to retrieve an Asset Report token, which initiates the process of creating an Asset
    * Report. Pass the Asset Report token as an argument to the getAssetPDF method, which
    * returns the PDF binary data of the Asset Report. Create a file and write the PDF binary
    * data to it. Create an intent chooser that allows the user to choose an email client and
    * attach the created Asset Report PDF file, subject, and message to the email intent as
    * extended data.
    */
    private void sendMail(String accessToken) throws IOException {
        String recipientList = editTextTo.getText().toString();
        String[] recipients = recipientList.split(",");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String[] fullName = name.split("\\W+");
        String firstName = fullName[0];
        String middleName = "";
        String lastName;

        if (fullName.length == 3) {
            middleName = fullName[1];
            lastName = fullName[2];
        } else if (fullName.length == 2) {
            lastName = fullName[1];
        } else {
            lastName = "";
        }

        PlaidClient plaidClient = PlaidClient.newBuilder()
                .clientIdAndSecret("Enter clientId", "Enter secret")
                .sandboxBaseUrl()
                .build();

        Response<IdentityGetResponse> identityResponse = plaidClient.service().identityGet(
                new IdentityGetRequest(accessToken)).execute();

        IdentityGetResponse.PhoneNumber phone = identityResponse.body().getAccounts().get(0)
                .getOwners().get(0).getPhoneNumbers().get(0);
        String accountId = identityResponse.body().getAccounts().get(0).getAccountId();

        List<String> accessTokenList = Arrays.asList(accessToken);

        AssetReportCreateRequest assetRequest =
                new AssetReportCreateRequest(accessTokenList, 60)
                        .withFirstName(firstName).withMiddleName(middleName).
                        withLastName(lastName).withPhoneNumber(phone.getData()).withEmail(email)
                        .withClientUserId(accountId);

        Response<AssetReportCreateResponse> response =
                plaidClient
                        .service()
                        .assetReportCreate(assetRequest)
                        .execute();

        String assetReportToken = response.body().getAssetReportToken();
        byte[] pdf = getAssetPDF(assetReportToken);

        File dir = new File(getApplicationContext().getExternalCacheDir(), "/PDF");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "AssetReport.pdf");
        OutputStream out = new FileOutputStream(file);
        out.write(pdf);
        out.close();

        String subject = editTextSubject.getText().toString();
        String message = editTextMessage.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.toURI().toString()));

        intent.setType("application/pdf");
        startActivity(Intent.createChooser(intent, "Choose an email client"));
    }

    /**
    * Returns a byte array representation (i.e. the PDF binary data) of the Plaid API Asset
    * Report PDF endpoint response.
    */
    public byte[] getAssetPDF(String assetReportToken) throws IOException {
        PlaidClient plaidClient = PlaidClient.newBuilder()
                .clientIdAndSecret("Enter clientId", "Enter secret")
                .sandboxBaseUrl()
                .build();
        try {
            Thread.sleep(20000);
            Response<ResponseBody> response =
                    plaidClient
                            .service()
                            .assetReportPdfGet(new AssetReportPdfGetRequest(assetReportToken))
                            .execute();
            return response.body().bytes();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return getAssetPDF(assetReportToken);
        }
    }
}
