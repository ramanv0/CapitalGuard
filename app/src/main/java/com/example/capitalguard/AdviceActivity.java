package com.example.capitalguard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;

import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class AdviceActivity extends AppCompatActivity {
    private EditText editTextStockSymbol;
    private TextView analysisResult;

    public interface MyCallback {
        void onCallback(double value);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advice);

        editTextStockSymbol = findViewById(R.id.edit_text_stock_symbol);
        Button buttonSend = findViewById(R.id.button_get_advice);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View progressSpinner = findViewById(R.id.get_advice_progress);
                progressSpinner.setVisibility(View.VISIBLE);

                String symbol = editTextStockSymbol.getText().toString();
                Pattern pattern = Pattern.compile("^([A-Z]{2,4}:(?![A-Z\\d]+\\.))?" +
                        "([A-Z]{1,4}|\\d{1,3}(?=\\.)|\\d{4,})(\\.[A-Z]{2})?$",
                        Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(symbol);
                boolean isTickerForm = matcher.matches();

                if (!symbol.equals("") && editTextStockSymbol.getText() != null && isTickerForm) {
                    getNewsSentiment(new MyCallback() {
                        @Override
                        public void onCallback(double avgSentimentNews) {
                            progressSpinner.setVisibility(View.GONE);

                            try {
                                double avgSentimentTwitter = getTweetSentiment(symbol);
                                int avgSentiment
                                    = (int) Math.round((avgSentimentTwitter + avgSentimentNews) / 2);
                                analysisResult = findViewById(R.id.analysis_result);

                                String message;
                                if (avgSentiment == 0) {
                                    // VERY NEGATIVE
                                    message = "The result of the sentiment analysis performed on " +
                                            "recent news articles and Twitter tweets about " + symbol +
                                            "was VERY NEGATIVE; as a result, we strongly recommend you to " +
                                            "not buy this stock at the moment.";
                                    analysisResult.setText(message);
                                    analysisResult.setTextColor(Color.parseColor("#c70000"));
                                } else if (avgSentiment == 1) {
                                    // NEGATIVE
                                    message = "The result of the sentiment analysis performed on " +
                                            "recent news articles and Twitter tweets about " + symbol +
                                            " was NEGATIVE; as a result, we recommend you to not buy this " +
                                            "stock at the moment.";
                                    analysisResult.setText(message);
                                    analysisResult.setTextColor(Color.parseColor("#F95004"));
                                } else if (avgSentiment == 2) {
                                    // NEUTRAL
                                    message = "The result of the sentiment analysis performed on " +
                                            "recent news articles and Twitter tweets about " + symbol +
                                            " was NEUTRAL; as a result, we do not definitively recommend " +
                                            "against buying this stock. However, we do recommend that you " +
                                            "continue to research this stock before buying.";
                                    analysisResult.setText(message);
                                } else if (avgSentiment == 3) {
                                    // POSITIVE
                                    message = "The result of the sentiment analysis performed on " +
                                            "recent news articles and Twitter tweets about " + symbol +
                                            " was POSITIVE; as a result, we recommend you to continue to " +
                                            "look into buying this stock.";
                                    analysisResult.setText(message);
                                    analysisResult.setTextColor(Color.parseColor("#00ed06"));
                                } else if (avgSentiment == 4) {
                                    // VERY POSITIVE
                                    message = "The result of the sentiment analysis performed on " +
                                            "recent news articles and Twitter tweets about " + symbol +
                                            " was POSITIVE; as a result, we strongly recommend you to " +
                                            "continue to look into buying this stock.";
                                    analysisResult.setText(message);
                                    analysisResult.setTextColor(Color.parseColor("#00C805"));
                                }
                            } catch (TwitterException ignored) {}
                        }
                    }, symbol);
                }
            }
        });
    }

    /**
    * Search for and retrieve news articles that contain symbol (the user-inputted stock ticker
    * symbol) through a News API request. Perform sentiment analysis on each news article in the
    * News API response through calls to calculateSentiment and calculate the average sentiment
    * score for the retrieved articles. Pass the average sentiment score as an argument to the
    * onCallback function of the MyCallback interface instance myCallback.
    */
    public void getNewsSentiment(MyCallback myCallback, String symbol) {
        NewsApiClient newsApiClient = new NewsApiClient("Enter News API key");
        newsApiClient.getEverything(
                new EverythingRequest.Builder()
                        .q("+" + symbol)
                        .language("en")
                        .build(),
                new NewsApiClient.ArticlesResponseCallback() {
                    @Override
                    public void onSuccess(ArticleResponse response) {
                        System.out.println(response.getArticles().get(0).getTitle());
                        double totalSentiment = 0.0;

                        for (Article article : response.getArticles()) {
                            String headline = article.getTitle();
                            String articleDescription = article.getDescription();
                            totalSentiment += calculateSentiment(headline + ". "
                                    + articleDescription);
                        }
                        double avgSentimentNews = totalSentiment / response.getArticles().size();
                        myCallback.onCallback(avgSentimentNews);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        System.out.println(throwable.getMessage());
                    }
                }
        );
    }

    /**
    * Retrieve Twitter tweets containing symbol (the user-inputted stock ticker symbol) using
    * the Twitter API via the Twitter4J library. Clean the tweets retrieved by the Twitter4J
    * search query through query parameters and regex via calls to cleanTweets. Perform
    * sentiment analysis on the cleaned tweets and return the average sentiment score for the tweets.
    */
    public double getTweetSentiment(String symbol) throws TwitterException {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true).setOAuthConsumerKey("Enter consumer key")
                .setOAuthConsumerSecret("Enter consumer secret")
                .setOAuthAccessToken("Enter access token")
                .setOAuthAccessTokenSecret("Enter access token secret");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        Query query = new Query(symbol + " -filter:retweets -filter:links -filter:replies -filter:images");
        query.setCount(100);
        query.setLocale("en");
        query.setLang("en");;
        QueryResult queryResult = twitter.search(query);
        List<Status> tweets = queryResult.getTweets();

        double totalSentiment = 0.0;
        for (Status tweetObj : tweets) {
            String tweet = cleanTweet(tweetObj.getText());
            totalSentiment += calculateSentiment(tweet);
        }
        return totalSentiment / tweets.size();
    }

    /**
    * Clean up tweets with regex by removing links, usernames, and '#' from hashtags (leaving only
    * the word), and correcting multiple white spaces to a single white space.
    */
    private String cleanTweet(String tweet) {
        return tweet.trim()
                .replaceAll("http.*?[\\S]+", "")
                .replaceAll("@[\\S]+", "")
                .replaceAll("#", "")
                .replaceAll("[\\s]+", " ");
    }

    /**
    * Perform sentiment analysis on text using the Stanford CoreNLP library's pipeline, via
    * linguistic annotations for text, including token and sentence
    * boundaries (tokenization and sentence splitting, respectively), parts of speech (pos),
    * dependency and constituency parses, and sentiment.
    */
    public static int calculateSentiment(String text) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        if (text != null && text.length() > 0) {
            Annotation annotation = pipeline.process(text);
            for (CoreMap sentence : annotation
                    .get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence
                        .get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                return RNNCoreAnnotations.getPredictedClass(tree);
            }
        }
        return 0;
    }
}
