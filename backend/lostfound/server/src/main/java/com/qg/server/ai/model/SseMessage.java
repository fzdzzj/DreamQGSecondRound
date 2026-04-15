package com.qg.server.ai.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SseMessage<T> {

    private String event; // message / done / error
    private T data;

    public static <T> SseMessage<T> message(T data) {
        return new SseMessage<>("message", data);
    }

    public static SseMessage<String> done() {
        return new SseMessage<>("done", "[DONE]");
    }

    public static SseMessage<String> error(String msg) {
        return new SseMessage<>("error", msg);
    }
}
