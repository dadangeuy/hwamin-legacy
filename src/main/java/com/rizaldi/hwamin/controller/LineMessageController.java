package com.rizaldi.hwamin.controller;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@LineMessageHandler
public class LineMessageController {

    @EventMapping
    public Message handle(MessageEvent<TextMessageContent> event) {
        return new TextMessage(event.getMessage().getText());
    }
}
