package com.example.capitalguard;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.os.Bundle;
import android.widget.Toast;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.common.collect.Lists;

import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity implements DialogReply {
    RecyclerView chatView;
    ChatAdapter chatAdapter;
    List<MessageActivity> messageList = new ArrayList<>();
    EditText editMessage;
    ImageButton btnSend;

    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private String uuid = UUID.randomUUID().toString();
    private  String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_activity);
        chatView = findViewById(R.id.chatView);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);

        chatAdapter = new ChatAdapter(messageList, this);
        chatView.setAdapter(chatAdapter);

        btnSend.setOnClickListener(new View.OnClickListener() {
            /**
            * Send the user-inputted message to the Dialogflow API via the sendMessageToDialogBot
            * function when the send button is clicked.
            */
            @Override public void onClick(View view) {
                String message = editMessage.getText().toString();
                if (!message.isEmpty()) {
                    messageList.add(new MessageActivity(message,false));
                    editMessage.setText("");
                    sendMessageToDialogBot(message);
                    Objects.requireNonNull(chatView.getAdapter()).notifyDataSetChanged();
                    Objects.requireNonNull(chatView.getLayoutManager())
                            .scrollToPosition(messageList.size()-1);
                } else {
                    Toast.makeText(ChatActivity.this, "Please enter a message!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        setUpDialogBot();
    }

    /**
    * Set up the financial assistant chatbot via the DialogFlow API.
    */
    private void setUpDialogBot() {
        try {
            InputStream stream = this.getResources().openRawResource(R.raw.capitalguardcredential);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            String projectID = ((ServiceAccountCredentials) credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(
                    FixedCredentialsProvider.create(credentials)).build();
            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(projectID, uuid);
        } catch (Exception e) {
            Log.d(TAG, "setUpDialogBot " + e.getMessage());
        }
    }

    /**
    * Send the user's message to the Dialogflow API asynchronously, where the message is
    * processed by natural language understanding models.
    */
    private void sendMessageToDialogBot(String message) {
        QueryInput input = QueryInput.newBuilder()
                .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build();
        new SendMessageAsync(this, sessionName, sessionsClient, input).execute();
    }

    /**
    * Process and update the chat UI with the intent response from the Dialogflow API, which
    * results from the identified intent of the user's message getting mapped to its
    * corresponding Node.js Cloud Function, via the callback method of the DialogReply interface.
    */
    @Override
    public void callback(DetectIntentResponse returnResponse) {
        if (returnResponse != null) {
            String dialogReply = returnResponse.getQueryResult().getFulfillmentText();
            if (!dialogReply.isEmpty()) {
                messageList.add(new MessageActivity(dialogReply, true));
                chatAdapter.notifyDataSetChanged();
                Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(
                        messageList.size() - 1);
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to connect!", Toast.LENGTH_SHORT).show();
        }
    }
}

