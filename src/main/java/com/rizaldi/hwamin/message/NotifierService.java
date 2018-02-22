package com.rizaldi.hwamin.message;

import com.linecorp.bot.model.message.TextMessage;
import com.rizaldi.hwamin.helper.Emoji;
import com.rizaldi.hwamin.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class NotifierService {
    @Autowired
    private MessageQueueService messageQueue;
    @Autowired
    private UserService userService;
    private Map<String, Long> timers = new ConcurrentHashMap<>();

    public void checkNotification(Map<String, Object> session) {
        String sessionId = (String) session.get("sessionId");
        if (timers.containsKey(sessionId)) {
            long timer = timers.get(sessionId);
            if (System.currentTimeMillis() > timer) sendNotification(session);
        } else initTimer(session);
    }

    private void initTimer(Map<String, Object> session) {
        String sessionId = (String) session.get("sessionId");
        timers.put(sessionId, System.currentTimeMillis());
    }

    public void resetTimer(Map<String, Object> session) {
        String sessionId = (String) session.get("sessionId");
        timers.put(sessionId, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12));
    }

    private void sendNotification(Map<String, Object> session) {
        String userId = (String) session.get("userId");
        messageQueue.addQueue(session, new TextMessage("bosen nih, main yuk " + userService.getUserName(userId) + "!" + Emoji.please));
        messageQueue.addQueue(session, new TextMessage("bilang 'main' ya kalau mau main sama aku" + Emoji.kiss));
        messageQueue.finishQueueing(session);
        resetTimer(session);
    }
}
