package com.streaming.data.app.sda.config;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

/**
 * Root configuration properties for the streaming data application.
 *
 * Values are bound from application.properties under the prefix
 * app.data-stream.
 */
@Validated
@ConfigurationProperties(prefix = "app.data-stream")
@ConstructorBinding
public class StreamingConfig {

    private final WebSocketConfig websocket;
    private final CsvConfig csv;
    private final ProcessingConfig processing;
    private final SimulationConfig simulation;

    public StreamingConfig(
            WebSocketConfig websocket,
            CsvConfig csv,
            ProcessingConfig processing,
            SimulationConfig simulation
    ) {
        this.websocket = websocket;
        this.csv = csv;
        this.processing = processing;
        this.simulation = simulation;
    }

    public WebSocketConfig getWebsocket() { return websocket; }
    public CsvConfig getCsv() { return csv; }
    public ProcessingConfig getProcessing() { return processing; }
    public SimulationConfig getSimulation() { return simulation; }

    /**
     * Configuration group for WebSocket settings.
     */
    @Validated
    public static class WebSocketConfig {
        private final int port;
        private final String path;

        public WebSocketConfig(
                @Min(1000) int port,
                @NotBlank String path
        ) {
            this.port = port;
            this.path = path;
        }
        public int getPort() { return port; }
        public String getPath() { return path; }
    }

    /**
     * Configuration group for CSV export settings.
     */
    @Validated
    public static class CsvConfig {
        private final String exportPath;
        private final long exportInterval;
        private final int batchSize;

        public CsvConfig(
                @NotBlank String exportPath,
                @Min(1000) long exportInterval,
                @Min(1) int batchSize
        ) {
            this.exportPath = exportPath;
            this.exportInterval = exportInterval;
            this.batchSize = batchSize;
        }
        public String getExportPath() { return exportPath; }
        public long getExportInterval() { return exportInterval; }
        public int getBatchSize() { return batchSize; }
    }

    /**
     * Configuration group for processing (buffer/backpressure).
     */
    @Validated
    public static class ProcessingConfig {
        private final int bufferSize;
        private final String backpressureStrategy;

        public ProcessingConfig(
                @Min(1) int bufferSize,
                @NotBlank String backpressureStrategy
        ) {
            this.bufferSize = bufferSize;
            this.backpressureStrategy = backpressureStrategy;
        }
        public int getBufferSize() { return bufferSize; }
        public String getBackpressureStrategy() { return backpressureStrategy; }
    }

    /**
     * Configuration group for simulated market data generation.
     */
    @Validated
    public static class SimulationConfig {
        private final long dataGenerationInterval;
        private final double initialBid;
        private final double initialAsk;
        private final double maxPriceChange;
        private final int minVolume;
        private final int maxVolume;

        public SimulationConfig(
                @Min(10) long dataGenerationInterval,
                @DecimalMin(value = "0.0", inclusive = true) double initialBid,
                @DecimalMin(value = "0.0", inclusive = true) double initialAsk,
                @DecimalMin(value = "0.0", inclusive = true) double maxPriceChange,
                @Min(1) int minVolume,
                @Min(1) int maxVolume
        ) {
            this.dataGenerationInterval = dataGenerationInterval;
            this.initialBid = initialBid;
            this.initialAsk = initialAsk;
            this.maxPriceChange = maxPriceChange;
            this.minVolume = minVolume;
            this.maxVolume = maxVolume;
            if (maxVolume < minVolume) {
                throw new IllegalArgumentException("maxVolume must be >= minVolume");
            }
        }

        public long getDataGenerationInterval() { return dataGenerationInterval; }
        public double getInitialBid() { return initialBid; }
        public double getInitialAsk() { return initialAsk; }
        public double getMaxPriceChange() { return maxPriceChange; }
        public int getMinVolume() { return minVolume; }
        public int getMaxVolume() { return maxVolume; }
    }
}
