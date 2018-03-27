package com.rizaldi.hwamin.message;

import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.rizaldi.hwamin.game.BaseScoreboard;
import com.rizaldi.hwamin.helper.Emoji;
import com.rizaldi.hwamin.user.UserService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class MessageFactoryService {
    private final UserService userService;
    @Getter
    private final List<Message> gameOptions = gameOptions();

    @Autowired
    public MessageFactoryService(UserService userService) {
        this.userService = userService;
    }

    private static List<Message> gameOptions() {
        List<CarouselColumn> columns = Collections.singletonList(getDuaEmpatCarouselColumn());
        return Collections.singletonList(new TemplateMessage("Games", new CarouselTemplate(columns)));
    }

    private static CarouselColumn getDuaEmpatCarouselColumn() {
        return new CarouselColumn(
                "https://s3.amazonaws.com/spoonflower/public/design_thumbnails/0027/0803/rrsandy_maths_sharon_turner_scrummy_things_shop_preview.png",
                "Dua Empat",
                "Gunakan operasi +-*/ untuk membentuk angka 24",
                Collections.singletonList(new MessageAction("Main", "main 24"))
        );
    }

    public TextMessage getScoreboard(BaseScoreboard scoreboard) {
        StringBuilder boardBuilder = new StringBuilder();
        boardBuilder
                .append(Emoji.console)
                .append("SCOREBOARD!!")
                .append(Emoji.console);
        boolean unknownName = false;
        for (Map.Entry<String, Integer> score : scoreboard.getScores().entrySet()) {
            if (!userService.getUser(score.getKey()).isPresent()) unknownName = true;
            boardBuilder
                    .append('\n')
                    .append(userService.getUserName(score.getKey()))
                    .append(": ")
                    .append(score.getValue());
        }
        if (unknownName)
            boardBuilder.append("\n\n*follow dulu atau bilang 'halo hwamin' supaya aku bisa tau nama kamu" + Emoji.please);
        return new TextMessage(boardBuilder.toString());
    }

    public TextMessage getGameAlreadyStarted() {
        return new TextMessage("gamenya udah dimulai!! " + Emoji.mad + " bilang 'udahan' dulu kalau mau ganti game" + Emoji.tired);
    }
}
