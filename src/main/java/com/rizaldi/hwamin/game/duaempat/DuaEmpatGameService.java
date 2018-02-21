package com.rizaldi.hwamin.game.duaempat;

import com.linecorp.bot.model.message.TextMessage;
import com.rizaldi.hwamin.game.BaseScoreboard;
import com.rizaldi.hwamin.helper.Emoji;
import com.rizaldi.hwamin.message.MessageFactoryService;
import com.rizaldi.hwamin.message.MessageQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DuaEmpatGameService {
    @Autowired
    private DuaEmpatLogicService duaEmpatLogic;
    @Autowired
    private MessageQueueService messageQueue;
    @Autowired
    private MessageFactoryService messageFactory;
    private Map<String, BaseScoreboard> scores = new ConcurrentHashMap<>();
    private Map<String, List<Integer>> questions = new ConcurrentHashMap<>();

    public void startSession(Map<String, Object> session) {
        String sessionId = (String) session.get("sessionId");
        BaseScoreboard scoreboard = new BaseScoreboard();
        scoreboard.setId(sessionId);
        scores.put(sessionId, scoreboard);
        questions.put(sessionId, duaEmpatLogic.getQuestion());
        messageQueue.addQueue(session, new TextMessage(getRules()));
        messageQueue.addQueue(session, new TextMessage(getQuestion(session)));
        messageQueue.finishQueueing(session);
    }

    public void endSession(Map<String, Object> session) {
        String sessionId = (String) session.get("sessionId");
        messageQueue.addQueue(session, new TextMessage("yah kok udahan" + Emoji.tired));
        messageQueue.addQueue(session, new TextMessage(messageFactory.getScoreboard(scores.get(sessionId))));
        messageQueue.finishQueueing(session);
        expiredSession(sessionId);
    }

    public void expiredSession(String sessionId) {
        scores.remove(sessionId);
        questions.remove(sessionId);
    }

    public void answer(Map<String, Object> session, String answer) throws Exception {
        String sessionId = (String) session.get("sessionId"),
                userId = (String) session.get("userId");
        try {
            boolean result = duaEmpatLogic.isCorrectAnswer(questions.get(sessionId), answer);
            if (result) {
                scores.get(sessionId).correctScoreForId(userId);
                messageQueue.addQueue(session, new TextMessage("jawaban " + messageFactory.getName(userId) + " benar" + Emoji.shocked));
                messageQueue.addQueue(session, new TextMessage(messageFactory.getScoreboard(scores.get(sessionId))));
                questions.put(sessionId, duaEmpatLogic.getQuestion());
                messageQueue.addQueue(session, new TextMessage(getQuestion(session)));
                messageQueue.finishQueueing(session);
            } else {
                scores.get(sessionId).wrongScoreForId(userId);
                messageQueue.addQueue(session, new TextMessage("jawaban " + messageFactory.getName(userId) + " salah" + Emoji.arrogant));
                messageQueue.finishQueueing(session);
            }
        } catch (Exception e) {
            throw new Exception("Answer format is not valid (can be ignored)");
        }
    }

    public void giveUp(Map<String, Object> session) {
        String sessionId = (String) session.get("sessionId"),
                userId = (String) session.get("userId");
        scores.get(sessionId).giveUpScoreForId(userId);
        questions.put(sessionId, duaEmpatLogic.getQuestion());
        messageQueue.addQueue(session, new TextMessage("dasar " + messageFactory.getName(userId) + " lemah" + Emoji.sick));
        messageQueue.addQueue(session, new TextMessage(messageFactory.getScoreboard(scores.get(sessionId))));
        messageQueue.addQueue(session, new TextMessage(getQuestion(session)));
        messageQueue.finishQueueing(session);
    }

    private String getRules() {
        return Emoji.mad + "PERATURAN!!" + Emoji.mad + "\naku akan kasih 4 angka, kamu buat hasilnya jadi 24 dengan" +
                " operasi +-*/() atau jawab 'tidak ada', yang paling duluan jawab yang menang lho, nanti bilang 'nyerah'" +
                " kalau bingung atau 'udahan' kalau udah capek main ya" + Emoji.laugh;
    }

    private String getQuestion(Map<String, Object> session) {
        String sessionId = (String) session.get("sessionId");
        List<Integer> question = questions.get(sessionId);
        return Emoji.blink + "SOAL" + Emoji.blink + "\n" + question.get(0) + ", " + question.get(1) + ", " + question.get(2) + ", " + question.get(3);
    }
}
