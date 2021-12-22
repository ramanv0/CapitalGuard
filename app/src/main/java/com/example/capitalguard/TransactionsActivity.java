package com.example.capitalguard;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.TransactionsGetResponse;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Response;

public class TransactionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        MaterialDatePicker.Builder<Pair<Long, Long>> dateRangeBuilder = MaterialDatePicker.Builder
                .dateRangePicker().setSelection(
                        new Pair<>(
                                MaterialDatePicker.thisMonthInUtcMilliseconds(),
                                MaterialDatePicker.todayInUtcMilliseconds()
                        )
                );

        MaterialDatePicker datePicker = dateRangeBuilder.setTitleText("Select dates").build();
        findViewById(R.id.button_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show(getSupportFragmentManager(), "Date Picked Opened");
            }
        });

        datePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onPositiveButtonClick(Pair<Long,Long> selection) {
                String accessToken = getIntent().getStringExtra("accessToken");

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String startDate = simpleDateFormat.format(selection.first);
                String endDate = simpleDateFormat.format(selection.second);

                try {
                    Date start = simpleDateFormat.parse(startDate);
                    Date end = simpleDateFormat.parse(endDate);
                    List<TransactionsGetResponse.Transaction> transactionsForRange =
                            getAllTransactions(start, end, accessToken).getTransactions();
                    RecyclerView dateRangeTransactionsView
                            = (RecyclerView) findViewById(R.id.date_range_txns);
                    TransactionsActivity.TransactionsAdapter adapter
                            = new TransactionsActivity.TransactionsAdapter(transactionsForRange);
                    dateRangeTransactionsView.setAdapter(adapter);
                    dateRangeTransactionsView.setLayoutManager(new LinearLayoutManager(
                            TransactionsActivity.this));
                } catch (ParseException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public TransactionsGetResponse getAllTransactions(Date start, Date end, String accessToken)
            throws IOException {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        PlaidClient plaidClient = PlaidClient.newBuilder()
                .clientIdAndSecret("606877ffd481e300111d6fbd", "4d6ee02896be2e2638837b27c9c6b9")
                .sandboxBaseUrl()
                .build();

        Response<TransactionsGetResponse> transactionResponse = plaidClient.service().transactionsGet(
                new TransactionsGetRequest(
                        accessToken,
                        start,
                        end))
                .execute();

        return transactionResponse.body();
    }

    public class TransactionsAdapter extends
        RecyclerView.Adapter<TransactionsActivity.TransactionsAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView transactionName;
            TextView transactionCategory;
            TextView transactionAmount;
            TextView transactionDate;
            ImageView transactionCategoryImage;

            public ViewHolder(View itemView) {
                super(itemView);
                transactionName = (TextView) itemView.findViewById(R.id.textView11);
                transactionCategory = (TextView) itemView.findViewById(R.id.textView12);
                transactionAmount = (TextView) itemView.findViewById(R.id.txnAmount);
                transactionDate = (TextView) itemView.findViewById(R.id.txnDate);
                transactionCategoryImage = (ImageView) itemView.findViewById(R.id.txnImage);
            }
        }

        private List<TransactionsGetResponse.Transaction> storeTxns;

        public TransactionsAdapter(List<TransactionsGetResponse.Transaction> txns) {
            storeTxns = txns;
        }

        @NonNull
        @Override
        public TransactionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                 int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView = inflater.inflate(R.layout.item_activity, parent, false);
            TransactionsActivity.TransactionsAdapter.ViewHolder viewHolder
                    = new TransactionsActivity.TransactionsAdapter.ViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionsAdapter.ViewHolder holder, int position) {
            TransactionsGetResponse.Transaction bindTransaction = storeTxns.get(position);
            String txnName = bindTransaction.getMerchantName();
            if (txnName == null || txnName.equals("")) {
                txnName = bindTransaction.getCategory().get(0);
            }
            TextView nameView = holder.transactionName;
            nameView.setText(txnName);

            String txnCategory = null;
            if (bindTransaction.getCategory().size() == 1) {
                txnCategory = bindTransaction.getCategory().get(0);
            } else if (bindTransaction.getCategory().size() >= 2) {
                txnCategory = bindTransaction.getCategory().get(0) + ", "
                        + bindTransaction.getCategory().get(1);
            }
            TextView categoryView = holder.transactionCategory;

            assert txnCategory != null;
            if (txnCategory.length() >= 29) {
                txnCategory = bindTransaction.getCategory().get(1);
                categoryView.setTextSize(13);
            }
            categoryView.setText(txnCategory);

            TextView priceView = holder.transactionAmount;
            String price = String.valueOf(bindTransaction.getAmount());
            if (bindTransaction.getAmount() < 0.0) {
                priceView.setTextColor(Color.parseColor("#00C805"));
            } else {
                priceView.setTextColor(Color.parseColor("#F95004"));
            }
            priceView.setText(price);

            String dateTxn = bindTransaction.getDate();
            TextView dateView = holder.transactionDate;
            dateView.setText(dateTxn);

            ImageView categoryImage = holder.transactionCategoryImage;
            String category = bindTransaction.getCategory().get(0);
            if (category.contains("Bank")) {
                categoryImage.setImageResource(R.drawable.ic_bank_image);
            } else if (category.contains("Community")) {
                categoryImage.setImageResource(R.drawable.ic_community);
            } else if (category.contains("Food")) {
                categoryImage.setImageResource(R.drawable.ic_food_drink);
            } else if (category.contains("Healthcare")) {
                categoryImage.setImageResource(R.drawable.ic_healthcare);
            } else if (category.contains("Interest")) {
                categoryImage.setImageResource(R.drawable.ic_bank_image);
            } else if (category.contains("Payment")) {
                categoryImage.setImageResource(R.drawable.ic_payment);
            } else if (bindTransaction.getCategory().get(1).contains("Gyms")) {
                categoryImage.setImageResource(R.drawable.ic_gyms);
            } else if (category.contains("Recreation")) {
                categoryImage.setImageResource(R.drawable.ic_recreation);
            } else if (category.contains("Service")) {
                categoryImage.setImageResource(R.drawable.ic_service);
            } else if (category.contains("Shops")) {
                categoryImage.setImageResource(R.drawable.ic_shops);
            } else if (category.contains("Tax")) {
                categoryImage.setImageResource(R.drawable.ic_tax);
            } else if (category.contains("Transfer")) {
                categoryImage.setImageResource(R.drawable.ic_transfer);
            } else if (category.contains("Travel")) {
                categoryImage.setImageResource(R.drawable.ic_travel);
            }
        }

        @Override
        public int getItemCount() {
            return storeTxns.size();
        }
    }
}
