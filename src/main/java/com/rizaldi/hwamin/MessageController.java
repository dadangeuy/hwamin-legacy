package com.rizaldi.hwamin;

import com.linecorp.bot.model.event.*;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.rizaldi.hwamin.helper.Emoji;
import com.rizaldi.hwamin.helper.Sticker;
import com.rizaldi.hwamin.message.CommandParserService;
import com.rizaldi.hwamin.message.MessageQueueService;
import com.rizaldi.hwamin.user.UserResolverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
@LineMessageHandler
public class MessageController {
    @Autowired
    private UserResolverService userResolver;
    @Autowired
    private CommandParserService commandParser;
    @Autowired
    private MessageQueueService messageQueue;

    @EventMapping
    public void handle(MessageEvent<TextMessageContent> event) {
        Map<String, Object> session = getSessionFromEvent(event);
        userResolver.resolveFrom(session);
        messageQueue.createQueue(session);
        try {
            commandParser.handle(session, event.getMessage().getText());
        } catch (Exception e) {
            messageQueue.addQueue(session, getErrorMessages());
            messageQueue.finishQueueing(session);
        }
    }

    @EventMapping
    public List<Message> handle(FollowEvent event) {
        return Arrays.asList(
                Sticker.welcome,
                new TextMessage("사랑해 aku hwamin, thanks udah kamu follow " + Emoji.flyKiss));
    }

    // TODO: set flag
    @EventMapping
    public void handle(UnfollowEvent event) {}

    @EventMapping
    public List<Message> handle(JoinEvent event) {
        return Arrays.asList(
                Sticker.blushing,
                new TextMessage("사랑합니다 aku hwamin, makasih udah kamu invite ke grup " + Emoji.kiss));
    }

    // TODO: remove group/room from session
    @EventMapping
    public void handle(LeaveEvent event) {}

    private List<Message> getErrorMessages() {
        return Collections.singletonList(new TextMessage("duh kayaknya lagi ada gangguan server " + Emoji.panic));
    }

    private Map<String, Object> getSessionFromEvent(Event event) {
        Map<String, Object> session = new HashMap<>();
        session.put("userId", event.getSource().getUserId());
        if (event.getSource() instanceof GroupSource) {
            session.put("sessionId", ((GroupSource) event.getSource()).getGroupId());
            session.put("sessionType", "group");
        } else if (event.getSource() instanceof RoomSource) {
            session.put("sessionId", ((RoomSource) event.getSource()).getRoomId());
            session.put("sessionType", "room");
        } else {
            session.put("sessionId", event.getSource().getUserId());
            session.put("sessionType", "private");
        }
        session.put("userId", event.getSource().getUserId());
        session.put("timestamp", event.getTimestamp().toEpochMilli());
        if (event instanceof ReplyEvent) session.put("replyToken", ((ReplyEvent) event).getReplyToken());
        return session;
    }
}
