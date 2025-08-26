package com.streaming.data.app.sda.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaming.data.app.sda.model.MarketData;
import com.streaming.data.app.sda.service.DataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

/**
 * WebSocket handler that streams market data to connected clients
 * and processes incoming messages if needed.
 */
@Component
public class DataWebSocketHandler implements WebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(DataWebSocketHandler.class);

    private final DataGenerator marketDataGenerator;
    private final ObjectMapper objectMapper;

    public DataWebSocketHandler(DataGenerator marketDataGenerator,
                                      ObjectMapper objectMapper) {
        this.marketDataGenerator = marketDataGenerator;
        this.objectMapper = objectMapper;
    }

    /**
     * Converts a MarketData object to its JSON representation.
     *
     * @param data the market data to convert
     * @return JSON string of the data, or "{}" if serialization fails
     */
    private String convertToJson(MarketData data){
        try{
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            logger.error("Error converting market data to JSON", e);
            return "{}";
        }
    }

    /**
     * Handles a WebSocket session by streaming market data messages to the client.
     *
     * @param session the WebSocket session for the connected client
     * @return a completion signal when the session is closed
     */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        logger.info("Websocket handler received for session {}", session.getId());

        return session.send(
                marketDataGenerator.generateMarketDataStream()
                        .map(this::convertToJson)
                        .map(session::textMessage)
                        .doOnNext(message -> logger.trace("Sending: {}", message))
                        .onErrorContinue((error, obj) -> logger.error("Error sending message: ", error))
        ).doOnTerminate(() -> logger.info("Websocket handler closed for session {}", session.getId()));
    }
}
