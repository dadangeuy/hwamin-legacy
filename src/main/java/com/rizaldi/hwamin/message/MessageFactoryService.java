package com.rizaldi.hwamin.message;

import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Service
public class MessageFactoryService {
    @Getter
    private List<Message> gameOptions = new LinkedList<>();

    public MessageFactoryService() {
        setGameOptions();
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
