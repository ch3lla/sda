package com.streaming.data.app.sda.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Market data model representing bid, ask, and volume information
 * with timestamp for a financial instrument.
 */
public class MarketData {

    @JsonProperty("timestamp")
    private final LocalDateTime timestamp;
    private final double bid;
    private final double ask;
    private final double volume;

    public MarketData(double bid, double ask, double volume, LocalDateTime timestamp) {
        this.bid = bid;
        this.ask = ask;
        this.volume = volume;
        this.timestamp = timestamp;
    }

    public double getVolume() {
        return volume;
    }

    public double getAsk() {
        return ask;
    }

    public double getBid() {
        return bid;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String[] toCsvArray() {
        return new String[]{
                timestamp.toString(),
                String.valueOf(bid),
                String.valueOf(ask),
                String.valueOf(volume)
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarketData)) return false;
        MarketData that = (MarketData) o;
        return Double.compare(bid, that.bid) == 0
                && Double.compare(ask, that.ask) == 0
                && Double.compare(volume, that.volume) == 0
                && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bid, ask, volume, timestamp);
    }

    @Override
    public String toString() {
        return "MarketData{ts=" + timestamp + ", bid=" + bid + ", ask=" + ask + ", volume=" + volume + "}";
    }

}
