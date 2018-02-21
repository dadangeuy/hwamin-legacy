package com.rizaldi.hwamin.message;

import com.linecorp.bot.model.message.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CommandParserService {
    @Autowired
    private MessageFactoryService messageFactory;
    @Autowired
    private MessageQueueService messageQueue;

    public void handle(Map<String, Object> session, String command) {
        command = command.replaceAll("(\\t|\\r?\\n)+", " ").trim().toLowerCase();
        switch (command) {
            case "main": {
                messageQueue.addQueue(session, messageFactory.getGameOptions());
                break;
            }
            default: {
                messageQueue.addQueue(session, new TextMessage(command));
                break;
            }
        }
        messageQueue.finishQueueing(session);
    }
}
