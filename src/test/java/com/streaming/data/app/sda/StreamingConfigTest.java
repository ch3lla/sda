package com.streaming.data.app.sda;

import com.streaming.data.app.sda.config.StreamingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "app.data-stream.websocket.port=8080",
        "app.data-stream.websocket.path=/market-data",
        "app.data-stream.csv.export-path=./data/test.csv",
        "app.data-stream.csv.export-interval=5000",
        "app.data-stream.csv.batch-size=100",
        "app.data-stream.processing.buffer-size=1000",
        "app.data-stream.processing.backpressure-strategy=BUFFER_OVERFLOW_DROP_LATEST",
        "app.data-stream.simulation.data-generation-interval=100",
        "app.data-stream.simulation.initial-bid=100.0",
        "app.data-stream.simulation.initial-ask=100.5",
        "app.data-stream.simulation.max-price-change=0.5",
        "app.data-stream.simulation.min-volume=100",
        "app.data-stream.simulation.max-volume=10000"
})
class StreamingConfigTest {

    @Test
    void configurationPropertiesShouldBindCorrectly() {
        StreamingConfig.WebSocketConfig wsConfig =
                new StreamingConfig.WebSocketConfig(8080, "/market-data");

        assertEquals(8080, wsConfig.getPort());
        assertEquals("/market-data", wsConfig.getPath());
    }

    @Test
    void csvConfigShouldValidateCorrectly() {
        StreamingConfig.CsvConfig csvConfig =
                new StreamingConfig.CsvConfig("./test.csv", 5000, 100);

        assertEquals("./test.csv", csvConfig.getExportPath());
        assertEquals(5000, csvConfig.getExportInterval());
        assertEquals(100, csvConfig.getBatchSize());
    }

    @Test
    void processingConfigShouldHaveValidDefaults() {
        StreamingConfig.ProcessingConfig processingConfig =
                new StreamingConfig.ProcessingConfig(1000, "BUFFER_OVERFLOW_DROP_LATEST");

        assertEquals(1000, processingConfig.getBufferSize());
        assertEquals("BUFFER_OVERFLOW_DROP_LATEST", processingConfig.getBackpressureStrategy());
    }
}