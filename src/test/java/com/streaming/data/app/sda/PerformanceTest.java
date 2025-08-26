package com.streaming.data.app.sda;

import com.streaming.data.app.sda.config.StreamingConfig;
import com.streaming.data.app.sda.model.MarketData;
import com.streaming.data.app.sda.service.DataGenerator;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceTest {

    @Test
    void marketDataGeneratorShouldHandleHighFrequencyGeneration() {
        // High-frequency configuration for testing
        StreamingConfig.SimulationConfig simulationConfig =
                new StreamingConfig.SimulationConfig(1, 100.0, 100.5, 0.5, 100, 1000);

        StreamingConfig config = new StreamingConfig(null, null, null, simulationConfig);
        DataGenerator generator = new DataGenerator(config);

        AtomicInteger messageCount = new AtomicInteger(0);
        LocalDateTime startTime = LocalDateTime.now();

        StepVerifier.create(
                        generator.generateMarketDataStream()
                                .take(Duration.ofSeconds(1))
                                .doOnNext(data -> messageCount.incrementAndGet())
                )
                .thenConsumeWhile(data -> {
                    assertNotNull(data.getTimestamp());
                    assertTrue(data.getBid() > 0);
                    assertTrue(data.getAsk() > data.getBid());
                    assertTrue(data.getVolume() > 0);
                    return true;
                })
                .verifyComplete();

        LocalDateTime endTime = LocalDateTime.now();
        Duration testDuration = Duration.between(startTime, endTime);

        System.out.printf("Generated %d messages in %d ms%n",
                messageCount.get(), testDuration.toMinutes());

        // Should generate close to 1000 messages per second (1ms interval)
        assertTrue(messageCount.get() > 500,
                "Should generate at least 500 messages per second");
    }

    @Test
    void memoryUsage_ShouldRemainStableUnderLoad() {
        StreamingConfig.SimulationConfig simulationConfig =
                new StreamingConfig.SimulationConfig(1, 100.0, 100.5, 0.5, 100, 1000);

        StreamingConfig config = new StreamingConfig(null, null, null, simulationConfig);
        DataGenerator generator = new DataGenerator(config);

        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // Process data for 2 seconds
        Flux<MarketData> dataStream = generator.generateMarketDataStream()
                .take(Duration.ofSeconds(2));

        StepVerifier.create(dataStream)
                .thenConsumeWhile(data -> true)
                .verifyComplete();

        // Force garbage collection
        System.gc();
        Thread.yield();

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        System.out.printf("Memory usage: Initial=%d bytes, Final=%d bytes, Increase=%d bytes%n",
                initialMemory, finalMemory, memoryIncrease);

        // Memory increase should be reasonable (less than 10MB)
        assertTrue(memoryIncrease < 10 * 1024 * 1024,
                "Memory usage increase should be less than 10MB");
    }
}