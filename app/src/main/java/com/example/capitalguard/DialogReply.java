package com.example.capitalguard;

import com.google.cloud.dialogflow.v2.DetectIntentResponse;

public interface DialogReply {
    void callback(DetectIntentResponse returnResponse);
}
