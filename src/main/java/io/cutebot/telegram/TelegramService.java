package io.cutebot.telegram;

import io.cutebot.telegram.exception.TgBotNotFoundException;
import io.cutebot.telegram.handlers.SetWebhookDto;
import io.cutebot.telegram.tgmodel.TgMessage;
import io.cutebot.telegram.tgmodel.TgResponseUpdate;
import io.cutebot.telegram.tgmodel.TgSendAnimation;
import io.cutebot.telegram.tgmodel.TgSendPhoto;
import io.cutebot.telegram.tgmodel.TgSendTextMessage;
import io.cutebot.telegram.tgmodel.TgUser;
import io.cutebot.telegram.tgmodel.inline.TgAnswerInlineQuery;
import io.cutebot.telegram.tgmodel.response.TgResponseMessage;
import io.cutebot.telegram.tgmodel.response.TgResponseUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);

    private RestTemplate restTemplate;

    private String webhookUrl;

    @Inject
    public TelegramService(@Value("${telegram.webhook.url}") String webhookUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(65000);
        restTemplate = new RestTemplate(factory);
        this.webhookUrl = webhookUrl;
    }

    public TgUser getMe(String token) {
        return getMethod(token, "getMe", TgResponseUser.class).result;
    }

    public TgResponseUpdate getUpdates(String token, int offset, int limit, int timeout) {
        String method = "getUpdates?offset=" + offset + "&limit=" + limit + "&timeout=" + timeout;
        try {
            return getMethod(token, method, TgResponseUpdate.class);
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 401) {
                throw new TgBotNotFoundException();
            }
            throw new RuntimeException(e);
        }
    }

    public TgMessage sendPhoto(String token, TgSendPhoto sendPhoto) {
        return postMethod(token, sendPhoto, "sendPhoto", TgResponseMessage.class).result;
    }

    public TgMessage sendAnimation(String token, TgSendAnimation animation) {
        return postMethod(token, animation, "sendAnimation", TgResponseMessage.class).result;
    }

    public TgMessage sendMessage(String token, TgSendTextMessage sendMessage) {
        try {
            return postMethod(token, sendMessage, "sendMessage", TgResponseMessage.class).result;
        } catch (HttpClientErrorException e) {
            log.warn("error during sendMessage", e);
            if (e.getRawStatusCode() == 403) {
                log.info("Blocked by user: {}", e.getResponseBodyAsString());
            }
            if (e.getRawStatusCode() == 400) {
                log.info("Chat not found: {}", e.getResponseBodyAsString());
            }
            throw e;
        }
    }

    public TgMessage updateMessage(String botToken, TgSendTextMessage tgSendMessage, Long updateMessageId) {
        tgSendMessage.messageId = updateMessageId;
        try {
            return postMethod(botToken, tgSendMessage, "editMessageText", TgResponseMessage.class).result;
        } catch (HttpClientErrorException e) {
            log.warn("error during editMessageText", e);
            if (e.getRawStatusCode() == 403) {
                log.info("Blocked by user: {}", e.getResponseBodyAsString());
            }
            if (e.getRawStatusCode() == 400) {
                log.info("Chat not found: {}", e.getResponseBodyAsString());
            }
            throw e;
        }

    }


    public void answerInlineQuery(String token, TgAnswerInlineQuery tgAnswerInlineQuery) {
        try {
            String resp = postMethod(token, tgAnswerInlineQuery, "answerInlineQuery", String.class);
        } catch (HttpClientErrorException e) {
            log.warn("error during answerInlineQuery", e);
            if (e.getRawStatusCode() == 403) {
                log.info("Blocked by user: {}", e.getResponseBodyAsString());
            }
            if (e.getRawStatusCode() == 400) {
                log.info("Chat not found: {}", e.getResponseBodyAsString());
            }
            throw e;
        }
    }

    public void setWebhook(String token) {
        SetWebhookDto setWebhookDto = new SetWebhookDto();
        setWebhookDto.url = webhookUrl + "/webhook/" + token;
        postMethod(token, setWebhookDto, "setWebhook", Void.class);
    }

    public void deleteWebhook(String token) {
        getMethod(token, "deleteWebhook", Void.class);
    }

    private <T> T postMethod(String token, Object request, String methodName, Class<T> clazz) {
        String url = "https://api.telegram.org/bot" + token + "/" + methodName;
        log.info("POST {} to {}", request, url);
        try {
            T response = restTemplate.postForEntity(url, request, clazz).getBody();
            log.info("POST {} RESPONSE {}", methodName, response);
            return response;
        } catch (HttpClientErrorException e) {
            log.info(e.getResponseBodyAsString());
            throw e;
        }
    }

    private <T> T getMethod(String token, String methodName, Class<T> clazz) {
        String url = "https://api.telegram.org/bot" + token + "/" + methodName;
        log.info("GET {}", url);
        try {
            T response = restTemplate.getForEntity(url, clazz).getBody();
            log.info("GET {} RESPONSE {}", methodName, response);
            return response;
        } catch (HttpClientErrorException e) {
            log.info("Error telegram api. GET {}. Response {}", url, e.getResponseBodyAsString());
            throw e;
        }
    }

}
