package com.rizaldi.hwamin.service;

import com.linecorp.bot.model.message.TextMessage;
import com.rizaldi.hwamin.controller.MessageOutController;
import com.rizaldi.hwamin.helper.Emoji;
import com.rizaldi.hwamin.helper.Solver24;
import com.rizaldi.hwamin.model.BaseScoreboard;
import com.rizaldi.hwamin.repository.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DuaEmpatGameService {
    private final MessageOutController messageQueue;
    private final MessageFactoryService messageFactory;
    private final UserService userService;
    private final Map<String, BaseScoreboard> scores = new ConcurrentHashMap<>();
    private final Map<String, List<Integer>> questions = new ConcurrentHashMap<>();
    private final Solver24 solver = new Solver24();
    private final Random random = new Random();

    @Autowired
    public DuaEmpatGameService(MessageOutController messageQueue,
                               MessageFactoryService messageFactory,
                               UserService userService) {
        this.messageQueue = messageQueue;
        this.messageFactory = messageFactory;
        this.userService = userService;
    }

    public void startSession(Map<String, Object> session) {
        String sessionId = (String) session.get("sessionId");
        BaseScoreboard scoreboard = new BaseScoreboard();
        scoreboard.setId(sessionId);
        scores.put(sessionId, scoreboard);
        createRandomQuestion(session);
        messageQueue.addQueue(session, new TextMessage(getRules()));
        messageQueue.addQueue(session, new TextMessage(getQuestion(session)));
        messageQueue.finishQueueing(session);
    }

    public void endSession(Map<String, Object> session) {
        String sessionId = (String) session.get("sessionId");
        messageQueue.addQueue(session, new TextMessage("yah kok udahan" + Emoji.tired));
        messageQueue.addQueue(session, messageFactory.getScoreboard(scores.get(sessionId)));
        messageQueue.finishQueueing(session);
        expiredSession(sessionId);
    }

    public void viewQuestion(Map<String, Object> session) {
        messageQueue.addQueue(session, new TextMessage(getQuestion(session)));
        messageQueue.finishQueueing(session);
    }

    public void expiredSession(String sessionId) {
        scores.remove(sessionId);
        questions.remove(sessionId);
    }

    public void answer(Map<String, Object> session, String answer) throws Exception {
        String sessionId = (String) session.get("sessionId"),
                userId = (String) session.get("userId");
        try {
            boolean result = solver.isCorrect(questions.get(sessionId), answer);
            if (result) {
                scores.get(sessionId).correctScoreForId(userId);
                messageQueue.addQueue(session, new TextMessage("jawaban " + userService.getUserName(userId) + " benar" + Emoji.shocked));
                messageQueue.addQueue(session, messageFactory.getScoreboard(scores.get(sessionId)));
                createRandomQuestion(session);
                messageQueue.addQueue(session, new TextMessage(getQuestion(session)));
                messageQueue.finishQueueing(session);
            } else {
                scores.get(sessionId).wrongScoreForId(userId);
                messageQueue.addQueue(session, new TextMessage("jawaban " + userService.getUserName(userId) + " salah" + Emoji.arrogant));
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
        scores.get(sessionId).correctScoreForId("hwamin");
        List<Integer> oldQuestion = createRandomQuestion(session);
        messageQueue.addQueue(session, new TextMessage("dasar " + userService.getUserName(userId) + " lemah" + Emoji.sick + "\njawabannya " + solver.getSolution(oldQuestion) + Emoji.arrogant));
        messageQueue.addQueue(session, messageFactory.getScoreboard(scores.get(sessionId)));
        messageQueue.addQueue(session, new TextMessage(getQuestion(session)));
        messageQueue.finishQueueing(session);
    }

    private List<Integer> createRandomQuestion(Map<String, Object> session) {
        String sessionId = (String) session.get("sessionId");
        return questions.put(sessionId,
                             Arrays.asList(random.nextInt(20),
                                           random.nextInt(20),
                                           random.nextInt(20),
                                           random.nextInt(20)));
    }

    private String getRules() {
        return Emoji.shout + "PERATURAN!!" + Emoji.shout + "\naku akan kasih 4 angka, kamu buat hasilnya jadi 24 dengan" +
                " operasi +-*/() atau jawab 'tidak ada', dan yang duluan jawab menang.\nbilang 'nyerah'" +
                " kalau kamu bingung atau 'udahan' kalau udah capek main ya" + Emoji.laugh;
    }

    private String getQuestion(Map<String, Object> session) {
        String sessionId = (String) session.get("sessionId");
        List<Integer> question = questions.get(sessionId);
        return Emoji.blink + "SOAL" + Emoji.blink + "\n" + question.get(0) + ", " + question.get(1) + ", " + question.get(2) + ", " + question.get(3);
    }
}
