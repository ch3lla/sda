package com.streaming.data.app.sda.websocket;

import com.streaming.data.app.sda.config.StreamingConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures WebSocket endpoint mappings.
 */
@Configuration
public class WebSocketConfig {
    private final StreamingConfig streamingConfig;

    public WebSocketConfig(StreamingConfig streamingConfig) {
        this.streamingConfig = streamingConfig;
    }

    /**
     * Maps the WebSocket handler to the configured path.
     *
     * @param handler the WebSocket handler for processing messages
     * @return handler mapping for WebSocket endpoints
     */
    @Bean
    public HandlerMapping webSocketHandlerMapping(DataWebSocketHandler handler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put(streamingConfig.getWebsocket().getPath(), handler);

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setUrlMap(map);
        handlerMapping.setOrder(1);

        return handlerMapping;
    }
}
