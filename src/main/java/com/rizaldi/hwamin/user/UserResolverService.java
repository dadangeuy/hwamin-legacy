package com.rizaldi.hwamin.user;

import com.linecorp.bot.client.LineMessagingClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class UserResolverService {
    @Autowired
    private LineMessagingClient client;
    @Autowired
    private UserRepository repository;

    public void resolveFrom(Map<String, Object> session) {
        Optional<User> userData = repository.findById((String) session.get("userId"));
        if (!userData.isPresent()) {
            CompletableFuture<User> futureResponse;
            switch ((String) session.get("sessionType")) {
                case "group": {
                    futureResponse = resolveFromGroup(session);
                    break;
                }
                case "room": {
                    futureResponse = resolveFromRoom(session);
                    break;
                }
                default: {
                    futureResponse = resolveFromFollower(session);
                    break;
                }
            }
            futureResponse.thenAccept(user -> repository.insert(user));
        }
    }

    private CompletableFuture<User> resolveFromFollower(Map<String, Object> session) {
        return client
                .getProfile((String) session.get("userId"))
                .thenApply(User::createFromResponse);
    }

    private CompletableFuture<User> resolveFromGroup(Map<String, Object> session) {
        return client
                .getGroupMemberProfile((String) session.get("sessionId"), (String) session.get("userId"))
                .thenApply(User::createFromResponse);
    }

    private CompletableFuture<User> resolveFromRoom(Map<String, Object> session) {
        return client
                .getRoomMemberProfile((String) session.get("sessionId"), (String) session.get("userId"))
                .thenApply(User::createFromResponse);
    }
}
