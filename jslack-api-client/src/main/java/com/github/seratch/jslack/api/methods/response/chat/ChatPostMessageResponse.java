package com.github.seratch.jslack.api.methods.response.chat;

import com.github.seratch.jslack.api.methods.SlackApiResponse;
import com.github.seratch.jslack.api.model.ErrorResponseMetadata;
import com.github.seratch.jslack.api.model.Message;
import lombok.Data;

@Data
public class ChatPostMessageResponse implements SlackApiResponse {

    private boolean ok;
    private String warning;
    private String error;
    private String needed;
    private String provided;
    private String deprecatedArgument;

    private ErrorResponseMetadata responseMetadata;

    private String channel;
    private String ts;
    private Message message;
}