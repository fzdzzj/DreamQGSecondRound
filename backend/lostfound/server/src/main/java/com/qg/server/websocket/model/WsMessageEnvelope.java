package com.qg.server.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * WebSocket 消息封包
 *
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsMessageEnvelope<T> {

    private String type;
    private T data;
    private LocalDateTime timestamp;

    public static <T> WsMessageEnvelope<T> of(String type, T data) {
        return new WsMessageEnvelope<>(type, data, LocalDateTime.now());
    }
}
