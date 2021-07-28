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
- **[Plaid API](https://plaid.com/docs/api/)**
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
  - To use the Plaid API in CapitalGuard, you will need to receive API keys by signing up [here](https://dashboard.plaid.com/signin?redirect=%2Foverview). Once you have signed up, you will have access to two API keys: `client_id` and `secret`. You can find your Plaid API keys [here](https://dashboard.plaid.com/account/keys). Then, in MainActivityJava.java, you must set the value of `clientUserId` in the `createLinkToken` method to your `client_id` and insert your `client_id` and `secret` as the first and second arguments of `clientIdAndSecret`, respectively, in the `createPlaidClient` method. You will also see that there are three different environments in which you can use the Plaid API: Sandbox, Development, and Production. CapitalGuard was built in the Sandbox environment, which gives you access to test credentials and life-like data.
    - Sandbox environment simple test credentials -> username: `user_good`, password: `pass_good`, pin: `credential_good` (when required). You can learn more about Sandbox test credentials [here](https://plaid.com/docs/sandbox/test-credentials/).
  - The Plaid API was installed in CapitalGuard with the following Gradle dependencies:
  ```
  implementation 'com.plaid.link:sdk-core:3.2.6'
  
  implementation 'com.plaid:plaid-java:8.1.0'
  ```

- **[Dialogflow API](https://cloud.google.com/dialogflow/es/docs)**
  - CapitalGuard uses the Dialogflow API to build a personal finance assistant that is able to answer users' questions about their financial health based on their financial data retrieved via the Plaid API and stored in the Firebase database.
  - Since CapitalGuard uses a custom-built, in-app conversation platform, and not one of Dialogflow's [integrations](https://cloud.google.com/dialogflow/es/docs/integrations), I had to write code that directly interacts with the end-user. I also had to directly and asynchronously interact with the Dialogflow API for each conversational turn in order to send end-user expressions to Dialogflow and receive information about intent matches. You can learn more about the processing flow when interacting with the Dialogflow API [here](https://cloud.google.com/dialogflow/es/docs/api-overview).
  - To use the Dialogflow API in CapitalGuard, you will need to set up a Google Cloud Platform (GCP) project and authentication. To do so, follow the steps in the GCP [Setup quickstart](https://cloud.google.com/dialogflow/docs/quick/setup): It will guide you through all of the steps required to start using the Dialogflow API, such as creating a Dialogflow project, GCP billing, enabling the Dialogflow API and audit logs, setting up authentication with service accounts and keys (for more information about authentication, you can also read [this](https://cloud.google.com/docs/authentication)), initializing the Google Cloud SDK (the Cloud SDK provides many useful tools for managing resources hosted on Google Cloud), and installing the Dialogflow API client library. 
    - CapitalGuard uses the most common option for calling GCP APIs (in this case, the Dialogflow API): Google supported client libraries. There are two other options for calling the Dialogflow API: REST and gRPC (you can read more about them [here](https://cloud.google.com/dialogflow/es/docs/reference/api-overview)). The Dialogflow API client library was installed in CapitalGuard with the following Gradle dependencies:
    ```
    implementation platform('com.google.cloud:libraries-bom:20.8.0')

    implementation 'com.google.cloud:google-cloud-dialogflow'
    ```
    If you choose to setup your CapitalGuard project with [Maven](https://maven.apache.org/) or [sbt](https://www.scala-sbt.org/), you can learn how to install the Dialogflow API [here](https://cloud.google.com/dialogflow/es/docs/quick/setup#lib).
  - After you setup your GCP project and authentication, you will need to build agents, which are virtual agents that are trained to handle expected conversations with end-users, using the Dialogflow Console. You can learn more about how Dialogflow agents work [here](https://cloud.google.com/dialogflow/es/docs/agents-overview).
    - To build an agent:
      1. Go to your [Dialogflow Console](https://dialogflow.cloud.google.com/#/login) and sign in
      2. Click "Create Agent" in the left sidebar menu
      3. Enter the requested information (agent's name, language, time zone)
      4. Select "Create a new Google project"
      5. Click "Create"
  - Once you created your agent, you will need to define and train intents to categorize end-user intentions for each conversation turn. The combined intents of a successful agent should be able to handle a complete conversation with the end-user. When the end-user writes a question to CapitalGuard's personal financial assistant bot, Dialogflow matches the question to the best intent in your agent.
    - To define a basic intent:
      1. Click the add intent button in the left sidebar menu
      2. Enter the name of your intent in the "Intent name" field
      3. Click "Add training phrases" in the "Training Phrases" section, and enter your training phrases
         - Training phrases are example phrases for what the end-user might ask. In the process of intent classification, Dialogflow matches the end-user's question to the intent whose training phrase(s) it most resembles. It is best that you define a handful (~10-20, depending on the complexity of your intent) of training phrases to improve the accuracy of the intent classification; however, Dialogflow utilizes machine learning to expand your list with other similar phrases.
      4. In the "Responses" section, enter the response you want your financial assistant bot to return to the end-user if the intent is matched in the Text Response section
         - Responses are not limited to text: You can have a speech or visual response, too. 
         - Responses can provide the end-user with a one-time answer, ask for more information, or terminate the conversation.
      5. Click "Save" and wait until the agent training is complete
      6. Test your intent using the chatbot simulator in the right sidebar
      
      To learn more about Dialogflow intents (Action, Parameters, Contexts, and Events), go [here](https://cloud.google.com/dialogflow/es/docs/intents-overview).
 
- **[News API](https://newsapi.org/)**
  - *Note: requests to the REST News API return JSON search results for current and historic news articles*
  - CapitalGuard uses the News API to retrieve news articles from sources and blogs across the web. Sentiment analysis is then performed on the articles to provide users with advice on potential investment opportunities.
  - To use the News API in CapitalGuard, you will need to register for a News API key, which are free while you are in development, [here](https://newsapi.org/register). 
  - Once you are registered and have obtained a News API key, follow the [Get started](https://newsapi.org/register) guide.
    - The News API offers two main endpoints:
      1. `/everything`: Search for articles on the web that mention a keyword or phrase
      2. `/top-headlines`: Get the current top headlines for a country, category, or publisher
    - CapitalGuard uses the `/everything` endpoint––endpoint (a)––in order to find news articles that mention the ticker symbol of the stock that the user is interested in buying and wants to receive advice for. To learn more about the request parameters and JSON response object of the `/everything` endpoint, you can read [this](https://newsapi.org/docs/endpoints/everything).
  - To authenticate the News API in CapitalGuard, in the `getNewsSentiment` method in AdviceActivity.java, you need to set the `apiKey` constructor argument of the instantiated `NewsApiClient` object called `newsApiClient` to your News API key.
  - To make requests and receive responses from the News API, you need to install the News API Java client library (SDK), which allows CapitalGuard to use the News API without having to make HTTP requests directly.
    - The News API Java client library was installed in CapitalGuard by:
      1. Adding the below JitPack repository to the project's root build.gradle file:
         ```
         allprojects {
           repositories {
              ...
              maven { url 'https://jitpack.io' }
           }
         }
         ```
      2. Adding the below dependency to the project's Gradle dependencies:
         ```
         implementation 'com.github.KwabenBerko:News-API-Java:1.0.0'
         ```
  - To learn more about News API usage, you can go [here](https://newsapi.org/docs).
 
- **[Twitter API](https://developer.twitter.com/en/docs/twitter-api)**
  - CapitalGuard uses the Twitter API to programmatically retrieve tweets. Sentiment analysis is then performed on the tweets to provide users with advice on potential investment opportunities.
  - To get access to the Twitter API, you need to:
    1. Apply and receive approval for a developer account
       - The Twitter API offers two product tracks: Standard or Academic Research. CapitalGuard uses the Standard Twitter API product track, which is the default track for most developers. You can apply for the Standard product track [here](https://developer.twitter.com/en/apply-for-access).
       - Once your developer account is approved, you can create a Standard project by following [this](https://developer.twitter.com/en/docs/projects/overview) guide and an associated developer App that will provide you a set of keys and tokens that you will use to authenticate all requests to the Twitter API.
    2. Save your developer App's keys and tokens
       - Within your developer App, you will be able to find or generate:
         - API Key: The "username" that will allow you to make Twitter API requests from CapitalGuard
         - API Secret (Key): The "password" that will allow you to make Twitter API requests from CapitalGuard
         - Access Token & Access Token Secret: Tokens that represent CapitalGuard's Twitter account and allow you to make Twitter API requests on behalf of the account
    3. Set up your Twitter API access
        - There are currently four Twitter API access methods (you can learn more about them [here](https://developer.twitter.com/en/docs/twitter-api/getting-started/getting-access-to-the-twitter-api)): CapitalGuard uses the Standard v1.1 access, which requires you to authenticate your Twitter API requests with keys and tokens from within the CapitalGuard developer app.
    4. Make requests to the Twitter API to retrieve tweets
       - Because CapitalGuard is built in Java, and the Twitter API currently maintains only JavaScript, Python, and Ruby official libraries and SDKs, I chose to use the [Twitter4J](https://twitter4j.org/en/index.html) community-supported Java library to integrate the Twitter API into CapitalGuard, as it implements all features of the Standard v1.1 API. Other community-supported Java libraries for the Twitter API include [twitter-client](https://github.com/redouane59/twitter-client) and [JTwitter](https://github.com/winterstein/JTwitter). To learn more about how to make requests to and receive responses from the Twitter API via the Twitter4J library, go to the [Twitter4J section](TODO: Add link) of this README file.

### Important Libraries
- **[AnyChart JS](https://www.anychart.com/products/anychart/overview/)**
  - CapitalGuard uses the AnyChart JavaScript library to create interactive visualizations of users' spending data retrieved using the Plaid API, which help users gain a better understanding of their spending behavior.
  -  The AnyChart library was installed in CapitalGuard with the following Gradle dependency:
  ```
  implementation 'com.github.AnyChart:AnyChart-Android:0.0.3'
  ```
  - Alternatively, you can download the [latest AAR file](https://github.com/AnyChart/AnyChart-Android/raw/master/builds/anychart-library.aar) and copy it into the libs folder of the CapitalGuard project. To learn how to do this in Android Studio, you can read the "JAR/ARR File" section of AnyChart's documentation [here](https://www.anychart.com/technical-integrations/samples/android-charts/).

- **[Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/)**
  - CapitalGuard uses the Stanford CoreNLP library to perform sentiment analysis on news articles, retrieved using the News API, and Twitter tweets, retrieved using the Twitter API via the Twitter4J Java library, to provide users with advice on potential investment opportunities.
  - Specifically, CapitalGuard utilizes the following linguistic annotation capabilities of the [CoreNLP pipeline](https://stanfordnlp.github.io/CoreNLP/pipeline.html), which is the centerpiece of CoreNLP that takes in raw text, runs a series of NLP annotators on the raw text, and outputs full annotation objects:
    - [Tokenization](https://stanfordnlp.github.io/CoreNLP/tokenize.html)
      - Tokenization is the process of splitting raw text into smaller units called tokens, which are defined by an elaborate collection of rules (based on [UD 2.0 specifications](https://lindat.mff.cuni.cz/repository/xmlui/handle/11234/1-1983
)). In general, however, CoreNLP tokens represent individual words. For example, `"CapitalGuard is a personal finance app"` would be tokenized into `"CapitalGuard"`, `"is"`, `"a"`, `"personal"`, `"finance"`, `"app"`, `"."`.
    - [Sentence splitting](https://stanfordnlp.github.io/CoreNLP/ssplit.html)
      - Sentence splitting is the process of separating text into sentences based on a set of rules, which you can learn about more [here](https://stanfordnlp.github.io/CoreNLP/ssplit.html#options).
    - [Parts of speech (POS)](https://stanfordnlp.github.io/CoreNLP/pos.html)
      - POS tagging assigns part-of-speech labels to tokens (e.g. whether they are verbs or nouns). For example, in the sentence `"CapitalGuard helps its users save money"`, `"CapitalGuard"` would be assigned the NNP (Proper noun, singular) tag, `"helps"` would be assigned the VBZ (Verb, present tense, third person singular) tag, `"its"` would be assigned the PRP$ (Possessive pronoun, also PP$) tag, `"users"` would be assigned the NNS (Noun, plural) tag, `"save"` would be assigned the VB (Verb, base form) tag, and `"money"` would be assigned the NN (Noun, singular) tag. To learn more about POS tag sets, you can go [here](https://www.ibm.com/docs/en/wca/3.5.0?topic=analytics-part-speech-tag-sets).
    - [Constituency and dependency parsing](https://stanfordnlp.github.io/CoreNLP/parse.html)
      - Constituency parsing provides a full syntactic analysis and phrase-structure tree parse of sentences, which is saved in TreeAnnotation as binary parse trees. Since a conversion from constituency parses to dependency parses is available in CoreNLP for the English language, a dependency representation is also created by the constituency conversion. Dependency parsing in CoreNLP produce three types of outputs: Basic dependencies (saved in BasicDependenciesAnnotation), enhanced dependencies (saved in EnhancedDependenciesAnnotation), and enhanced++ dependencies (saved in EnhancedPlusPlusDependenciesAnnotation). 
    - [Sentiment](https://stanfordnlp.github.io/CoreNLP/sentiment.html)
      - The CoreNLP library provides a sentiment tool that implements [Socher et al’s sentiment analysis model](https://nlp.stanford.edu/~socherr/EMNLP2013_RNTN.pdf). The sentiment tool attaches the analyzed sentence's binarized tree (created with the parsing annotator) to the `CoreMap`: This process adds annotations from `RNNCoreAnnotations`, which indicate the predicted class and sentiment scores for each subtree, to the nodes of the tree. You can learn more about Stanford CoreNLP's sentiment tool [here](https://nlp.stanford.edu/sentiment/).
  - The Stanford CoreNLP library was installed in CapitalGuard with the following Gradle dependencies:
  ```
  implementation ('edu.stanford.nlp:stanford-corenlp:4.2.2')
  
  implementation ('edu.stanford.nlp:stanford-corenlp:4.2.2:models')
  ```
      
- **[Twitter4J](https://twitter4j.org/en/index.html)**
  - CapitalGuard uses the Twitter4J library to integrate CapitalGuard with the Standard v1.1 Twitter API in order to programmatically retrieve tweets.
  - To use the Twitter API via Twitter4J in CapitalGuard, in the `getTweetSentiment` method in AdviceActivity.java, you will need to set the argument of:
    - `setOAuthConsumerKey` method to your Twitter API Key
    - `setOAuthConsumerSecret` method to your Twitter API Secret (Key)
    - `setOAuthAccessToken` method to your Access Token
    - `setOAuthAccessTokenSecret` method to your Access Token Secret
    
    (*Note: If you are unsure about where to find the above Twitter API keys and tokens, make sure you read the Twitter API section of this README file.*)
  - The Twitter4J library was installed in CapitalGuard with the following Gradle dependency:
  ```
  implementation 'org.twitter4j:twitter4j-core:4.0.7'
  ```
 - Alternatively, I could have downloaded and added the [twitter4j-core-4.0.7.jar](https://twitter4j.org/archive/twitter4j-4.0.7.zip) file to CapitalGuard's classpath.
