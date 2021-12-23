package com.example.capitalguard;

import androidx.annotation.RequiresApi;
import retrofit2.Response;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.CategoriesGetRequest;
import com.plaid.client.response.CategoriesGetResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AnalyticsActivity extends MainActivityJava {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        List<CategoriesGetResponse.Category> categories = null;
        try {
            categories = getCategories();
        } catch (IOException e) {
            e.printStackTrace();
        }
        AnyChartView anyChartView = findViewById(R.id.any_chart_view);

        Pie pie = AnyChart.pie();

        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
            @Override
            public void onClick(Event event) {
                Toast.makeText(AnalyticsActivity.this, event.getData().get("x") + ": "
                        + event.getData().get("value"), Toast.LENGTH_SHORT).show();
            }
        });

        int bankCount = 0;
        int communityCount = 0;
        int foodCount = 0;
        int healthcareCount = 0;
        int interestCount = 0;
        int paymentCount = 0;
        int recreationCount = 0;
        int serviceCount = 0;
        int shopsCount = 0;
        int taxCount = 0;
        int transfersCount = 0;
        int travelCount = 0;

        for (CategoriesGetResponse.Category category : categories) {
            String categoryHierarchy = category.getHierarchy().get(0);
            if (categoryHierarchy.contains("Bank")) {
                bankCount += 1;
            } else if (categoryHierarchy.contains("Community")) {
                communityCount += 1;
            } else if (categoryHierarchy.contains("Food")) {
                foodCount++;
            } else if (categoryHierarchy.contains("Healthcare")) {
                healthcareCount += 1;
            } else if (categoryHierarchy.contains("Interest")) {
                interestCount += 1;
            } else if (categoryHierarchy.contains("Payment")) {
                paymentCount += 1;
            } else if (categoryHierarchy.contains("Recreation")) {
                recreationCount += 1;
            } else if (categoryHierarchy.contains("Service")) {
                serviceCount += 1;
            } else if (categoryHierarchy.contains("Shops")) {
                shopsCount += 1;
            } else if (categoryHierarchy.contains("Tax")) {
                taxCount += 1;
            } else if (categoryHierarchy.contains("Transfer")) {
                transfersCount += 1;
            } else if (categoryHierarchy.contains("Travel")) {
                travelCount += 1;
            }
        }

        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Bank Fees", bankCount));
        data.add(new ValueDataEntry("Community", communityCount));
        data.add(new ValueDataEntry("Food and Drink", foodCount));
        data.add(new ValueDataEntry("Healthcare", healthcareCount));
        data.add(new ValueDataEntry("Interest ", interestCount));
        data.add(new ValueDataEntry("Payment (CC, Rent, Loan)", paymentCount));
        data.add(new ValueDataEntry("Recreation", recreationCount));
        data.add(new ValueDataEntry("Service", serviceCount));
        data.add(new ValueDataEntry("Shops", shopsCount));
        data.add(new ValueDataEntry("Tax", taxCount));
        data.add(new ValueDataEntry("Transfers", transfersCount));
        data.add(new ValueDataEntry("Travel", travelCount));

        pie.data(data);
        pie.labels().position("outside");
        pie.legend().title().enabled(true);
        pie.legend().title().text("Transaction Categories").padding(0d, 0d, 10d, 0d).fontSize(26);
        pie.legend().position("center-bottom").itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER);

        anyChartView.setChart(pie);
    }

    public List<CategoriesGetResponse.Category> getCategories() throws IOException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        PlaidClient plaidClient = createPlaidClient();
        Response<CategoriesGetResponse> categoriesResponse =
                plaidClient
                        .service()
                        .categoriesGet(new CategoriesGetRequest())
                        .execute();

        assert categoriesResponse.body() != null;
        return categoriesResponse.body().getCategories();
    }
}