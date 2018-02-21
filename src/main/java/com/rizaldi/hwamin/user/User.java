package com.rizaldi.hwamin.user;

import com.linecorp.bot.model.profile.UserProfileResponse;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class User {
    @Id
    private String userId;
    private String displayName;
    private String pictureUrl;
    private String statusMessage;

    public User() {}

    private User(UserProfileResponse response) {
        displayName = response.getDisplayName();
        userId = response.getUserId();
        pictureUrl = response.getPictureUrl();
        statusMessage = response.getStatusMessage();
    }

    public static User createFromResponse(UserProfileResponse response) {
        return new User(response);
    }
}
