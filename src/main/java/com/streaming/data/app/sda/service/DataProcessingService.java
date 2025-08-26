package com.streaming.data.app.sda.service;

import com.streaming.data.app.sda.config.StreamingConfig;
import com.streaming.data.app.sda.model.MarketData;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

/**
 * Service that processes incoming market data using reactive streams
 * with configurable backpressure handling and buffering strategies.
 */
@Service
public class DataProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(DataProcessingService.class);

    private final StreamingConfig streamingConfig;
    private final CsvExportService csvExportService;
    private final DataGenerator marketDataGenerator;

    private Sinks.Many<MarketData> marketDataSink;
    private Flux<MarketData> processedDataStream;

    public DataProcessingService(StreamingConfig streamingConfig,
                                 CsvExportService csvExportService,
                                 DataGenerator marketDataGenerator) {
        this.streamingConfig = streamingConfig;
        this.csvExportService = csvExportService;
        this.marketDataGenerator = marketDataGenerator;
    }

    /**
     * Initializes the sink and processing pipeline after bean creation.
     */
    @PostConstruct
    public void initialize() {
        logger.info("Initializing DataProcessingService");

        this.marketDataSink = Sinks.many()
                .multicast()
                .onBackpressureBuffer(streamingConfig.getProcessing().getBufferSize());

        this.processedDataStream = marketDataSink.asFlux()
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(marketData -> logger.info("Processing market data: {}", marketData.toString()))
                .onBackpressureBuffer(streamingConfig.getProcessing().getBufferSize(), this::handleBufferOverflow);

        startDataProcessing();
        logger.info("DataProcessingService initialized");
    }

    /**
     * Handles dropped data when buffer overflows.
     */
    private void handleBufferOverflow(MarketData droppedData) {
        logger.warn("Buffer overflow - dropping market data: {}", droppedData);
    }

    /**
     * Get the processed data stream for additional subscribers
     */
    public Flux<MarketData> getProcessedDataStream() {
        return processedDataStream;
    }

    /**
     * Manually emit market data (useful for testing or external data sources)
     */
    public void emitMarketData(MarketData marketData) {
        marketDataSink.tryEmitNext(marketData);
    }

    /**
     * Cleans up resources before bean destruction.
     */
    @PreDestroy
    public void cleanup() {
        logger.info("Shutting down DataProcessingService");
        if (marketDataSink != null) {
            marketDataSink.tryEmitComplete();
        }
    }

    /**
     * Starts data generation and CSV export subscriptions.
     */
    private void startDataProcessing() {
        // Subscribe market data generator to the sink
        marketDataGenerator.generateMarketDataStream()
                .subscribe(
                        data -> marketDataSink.tryEmitNext(data),
                        error -> {
                            logger.error("Error in data generation stream", error);
                            marketDataSink.tryEmitError(error);
                        },
                        () -> {
                            logger.info("Data generation stream completed");
                            marketDataSink.tryEmitComplete();
                        }
                );

        // Subscribe CSV export service to processed data
        processedDataStream
                .bufferTimeout(
                        streamingConfig.getCsv().getBatchSize(),
                        java.time.Duration.ofMillis(streamingConfig.getCsv().getExportInterval())
                )
                .filter(batch -> !batch.isEmpty())
                .subscribe(
                        csvExportService::exportBatch,
                        error -> logger.error("Error in processing pipeline", error)
                );
    }
}
