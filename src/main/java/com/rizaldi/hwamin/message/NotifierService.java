package com.rizaldi.hwamin.message;

import com.linecorp.bot.model.message.TextMessage;
import com.rizaldi.hwamin.helper.Emoji;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class NotifierService {
    @Autowired
    private MessageQueueService messageQueue;
    private Map<String, Long> timers = new ConcurrentHashMap<>();

    public void checkNotification(Map<String, Object> session) {
        String sessionId = (String) session.get("sessionId");
        if (timers.containsKey(sessionId)) {
            long timer = timers.get(sessionId);
            if (System.currentTimeMillis() > timer) sendNotification(session);
        } else resetTimer(session);
    }

    public void resetTimer(Map<String, Object> session) {
        String sessionId = (String) session.get("sessionId");
        timers.put(sessionId, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12));
    }

    private void sendNotification(Map<String, Object> session) {
        messageQueue.addQueue(session, new TextMessage("bosen nih, main yuk!" + Emoji.please));
        messageQueue.addQueue(session, new TextMessage("cukup bilang 'main' untuk lihat game2 yang aku punya" + Emoji.kiss));
        messageQueue.finishQueueing(session);
        resetTimer(session);
    }
}
