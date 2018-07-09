package io.cutebot.simpleanswer.service;

import io.cutebot.telegram.TelegramService;
import io.cutebot.telegram.tgmodel.TgResponseUpdate;
import io.cutebot.telegram.tgmodel.TgSendTextMessage;
import io.cutebot.telegram.tgmodel.TgUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;


@Service
public class BotService {
    private static final Logger log = LoggerFactory.getLogger(BotService.class);

    @Inject
    private TelegramService telegramService;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.message}")
    private String message;

    public void handle(TgUpdate update) {
        TgSendTextMessage msg = new TgSendTextMessage();
        msg.text = message;
        if (update.message != null) {
            msg.chatId = update.message.chat.id;
            telegramService.sendMessage(botToken, msg);
        }
        if (update.callbackQuery != null) {
            msg.chatId = update.callbackQuery.from.id;
            telegramService.sendMessage(botToken, msg);
        }
    }

    public TgResponseUpdate getMessages(int offset, Integer timeout, int limit) {
        return telegramService.getUpdates(botToken, offset, limit, timeout);
    }

    public void stopWithWebhook() {
        telegramService.deleteWebhook(botToken);
    }

    public void startWithWebhook() {
        telegramService.setWebhook(botToken);
    }
}
