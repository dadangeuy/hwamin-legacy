package com.rizaldi.hwamin.message;

import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.rizaldi.hwamin.game.BaseScoreboard;
import com.rizaldi.hwamin.helper.Emoji;
import com.rizaldi.hwamin.user.User;
import com.rizaldi.hwamin.user.UserRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MessageFactoryService {
    @Autowired
    private UserRepository userRepository;
    @Getter
    private List<Message> gameOptions = new LinkedList<>();

    public MessageFactoryService() {
        setGameOptions();
    }

    public String getScoreboard(BaseScoreboard scoreboard) {
        StringBuilder boardBuilder = new StringBuilder();
        boardBuilder
                .append(Emoji.console)
                .append("SCOREBOARD!!")
                .append(Emoji.console);
        for (Map.Entry<String, Integer> score : scoreboard.getScores().entrySet()) {
            boardBuilder
                    .append('\n')
                    .append(getName(score.getKey()))
                    .append(": ")
                    .append(score.getValue());
        }
        return boardBuilder.toString();
    }

    public String getName(String userId) {
        Optional<User> result = userRepository.findById(userId);
        return result.isPresent() ? result.get().getDisplayName() : "unknown";
    }

    private void setGameOptions() {
        List<CarouselColumn> columns = Collections.singletonList(getDuaEmpatCarouselColumn());
        gameOptions.add(new TemplateMessage("Games", new CarouselTemplate(columns)));
    }

    private CarouselColumn getDuaEmpatCarouselColumn() {
        return new CarouselColumn(
                "https://s3.amazonaws.com/spoonflower/public/design_thumbnails/0027/0803/rrsandy_maths_sharon_turner_scrummy_things_shop_preview.png",
                "Dua Empat",
                "Gunakan operasi +-*/ untuk membentuk angka 24",
                Collections.singletonList(new MessageAction("Main", "main 24"))
        );
    }
}
