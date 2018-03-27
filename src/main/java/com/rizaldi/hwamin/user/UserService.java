package com.rizaldi.hwamin.user;

import com.linecorp.bot.client.LineMessagingClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {
    private final LineMessagingClient client;
    private final UserRepository repository;

    @Autowired
    public UserService(LineMessagingClient client,
                       UserRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    public CompletableFuture<User> fetchUser(Map<String, Object> session) {
        CompletableFuture<User> futureResponse;
        switch ((String) session.get("sessionType")) {
            case "group":
                futureResponse = resolveFromGroup(session);
                break;
            case "room":
                futureResponse = resolveFromRoom(session);
                break;
            default:
                futureResponse = resolveFromFollower(session);
                break;
        }
        futureResponse.thenAcceptAsync(this::updateUser);
        return futureResponse;
    }

    @Cacheable(value = "user", key = "#userId")
    public Optional<User> getUser(String userId) {
        return repository.findById(userId);
    }

    public String getUserName(String userId) {
        Optional<User> user = getUser(userId);
        return user.isPresent() ? user.get().getDisplayName() : "unknown";
    }

    @SuppressWarnings("WeakerAccess")
    @CachePut(value = "user", key = "#user.userId")
    public Optional<User> updateUser(User user) {
        repository.save(user);
        return Optional.of(user);
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
