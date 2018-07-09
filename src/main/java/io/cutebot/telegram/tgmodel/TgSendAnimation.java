package io.cutebot.telegram.tgmodel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TgSendAnimation extends TgSendMessage {
    public String caption;

    @JsonProperty("parse_mode")
    public String parseMode = "HTML";

    public String animation;

    @Override
    public String toString() {
        return "TgSendPhoto{" +
                "caption='" + caption + '\'' +
                ", parseMode='" + parseMode + '\'' +
                ", animation='" + animation + '\'' +
                '}';
    }

}
