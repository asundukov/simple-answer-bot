package io.cutebot;

import io.cutebot.simpleanswer.service.BotService;
import io.cutebot.telegram.handlers.LongPollProcess;
import io.cutebot.telegram.handlers.WebhookListener;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.ws.rs.core.Context;

@Configuration
@PropertySource("file:${settingsDir}/application.properties")
public class AppConfig extends ResourceConfig {
    private Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Value("${telegram.longpoll.enable}")
    private Boolean longPollEnable;

    public AppConfig(@Context ConfigurableApplicationContext context) {
        register(WebhookListener.class);
    }

    public void startBots(ConfigurableApplicationContext context) {
        if (longPollEnable) {
            context.getBean(BotService.class).stopWithWebhook();
            context.getBean(LongPollProcess.class).start();
        } else {
            context.getBean(BotService.class).startWithWebhook();
        }
    }

}
