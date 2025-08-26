package com.streaming.data.app.sda.service;

import com.streaming.data.app.sda.config.StreamingConfig;
import com.streaming.data.app.sda.model.MarketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service responsible for generating realistic market data simulation
 * with configurable parameters for bid/ask prices and volume.
 */
@Service
public class DataGenerator {

    private  static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);
    private final StreamingConfig streamingConfig;
    private final Random random;

    private volatile double currentBid;
    private volatile double currentAsk;

    public DataGenerator(StreamingConfig streamingConfig) {
        this.streamingConfig = streamingConfig;
        this.random = new Random();
        this.currentBid = streamingConfig.getSimulation().getInitialBid();
        this.currentAsk = streamingConfig.getSimulation().getInitialAsk();

        logger.info("DataGenerator initialized with bid={}, ask={}",
                currentBid, currentAsk);
    }

    /**
     * Creates an infinite stream of market data with realistic price movements
     * and random volume generation.
     *
     * @return Flux of MarketData objects
     */
    public Flux<MarketData> generateMarketDataStream() {
        return Flux.interval(Duration.ofMillis(streamingConfig.getSimulation().getDataGenerationInterval()))
                .map(tick -> generateNextMarketData())
                .doOnNext(data -> logger.debug("Generated: {}", data))
                .doOnError(error -> logger.error("Error generating market data", error));
    }

    /**
     * Generates next stream of market data
     * @return MarketData object
     */
    private MarketData generateNextMarketData() {
        updatePrices();
        long volume = generateVolume();

        return new MarketData(
                currentBid,
                currentAsk,
                volume,
                LocalDateTime.now()
        );
    }

    /**
     * Updates the bid and ask prices through range values
     * while maintaining bid < ask relationship
     */
    private void updatePrices() {
        double maxChange = streamingConfig.getSimulation().getMaxPriceChange();

        double bidChange = (random.nextDouble() - 0.5) * 2 * maxChange;
        double askChange = (random.nextDouble() - 0.5) * 2 * maxChange;

        double newBid = Math.max(0.01, currentBid + bidChange);
        double newAsk = Math.max(newBid + 0.01, currentAsk + askChange);

        currentBid = Math.round(newBid * 100.0) / 100.0;
        currentAsk = Math.round(newAsk * 100.0) / 100.0;
    }

    /**
     * Generates volume using range values
     * @return Volume in long format
     */
    private long generateVolume() {
        int min = streamingConfig.getSimulation().getMinVolume();
        int max = streamingConfig.getSimulation().getMaxVolume();
        return min + random.nextInt(max - min + 1);
    }
}
