package com.streaming.data.app.sda;

import com.streaming.data.app.sda.config.StreamingConfig;
import com.streaming.data.app.sda.model.MarketData;
import com.streaming.data.app.sda.service.DataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

class DataGeneratorTest {

    private DataGenerator marketDataGenerator;
    private StreamingConfig config;

    @BeforeEach
    void setUp() {
        // Create test configuration
        StreamingConfig.SimulationConfig simulationConfig =
                new StreamingConfig.SimulationConfig(50, 100.0, 100.5, 0.5, 100, 1000);

        config = new StreamingConfig(null, null, null, simulationConfig);
        marketDataGenerator = new DataGenerator(config);
    }

    @Test
    void generateMarketDataStreamShouldProduceValidData() {
        Flux<MarketData> dataStream = marketDataGenerator.generateMarketDataStream();

        StepVerifier.create(dataStream.take(5))
                .assertNext(data -> {
                    assertNotNull(data.getTimestamp());
                    assertTrue(data.getBid() > 0);
                    assertTrue(data.getAsk() > data.getBid());
                    assertTrue(data.getVolume() >= 100 && data.getVolume() <= 1000);
                })
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void marketDataShouldMaintainBidAskSpread() {
        Flux<MarketData> dataStream = marketDataGenerator.generateMarketDataStream();

        StepVerifier.create(dataStream.take(10))
                .thenConsumeWhile(data -> data.getAsk() > data.getBid())
                .verifyComplete();
    }
}