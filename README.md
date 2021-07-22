# CapitalGuard
A full stack Android personal finance and wealth management app built in Android Studio with Java, XML, Firebase Authentication and Realtime Database, Stanford CoreNLP, AnyChart JavaScript library, Google Cloud Functions written in Node.js, and the Plaid, Dialogflow, News, and Twitter (via Twitter4J) APIs. CapitalGuard allows you to connect all of your financial accounts into one space to get a complete picture of your financial health, help you gain control of your spending, alert you of any suspicious activity or overspending, and track and optimize your investments.

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
  - To use the Plaid API, you will need to receive API keys by signing up [here](https://dashboard.plaid.com/signin?redirect=%2Foverview). Once you have signed up, you will have access to two API keys: `client_id` and `secret`. You can find your Plaid API keys [here](https://dashboard.plaid.com/account/keys). You will also see that there are three different environments in which you can use the Plaid API: Sandbox, Development, and Production. CapitalGuard was built in the Sandbox environment, which gives you access to test credentials and life-like data.

