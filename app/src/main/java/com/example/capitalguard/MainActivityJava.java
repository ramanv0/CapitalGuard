package com.example.capitalguard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capitalguard.network.LinkTokenRequester;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.AccountsBalanceGetRequest;
import com.plaid.client.request.AuthGetRequest;
import com.plaid.client.request.InstitutionsGetByIdRequest;
import com.plaid.client.request.InvestmentsHoldingsGetRequest;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.LinkTokenCreateRequest;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.Account;
import com.plaid.client.response.AccountsBalanceGetResponse;
import com.plaid.client.response.AuthGetResponse;
import com.plaid.client.response.Institution;
import com.plaid.client.response.InstitutionsGetByIdResponse;
import com.plaid.client.response.InvestmentsHoldingsGetResponse;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.LinkTokenCreateResponse;
import com.plaid.client.response.Security;
import com.plaid.client.response.TransactionsGetResponse;
import com.plaid.link.Plaid;
import com.plaid.link.configuration.LinkTokenConfiguration;
import com.plaid.link.result.LinkResultHandler;
import com.plaid.link.result.LinkSuccessMetadata;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kotlin.Unit;
import retrofit2.Response;


@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivityJava extends AppCompatActivity implements Dialog.DialogListener {

    public static String accessTokenGlobal = "";
    private TextView setAlert;
    private TextView currLimit;
    private double newSetLimit = 250;
    private double allInvestmentsGlobal;
    private ArrayList<SecurityType> securityTypeList;
    private ArrayList<SecuritiesActivityObject> securityObjectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_java);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams
                .FLAG_FULLSCREEN);

        View linkButton = findViewById(R.id.open_link);
        linkButton.setOnClickListener(view -> {
            setOptionalEventListener();
            try {
                String linkToken = createLinkToken();
                if (linkToken != null && !linkToken.equals("")) {
                    onLinkTokenSuccess(linkToken);
                }
            } catch (IOException e) {
                onLinkTokenError(e);
                e.printStackTrace();
            }
            /*
            * Uncomment the below openLink function call and delete the above try-catch if you want
            * to use Curl to generate the link token, so that you can utilize the Plaid Link
            * client-side integration. Currently generating the link token programmatically through
            * request-response Plaid API calls.
            */

            // openLink();
        });
    }

    private String createLinkToken() throws IOException {
        setPolicy();
        String clientUserId = "Enter clientId";
        LinkTokenCreateRequest.User user = new LinkTokenCreateRequest.User(clientUserId);

        LinkTokenCreateRequest request = new LinkTokenCreateRequest(
                user,
                "Plaid App",
                Arrays.asList("auth", "transactions", "assets", "investments"),
                Collections.singletonList("US"),
                "en")
                .withWebhook("https://example.com/webhook")
                .withLinkCustomizationName("default")
                .withAndroidPackageName("com.example.capitalguard");

        Response<LinkTokenCreateResponse> response =
                createPlaidClient().service().linkTokenCreate(
                        request).execute();

        assert response.body() != null;
        return response.body().getLinkToken();
    }

    private void setOptionalEventListener() {
        Plaid.setLinkEventListener(linkEvent -> {
            Log.i("Event", linkEvent.toString());
            return Unit.INSTANCE;
        });
    }

    /**
    * Generate a link token using Curl (see LinkTokenRequester.kt). Uncomment the curl request
    * in LinkTokenRequester.kt for this method to work.
    */
    private void openLink() {
        LinkTokenRequester.INSTANCE.getToken()
                .subscribe(this::onLinkTokenSuccess, this::onLinkTokenError);
    }

    private void onLinkTokenSuccess(String token) {
        Plaid.create(
                getApplication(),
                new LinkTokenConfiguration.Builder()
                        .token(token)
                        .build())
                .open(this);
    }

    private void onLinkTokenError(Throwable error) {
        if (error instanceof java.net.ConnectException) {
            Toast.makeText(
                    this,
                    "Please run `sh start_server.sh <client_id> <sandbox_secret>`",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private LinkResultHandler myPlaidResultHandler = new LinkResultHandler(
            linkSuccess -> {
                String publicToken = linkSuccess.getPublicToken();
                LinkSuccessMetadata metadata = linkSuccess.getMetadata();

                String institutionId = metadata.getInstitution().getId();
                String institutionName = metadata.getInstitution().getName();
                String account_name = metadata.getAccounts().get(0).getName();

                setContentView(R.layout.main_transaction_screen);

                String accessToken = "";
                double balance = 0;
                String accountACH = "";
                Bitmap logoBitmap = null;
                String largestCategory = "";
                List<TransactionsGetResponse.Transaction> homeTransactions = null;
                List<TransactionsGetResponse.Transaction> allTransactions;
                TransactionsGetResponse.Transaction largestTxn = null;
                HashMap<String, Double> largestAndSmallestType = null;
                double allInvestments = 0;

                try {
                    accessToken = getAccessToken(publicToken);
                    accessTokenGlobal = accessToken;

                    balance = getAccountBalance(accessToken);
                    accountACH = getAccountNumber(accessToken);
                    logoBitmap = getInstitutionLogo(institutionId);
                    allTransactions = getAllTransactions(accessToken);

                    Pair<List<TransactionsGetResponse.Transaction>, TransactionsGetResponse.Transaction>
                            specificTransactionsPair = latestTransactions(accessToken);
                    homeTransactions = specificTransactionsPair.first;
                    largestTxn = specificTransactionsPair.second;

                    largestCategory = getLargestCategory(allTransactions);

                    Pair<Double, Pair> getInvestmentsPair = getInvestments(accessToken);
                    allInvestments = getInvestmentsPair.first;

                    Pair<HashMap<String, Double>, Pair> largestAndSmallestTypePair
                            = getInvestmentsPair.second;
                    largestAndSmallestType = largestAndSmallestTypePair.first;

                    Pair securityObjectPair = largestAndSmallestTypePair.second;

                    securityTypeList = (ArrayList<SecurityType>) securityObjectPair.first;
                    securityObjectList = (ArrayList<SecuritiesActivityObject>) securityObjectPair.second;
                    allInvestmentsGlobal = allInvestments;
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }

                String balanceAmount = "$" + balance;

                TextView balanceAmountView = (TextView) findViewById(R.id.textView2);
                balanceAmountView.setText(balanceAmount);

                TextView accName = (TextView) findViewById(R.id.textView5);
                accName.setText(account_name);

                TextView accNumber = (TextView) findViewById(R.id.textView4);
                accNumber.setText(accountACH);

                if (logoBitmap != null) {
                    ImageView logoImage = (ImageView) findViewById(R.id.imageView);
                    logoImage.setImageBitmap(logoBitmap);
                }

                RecyclerView rvTransactions = (RecyclerView) findViewById(R.id.rvTransactions);
                List<TransactionsGetResponse.Transaction> transactionsList = homeTransactions;
                TransactionsAdapter adapter = new TransactionsAdapter(transactionsList);
                rvTransactions.setAdapter(adapter);
                rvTransactions.setLayoutManager(new LinearLayoutManager(this));

                setAlert = (TextView) findViewById(R.id.AlertSetLimit);
                currLimit = (TextView) findViewById(R.id.currentLimit);
                setAlert.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialog();
                    }
                });

                String singleAccountId = metadata.getAccounts().get(0).getName();

                HashMap<String, Object> map = new HashMap<>();
                map.put(singleAccountId, new User(publicToken, accessToken, balance, institutionId,
                        institutionName, homeTransactions, largestTxn, largestCategory));

                double highestReturn = profitsAndLoses(securityObjectList).first;

                ArrayList<SecuritiesActivityObject> losesList
                        = profitsAndLoses(securityObjectList).second;

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                        .child("test user").child("plaid data");

                ref.setValue(map);
                ref.child("investment types").setValue(securityTypeList);
                ref.child("investment types").child("largest and smallest type")
                        .setValue(largestAndSmallestType);
                ref.child("investments").setValue(securityObjectList);
                ref.child("investments").child("investments value").setValue(allInvestments);
                ref.child("investments").child("most profitable").setValue(highestReturn);
                ref.child("investments").child("loses").setValue(losesList);

                return Unit.INSTANCE;
            },
            linkExit -> Unit.INSTANCE
    );

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!myPlaidResultHandler.onActivityResult(requestCode, resultCode, data)) {
            Log.i(MainActivityJava.class.getSimpleName(), "Not handled");
        }
    }

    public static class User {
        public String pToken;
        public String institutionId;
        public String institutionName;
        public String aToken;
        public double balanceAmount;
        public List<TransactionsGetResponse.Transaction> transactions;
        public TransactionsGetResponse.Transaction largestTrans;
        public String largestCategory;

        public User(String pToken, String aToken, double balanceAmount, String institutionId,
                    String institutionName, List<TransactionsGetResponse.Transaction> transactions,
                    TransactionsGetResponse.Transaction largestTrans, String largestCategory) {
            this.pToken = pToken;
            this.aToken = aToken;
            this.institutionId = institutionId;
            this.institutionName = institutionName;
            this.balanceAmount = balanceAmount;
            this.transactions = transactions;
            this.largestTrans = largestTrans;
            this.largestCategory = largestCategory;
        }
    }

    public void openAnalyticsView(View view) {
        Intent intent = new Intent(MainActivityJava.this, AnalyticsActivity.class);
        startActivity(intent);
    }

    public void openTransactionsView(View view) {
        Intent intent = new Intent(MainActivityJava.this, TransactionsActivity.class);
        intent.putExtra("accessToken", accessTokenGlobal);
        startActivity(intent);
    }

    public PlaidClient createPlaidClient() {
        return PlaidClient.newBuilder()
                .clientIdAndSecret("Enter clientId", "Enter secret")
                .sandboxBaseUrl()
                .build();
    }

    public String getAccessToken(String publicToken) throws IOException {
        setPolicy();
        PlaidClient plaidClient = createPlaidClient();
        Response<ItemPublicTokenExchangeResponse> itemResponse = plaidClient.service()
                .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken))
                .execute();

        return itemResponse.body().getAccessToken();
    }

    public double getAccountBalance(String accessToken) throws IOException {
        setPolicy();
        PlaidClient plaidClient = createPlaidClient();

        Response<AccountsBalanceGetResponse> accountsBalanceResponse = plaidClient.service()
                .accountsBalanceGet(new AccountsBalanceGetRequest(accessToken))
                .execute();

        List<Account> accounts = accountsBalanceResponse.body().getAccounts();
        return accounts.get(0).getBalances().getAvailable();
    }

    public String getAccountNumber(String accessToken) throws IOException {
        setPolicy();
        PlaidClient plaidClient = createPlaidClient();
        Response<AuthGetResponse> accountsBalanceResponse = plaidClient.service()
                .authGet(new AuthGetRequest(accessToken))
                .execute();

        AuthGetResponse.Numbers numbers = accountsBalanceResponse.body().getNumbers();
        return numbers.getACH().get(0).getAccount();
    }

    public Bitmap getInstitutionLogo(String institutionID) throws IOException {
        setPolicy();
        PlaidClient plaidClient = createPlaidClient();
        Response<InstitutionsGetByIdResponse> logoResponse = plaidClient.service()
                .institutionsGetById(new InstitutionsGetByIdRequest(institutionID,
                        Collections.singletonList("US")).withIncludeOptionalMetadata(true))
                .execute();

        Institution institution = logoResponse.body().getInstitution();

        String instLogo = institution.getLogo();
        Bitmap logoBitmap = null;
        if (instLogo != null && !instLogo.equals("")) {
            byte[] decodedString = Base64.decode(instLogo, Base64.DEFAULT);
            logoBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        return logoBitmap;
    }

    public List<TransactionsGetResponse.Transaction> getAllTransactions(String accessToken)
            throws IOException, ParseException {

        setPolicy();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = simpleDateFormat.parse("2010-01-01");
        Date endDate = simpleDateFormat.parse("2021-06-08");

        PlaidClient plaidClient = createPlaidClient();
        Response<TransactionsGetResponse> transactionResponse = plaidClient.service().transactionsGet(
                new TransactionsGetRequest(
                        accessToken,
                        startDate,
                        endDate))
                .execute();

        return transactionResponse.body().getTransactions();
    }

    public void openDialog() {
        Dialog limitDialog = new Dialog();
        limitDialog.show(getSupportFragmentManager(), "limit dialog");
    }

    @Override
    public void applyText(String limit) throws IOException, ParseException {
        newSetLimit = Double.parseDouble(limit);
        currLimit.setText("Current threshold: $" + limit);
        latestTransactions(accessTokenGlobal);
    }

    /**
    * Return a Pair object containing the last 25 transactions from the user's account as the
    * first value and the largest transaction as the second value. Determine if the daily
    * transaction limit is exceeded: If true, modify the UI to display an alert to the user.
    */
    public Pair<List<TransactionsGetResponse.Transaction>, TransactionsGetResponse.Transaction>
            latestTransactions(String accessToken) throws IOException, ParseException {

        setPolicy();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = simpleDateFormat.parse("2010-01-01");
        Date endDate = simpleDateFormat.parse("2021-06-08");

        PlaidClient plaidClient = createPlaidClient();
        Response<TransactionsGetResponse> transactionSpecificResponse
                = plaidClient.service().transactionsGet(
                new TransactionsGetRequest(
                        accessToken,
                        startDate,
                        endDate)
                        .withCount(25)
                        .withOffset(0)).execute();

        boolean alertGlobal = false;
        double limit = newSetLimit;
        int alertCount = 0;
        TransactionsGetResponse.Transaction largestTxn = transactionSpecificResponse.body()
                .getTransactions().get(0);
        for (Instant currDate = endDate.toInstant().minus(90, ChronoUnit.DAYS);
             currDate.isBefore(endDate.toInstant());
             currDate = currDate.plus(1, ChronoUnit.DAYS)) {

            double currDateTotal = 0.0;
            boolean alert = false;

            for (int i = 0; i < 25; i++) {
                TransactionsGetResponse.Transaction currTransaction
                        = transactionSpecificResponse.body().getTransactions().get(i);

                if (currDate.toString().contains(currTransaction.getDate())) {
                    currDateTotal += currTransaction.getAmount();
                }
                if (currDateTotal >= limit) {
                    if (!alert) {
                        alert = true;
                        alertCount++;
                    }
                    if (!alertGlobal) { alertGlobal = true; }
                }
                if (currTransaction.getAmount() > largestTxn.getAmount()) {
                    largestTxn = currTransaction;
                }
            }
        }

        TextView alertTitle = (TextView) findViewById(R.id.textViewAlert);
        TextView alertText = (TextView) findViewById(R.id.alertText);
        String alertMessage;
        if (alertGlobal) {
            alertTitle.setTextColor(Color.parseColor("#F95004"));
            alertText.setAllCaps(true);
            alertMessage = String.format("You have %d %s", alertCount, "alerts. Please review " +
                    "your transactions below, and contact your bank if you suspect any " +
                    "unauthorized transactions from your account.");
        }
        else {
            alertTitle.setTextColor(Color.parseColor("#737373"));
            alertText.setAllCaps(false);
            alertMessage = "You do not have any alerts! Rest assured that we will inform you of " +
                    "any suspicious activity in your account.";
        }
        alertText.setText(alertMessage);

        return new Pair<>(transactionSpecificResponse.body().getTransactions(), largestTxn);
    }

    /**
    * Create a basic adapter extending from RecyclerView.Adapter and specify a custom ViewHolder
    * that gives access to all relevant views.
    */
    public static class TransactionsAdapter extends
            RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView transactionName;
            public TextView transactionCategory;
            public TextView transactionAmount;
            public TextView transactionDate;
            public ImageView transactionCategoryImage;

            /**
            * Create a constructor that accepts the entire item row and does the view lookups to
            * find each subview.
            */
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
            return new ViewHolder(contactView);
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

    public void openChatView(View view) {
        Intent intent = new Intent(MainActivityJava.this, ChatActivity.class);
        startActivity(intent);
    }

    public void openAssetReportView(View view) {
        Intent intent = new Intent(MainActivityJava.this, AssetReportPDF.class);
        intent.putExtra("accessToken", accessTokenGlobal);
        startActivity(intent);
    }

    public String getLargestCategory(List<TransactionsGetResponse.Transaction> allTransactions) {
        List<String> categories = Arrays.asList("Bank", "Community", "Food and Drink", "Healthcare",
                "Interest", "Payment", "Gyms", "Recreation", "Service", "Shops", "Tax", "Transfer",
                "Travel");
        String largestCategory = "";
        double largestCategoryAmount = 0.0;
        double currentAmount = 0.0;
        for (int i = 0; i < categories.size(); i++) {
            for (int j = 0; j < allTransactions.size(); j++) {
                if (allTransactions.get(j).getCategory().get(0).equals(categories.get(i))) {
                    currentAmount += allTransactions.get(j).getAmount();
                }
            }
            if (currentAmount > largestCategoryAmount) {
                largestCategoryAmount = currentAmount;
                largestCategory = categories.get(i);
            }
        }
        return largestCategory;
    }

    /**
    * Return a nested Pair object containing the total value of all investments: allInvestments,
    * a HashMap containing the names and values of the largest and smallest investment types:
    * largestAndSmallestType, a list containing SecurityType objects: securityTypeList, and a
    * list containing SecuritiesActivityObject objects: securityObjectList.
    */
    public Pair<Double, Pair> getInvestments(String accessToken) throws IOException {
        PlaidClient plaidClient = createPlaidClient();

        Response<InvestmentsHoldingsGetResponse> response =
                plaidClient.service().investmentsHoldingsGet(
                        new InvestmentsHoldingsGetRequest(accessToken))
                        .execute();

        ArrayList<SecurityType> securityTypeList = matchSecuritiesInHoldings(response.body()
                        .getHoldings(), response.body().getSecurities());
        ArrayList<SecuritiesActivityObject> securityObjectList = buildSecuritiesArrayList
                (response.body().getHoldings(), response.body().getSecurities());

        double allInvestments = totalInvestments(response.body().getAccounts());
        HashMap<String, Double> largestAndSmallestType = getLargestAndSmallestType(securityTypeList);

        return new Pair<>(allInvestments, new Pair<HashMap<String, Double>, Pair>
                (largestAndSmallestType, new Pair<>(securityTypeList, securityObjectList)));
    }

    /**
    * Return the total value of all investments in all investment accounts.
    */
    public double totalInvestments(List<Account> accounts) {
        double totalValue = 0.0;
        for (Account account: accounts) {
            if (account.getType().equals("investment")) {
                totalValue += account.getBalances().getCurrent();
            }
        }
        return totalValue;
    }

    /**
    * Return an ArrayList of SecurityType objects, built from the user's holdings and details
    * of the securities in the holdings retrieved using the Plaid API, which will populate the
    * list view in InvestmentsActivity.
    */
    public ArrayList<SecurityType> matchSecuritiesInHoldings(
            List<InvestmentsHoldingsGetResponse.Holding> holdings, List<Security> securities) {

        String type;
        ArrayList<SecurityType> securitiesTypeList = new ArrayList<>();
        HashMap<String, ArrayList<Double>> map = new HashMap<>();

        for (InvestmentsHoldingsGetResponse.Holding holding : holdings) {
            for (Security security : securities) {
                if (holding.getSecurityId().equals(security.getSecurityId())) {
                    type = security.getType();
                    ArrayList<Double> list = new ArrayList<>();
                    if (map.containsKey(type)) {
                        list.add(map.get(type).get(0) + holding.getQuantity());
                        list.add(map.get(type).get(1) + holding.getInstitutionValue());
                    }
                    else {
                        list.add(holding.getQuantity());
                        list.add(holding.getInstitutionValue());
                    }
                    map.put(type, list);
                }
            }
        }

        for (String key : map.keySet()) {
            SecurityType securityTypeObj = new SecurityType(key,
                    String.valueOf(round(map.get(key).get(0), 2)),
                    String.valueOf(round(map.get(key).get(1), 2)));
            securitiesTypeList.add(securityTypeObj);
        }

        SecurityType titles = new SecurityType("Type", "Quantity", "Value");
        securitiesTypeList.add(0, titles);

        return securitiesTypeList;
    }

    /**
    * Return a Pair object with the most profitable security as the first value and an
    * ArrayList of securities with negative return as the second value––these values will be
    * written to the database and used to answer users' queries about their finances using the
    * Dialogflow API.
    */
    public Pair<Double, ArrayList<SecuritiesActivityObject>> profitsAndLoses(
            ArrayList<SecuritiesActivityObject> securityArrayList) {

        double mostProfitable = 0.0;
        double currValue;
        ArrayList<SecuritiesActivityObject> losesList = new ArrayList<>();

        for (int i = 1; i < securityArrayList.size(); i++) {
            currValue = Double.parseDouble(securityArrayList.get(i).getTotalReturn());
            if (mostProfitable < currValue) {
                mostProfitable = currValue;
            }
            if (currValue < 0) {
                losesList.add(securityArrayList.get(i));
            }
        }
        return new Pair<>(mostProfitable, losesList);
    }

    /**
    * Return a HashMap containing the largest and smallest security type in securitiesTypeList,
    * which will be written to the database and used to answer users' queries about their
    * finances using the Dialogflow API.
    */
    public HashMap<String, Double> getLargestAndSmallestType(ArrayList<SecurityType>
                                                                     securitiesTypeList) {
        String largestType = "";
        double largestValue = 0.0;
        double smallestValue = Double.POSITIVE_INFINITY;
        String smallestType = "";
        double currValue;

        for (int i = 1; i < securitiesTypeList.size(); i++) {
            currValue = Double.parseDouble(securitiesTypeList.get(i).getValue());
            if (currValue > largestValue) {
                largestValue = currValue;
                largestType = securitiesTypeList.get(i).getType();
            }
            if (currValue < smallestValue) {
                smallestValue = currValue;
                smallestType = securitiesTypeList.get(i).getType();
            }
        }
        HashMap<String, Double> map = new HashMap<>();
        map.put(largestType, largestValue);
        map.put(smallestType, smallestValue);

        return map;
    }

    /**
    * Return an ArrayList of SecuritiesActivityObject objects, built from the user's holdings
    * and details of the securities in the holdings retrieved using the Plaid API, which will
    * populate the list view in SecuritiesActivity.
    */
    public ArrayList<SecuritiesActivityObject> buildSecuritiesArrayList(
            List<InvestmentsHoldingsGetResponse.Holding> holdings, List<Security> securities) {

        String name;
        String symbol;
        Double price;
        Double shares;
        double equity;
        double totalReturn;
        ArrayList<SecuritiesActivityObject> securitiesObjectList = new ArrayList<>();

        for (InvestmentsHoldingsGetResponse.Holding holding : holdings) {
            for (Security security : securities) {
                if (holding.getSecurityId().equals(security.getSecurityId())) {
                    name = security.getName();
                    symbol = security.getTickerSymbol();
                    if (symbol == null) {
                        symbol = "$";
                    }
                    price = security.getClosePrice();
                    shares = holding.getQuantity();
                    equity = price * shares;
                    totalReturn = equity - (holding.getCostBasis() * shares);
                    SecuritiesActivityObject securityObject = new SecuritiesActivityObject(name,
                            symbol, String.valueOf(round(price, 2)),
                            String.valueOf(round(shares, 2)),
                            String.valueOf(round(totalReturn, 2)),
                            String.valueOf(round(equity, 2)));

                    securitiesObjectList.add(securityObject);
                }
            }
        }
        SecuritiesActivityObject title = new SecuritiesActivityObject("Name",
                "Symbol", "Shares", "Price", "Return",
                "Equity");
        securitiesObjectList.add(0, title);

        return securitiesObjectList;
    }

    public static void setPolicy() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void openInvestmentsView(View view) {
        Intent intent = new Intent(MainActivityJava.this, InvestmentsActivity.class);
        intent.putExtra("investments", allInvestmentsGlobal);
        intent.putExtra("securityObjects", securityTypeList);
        intent.putExtra("securitiesActivityObject", securityObjectList);
        startActivity(intent);
    }

    public void openAdviceView(View view) {
        Intent intent = new Intent(MainActivityJava.this, AdviceActivity.class);
        startActivity(intent);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }
}