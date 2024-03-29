package com.rizaldi.hwamin.controller;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class MessageOutController {
    private final LineMessagingClient client;
    private final Map<String, List<Message>> messagesQueue = new ConcurrentHashMap<>();

    @Autowired
    public MessageOutController(LineMessagingClient client) {this.client = client;}

    public void createQueue(Map<String, Object> session) {
        messagesQueue.put(getTokenFrom(session), new LinkedList<>());
    }

    public void addQueue(Map<String, Object> session, List<Message> messages) {
        messagesQueue.get(getTokenFrom(session)).addAll(messages);
    }

    public void addQueue(Map<String, Object> session, Message message) {
        messagesQueue.get(getTokenFrom(session)).add(message);
    }

    public void finishQueueing(Map<String, Object> session) {
        String token = getTokenFrom(session);
        client.replyMessage(new ReplyMessage(token, messagesQueue.remove(token)));
    }

    private String getTokenFrom(Map<String, Object> session) {
        return (String) session.get("replyToken");
    }
}
