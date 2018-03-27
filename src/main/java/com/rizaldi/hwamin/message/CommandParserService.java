package com.rizaldi.hwamin.message;

import com.linecorp.bot.model.message.TextMessage;
import com.rizaldi.hwamin.game.duaempat.DuaEmpatGameService;
import com.rizaldi.hwamin.helper.Emoji;
import com.rizaldi.hwamin.user.UserService;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CommandParserService {
    private final MessageFactoryService messageFactory;
    private final MessageQueueService messageQueue;
    private final DuaEmpatGameService duaEmpatGame;
    private final NotifierService notifier;
    private final UserService userService;
    private final Map<String, String> gameSession;

    @Autowired
    public CommandParserService(MessageFactoryService messageFactory,
                                MessageQueueService messageQueue,
                                DuaEmpatGameService duaEmpatGame,
                                NotifierService notifier,
                                UserService userService) {
        this.messageFactory = messageFactory;
        this.messageQueue = messageQueue;
        this.duaEmpatGame = duaEmpatGame;
        this.notifier = notifier;
        this.userService = userService;
        this.gameSession = ExpiringMap.builder()
                .expiration(12, TimeUnit.HOURS)
                .expirationPolicy(ExpirationPolicy.CREATED)
                .expirationListener((String sessionId, String game) -> {
                    switch (game) {
                        case "duaempat":
                            duaEmpatGame.expiredSession(sessionId);
                            break;
                        default:
                            break;
                    }
                })
                .build();
    }

    public void handle(Map<String, Object> session, String command) {
        command = command.replaceAll("(\\t|\\r?\\n)+", " ").trim().toLowerCase();
        String sessionId = (String) session.get("sessionId");
        boolean inGame = gameSession.containsKey(sessionId);
        if (inGame) {
            String game = gameSession.get(sessionId);
            switch (game) {
                case "duaempat":
                    switch (command) {
                        case "main":
                            messageQueue.addQueue(session, messageFactory.getGameAlreadyStarted());
                            messageQueue.finishQueueing(session);
                            break;
                        case "main 24":
                            duaEmpatGame.viewQuestion(session);
                            break;
                        case "nyerah":
                            duaEmpatGame.giveUp(session);
                            break;
                        case "udahan":
                            duaEmpatGame.endSession(session);
                            gameSession.remove(sessionId);
                            break;
                        default:
                            try {
                                duaEmpatGame.answer(session, command);
                            } catch (Exception ignore) {}
                            break;
                    }
                    break;
                default:
                    break;
            }
        } else {
            switch (command) {
                case "main":
                    messageQueue.addQueue(session, messageFactory.getGameOptions());
                    messageQueue.finishQueueing(session);
                    notifier.resetTimer(session);
                    break;
                case "main 24":
                    duaEmpatGame.startSession(session);
                    gameSession.put(sessionId, "duaempat");
                    notifier.resetTimer(session);
                    break;
                case "halo hwamin":
                    userService.fetchUser(session).thenAcceptAsync(user -> {
                        messageQueue.addQueue(session, new TextMessage("halo juga " + user.getDisplayName() + Emoji.flyKiss));
                        messageQueue.finishQueueing(session);
                    });
                    notifier.resetTimer(session);
                    break;
                default:
                    notifier.checkNotification(session);
                    break;
            }
        }
    }
}
