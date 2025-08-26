package com.streaming.data.app.sda;

import com.streaming.data.app.sda.config.StreamingConfig;
import com.streaming.data.app.sda.model.MarketData;
import com.streaming.data.app.sda.service.CsvExportService;
import com.streaming.data.app.sda.service.DataGenerator;
import com.streaming.data.app.sda.service.DataProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataProcessingServiceTest {

    @Mock
    private CsvExportService csvExportService;

    @Mock
    private DataGenerator marketDataGenerator;

    private DataProcessingService dataProcessingService;

    @BeforeEach
    void setUp() {
        StreamingConfig.ProcessingConfig processingConfig =
                new StreamingConfig.ProcessingConfig(1000, "BUFFER_OVERFLOW_DROP_LATEST");

        StreamingConfig.CsvConfig csvConfig =
                new StreamingConfig.CsvConfig("./test-data.csv", 1000, 10);

        StreamingConfig config = new StreamingConfig(null, csvConfig, processingConfig, null);

        // Mock market data generation
        when(marketDataGenerator.generateMarketDataStream())
                .thenReturn(Flux.interval(Duration.ofMillis(100))
                        .map(i -> new MarketData(
                                100.0 + i, 100.5 + i, 1000 + i, LocalDateTime.now())));

        dataProcessingService = new DataProcessingService(config, csvExportService, marketDataGenerator);
    }

    @Test
    void processedDataStreamShouldEmitMarketData() {
        dataProcessingService.initialize();

        StepVerifier.create(dataProcessingService.getProcessedDataStream().take(3))
                .expectNextMatches(data -> data.getBid() > 0 && data.getAsk() > data.getBid())
                .expectNextMatches(data -> data.getVolume() > 0)
                .expectNextMatches(data -> data.getTimestamp() != null)
                .verifyComplete();
    }

    @Test
    void emitMarketDataDeliversManuallyEmittedItem() {
        dataProcessingService.initialize();

        MarketData testData = new MarketData(105.25, 105.50, 2500.0, LocalDateTime.now());

        double eps = 1e-6;

        StepVerifier.create(
                        dataProcessingService.getProcessedDataStream()
                                .filter(d ->
                                        Math.abs(d.getBid() - testData.getBid()) < eps &&
                                                Math.abs(d.getAsk() - testData.getAsk()) < eps &&
                                                Math.abs(d.getVolume() - testData.getVolume()) < eps
                                )
                                .take(1)
                )
                .then(() -> dataProcessingService.emitMarketData(testData))
                .assertNext(d -> {
                    assertTrue(Math.abs(d.getBid() - 105.25) < eps);
                    assertTrue(Math.abs(d.getAsk() - 105.50) < eps);
                    assertTrue(Math.abs(d.getVolume() - 2500.0) < eps);
                })
                .verifyComplete();

    }
}