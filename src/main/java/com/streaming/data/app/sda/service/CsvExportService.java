package com.streaming.data.app.sda.service;

import com.opencsv.CSVWriter;
import com.streaming.data.app.sda.config.StreamingConfig;
import com.streaming.data.app.sda.model.MarketData;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service responsible for exporting market data to CSV files
 * with configurable paths and thread-safe operations.
 */
@Service
public class CsvExportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvExportService.class);

    private static final String[] CSV_HEADERS = {"Timestamp", "Bid", "Ask", "Volume"};

    private final StreamingConfig config;
    private final ReentrantLock writeLock = new ReentrantLock();

    private Path csvFilePath;
    private boolean headerWritten = false;

    public CsvExportService(StreamingConfig config) {
        this.config = config;
    }

    /**
     * Initializes the CSV export file after bean creation.
     * @throws IOException if directory creation or file operations fail
     */
    @PostConstruct
    public void initialize() throws IOException {
        this.csvFilePath = Paths.get(config.getCsv().getExportPath());

        // Create directories if they don't exist
        Files.createDirectories(csvFilePath.getParent());

        // Initialize CSV file with headers if it doesn't exist
        if (!Files.exists(csvFilePath)) {
            writeHeaders();
        } else {
            // File exists, assume headers are already written
            headerWritten = true;
        }

        logger.info("CSV export initialized. File path: {}", csvFilePath.toAbsolutePath());
    }

    /**
     * Export a batch of market data to CSV file
     *
     * @param marketDataBatch List of market data to export
     */
    public void exportBatch(List<MarketData> marketDataBatch) {
        if (marketDataBatch.isEmpty()) {
            return;
        }

        writeLock.lock();
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(csvFilePath.toFile(), true))) {

            // Write headers if not written yet
            if (!headerWritten) {
                csvWriter.writeNext(CSV_HEADERS);
                headerWritten = true;
            }

            // Write market data
            for (MarketData data : marketDataBatch) {
                csvWriter.writeNext(data.toCsvArray());
            }

            csvWriter.flush();

            logger.info("Exported {} market data records to CSV", marketDataBatch.size());

        } catch (IOException e) {
            logger.error("Error writing to CSV file: {}", csvFilePath, e);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Export single market data record
     *
     * @param marketData Single market data record
     */
    public void exportSingle(MarketData marketData) {
        exportBatch(List.of(marketData));
    }

    /**
     * Write Headers to CSV file
     * @throws IOException if file not found
     */
    private void writeHeaders() throws IOException {
        writeLock.lock();
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(csvFilePath.toFile()))) {
            csvWriter.writeNext(CSV_HEADERS);
            headerWritten = true;
            logger.info("CSV headers written to file");
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Get the current CSV file path
     */
    public Path getCsvFilePath() {
        return csvFilePath;
    }

    /**
     * Check if CSV file exists and has data
     */
    public boolean csvFileExists() {
        return Files.exists(csvFilePath);
    }

    /**
     * Get the size of the CSV file in bytes
     */
    public long getCsvFileSize() throws IOException {
        return csvFileExists() ? Files.size(csvFilePath) : 0;
    }
}
