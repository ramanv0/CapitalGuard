# CapitalGuard
A full stack Android personal finance and wealth management app built in Android Studio with Java, XML, Firebase Authentication and Realtime Database, Stanford CoreNLP, AnyChart JavaScript library, Google Cloud Functions written in Node.js, and the Plaid, Dialogflow, News, and Twitter (via Twitter4J) APIs. CapitalGuard allows you to connect all of your financial accounts into one space to get a complete picture of your financial health, help you gain control of your spending, and track and optimize your investments.

## Key Features
- Email and password based authentication with custom UI using the [Firebase Authentication SDK](https://firebase.google.com/docs/auth/android/password-auth)
- Client-side integration to the [Plaid API](https://plaid.com/docs/api/) via [Plaid Link](https://plaid.com/docs/link/) that handles credential validation, authenticates the user’s bank account information, and connects the user’s financial accounts to Plaid
- Alert the user of suspicious activity or overspending if the transaction data retrieved using the Plaid API exceeds the user’s spending limit
- Data analysis and interactive visualization on the user’s spending data retrieved with the Plaid API using custom adapters, various UI [layouts](https://developer.android.com/guide/topics/ui/declaring-layout#CommonLayouts), and the [AnyChart JavaScript library](https://www.anychart.com/products/anychart/docs/), which help the user gain a better understanding of their spending behavior
- Financial assistant chatbot with a custom chat UI built using the [Dialogflow API](https://cloud.google.com/dialogflow/docs) and [Google Cloud Functions](https://cloud.google.com/functions) written in Node.js that query the user’s financial data from the Plaid API stored in the [Firebase database](https://firebase.google.com/docs/database/android/start)
- Provide advice to the user about potential investment opportunities based on sentiment analysis performed on news articles retrieved using the [News API](https://newsapi.org/docs) and Twitter tweets retrieved using the [Twitter API](https://developer.twitter.com/en/docs/twitter-api) via the [Twitter4J Java library](https://twitter4j.org/en/index.html) using the [Stanford CoreNLP library](https://stanfordnlp.github.io/CoreNLP/)

## Technologies Used
### Languages
- Java
- Regex
- XML
- JavaScript (Node.js)
### APIs
- [Plaid API](https://plaid.com/docs/api/)
  - The following Plaid API product endpoints were used to build CapitalGuard (*Note: all Plaid API endpoint requests return standard JSON responses*):
    - [Institutions](https://plaid.com/docs/api/institutions/): retrieve data (e.g. the institution's id, name, supported Plaid products, logo, etc.) about supported financial institutions
    - [Account](https://plaid.com/docs/api/accounts/): fetch account information (e.g. the accounts's id, name, balance, type, etc.) and schemas (the account, currency code, and investment transaction types and corresponding subtypes recognized by Plaid)
    - [Token](https://plaid.com/docs/api/tokens): obtain a `link_token` to initialize [Plaid Link](https://plaid.com/docs/link/), which is the client-side component that the user interacts with in order to connect their financial accounts with the Plaid API. Once Link is initialized and the user has successfully created an `Item` (*Note: `Item` is a Plaid term for a login at a financial institution*), it will return a `public_token` through an [`onSuccess`](https://plaid.com/docs/link/web/#onsuccess) callback that can be [exchanged](https://plaid.com/docs/api/tokens/#itempublic_tokenexchange) for a Plaid API `access_token`. The `access_token` must be obtained in order to call Plaid API endpoints and retrieve information about an `Item`.
    - [Transactions](https://plaid.com/docs/api/products/#transactions): receive paginated user-authorized transaction data for credit, depository, and loan-type accounts
    - [Auth](https://plaid.com/docs/api/products/#auth): retrieve and verify bank account and identification numbers (e.g. routing numbers), and high-level account auth data
    - [Balance](https://plaid.com/docs/api/products/#balance): obtain real-time balance data for each of an `Item`'s financial accounts
    - [Identity](https://plaid.com/docs/api/products/#identity): retrieve and verify the user's identity (name, address, phone number, email) against obtained bank account information
    - [Assets](https://plaid.com/docs/api/products/#assets): access the user's financial information to create and retrieve Asset Reports that contain detailed information about the user's assets and transactions, which can be used for loan underwriting
    - [Investments](https://plaid.com/docs/api/products/#investments): get user-authorized stock position and transaction data from the user's investment accounts
  - To use the Plaid API in CapitalGuard, you will need to receive API keys by signing up [here](https://dashboard.plaid.com/signin?redirect=%2Foverview). Once you have signed up, you will have access to two API keys: `client_id` and `secret`. You can find your Plaid API keys [here](https://dashboard.plaid.com/account/keys). Then, in MainActivityJava.java, you must set the value of `clientUserId` in the `createLinkToken` method to your `client_id` and insert your `client_id` and `secret` as the first and second arguments of `clientIdAndSecret` in the `createPlaidClient` method, respectively. You will also see that there are three different environments in which you can use the Plaid API: Sandbox, Development, and Production. CapitalGuard was built in the Sandbox environment, which gives you access to test credentials and life-like data.
    - Sandbox environment simple test credentials -> username: `user_good`, password: `pass_good`, pin: `credential_good` (when required). You can learn more about Sandbox test credentials [here](https://plaid.com/docs/sandbox/test-credentials/).

