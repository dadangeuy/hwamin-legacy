package com.rizaldi.hwamin.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document
@Data
public class BaseScoreboard {
    @Id
    private String id;
    private Map<String, Integer> scores = new HashMap<>();
    private int correctScore = 10;
    private int wrongScore = -5;
    private int giveUpScore = -3;

    public void correctScoreForId(String userId) {
        scores.put(userId, scores.getOrDefault(userId, 0) + correctScore);
    }

    public void wrongScoreForId(String userId) {
        scores.put(userId, scores.getOrDefault(userId, 0) + wrongScore);
    }

    public void giveUpScoreForId(String userId) {
        scores.put(userId, scores.getOrDefault(userId, 0) + giveUpScore);
    }
}
