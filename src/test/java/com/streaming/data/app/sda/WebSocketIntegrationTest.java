package com.streaming.data.app.sda;

import com.streaming.data.app.sda.config.StreamingConfig;
import com.streaming.data.app.sda.model.MarketData;
import com.streaming.data.app.sda.service.CsvExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvExportServiceTest {

    @TempDir
    Path tempDir;

    private CsvExportService csvExportService;

    @BeforeEach
    void setUp() {
        Path csvPath = tempDir.resolve("test-market-data.csv");

        StreamingConfig.CsvConfig csvConfig =
                new StreamingConfig.CsvConfig(csvPath.toString(), 5000, 100);

        StreamingConfig config = new StreamingConfig(null, csvConfig, null, null);
        csvExportService = new CsvExportService(config);
    }

    @Test
    void initializeShouldCreateCsvFileWithHeaders() throws IOException {
        csvExportService.initialize();

        assertTrue(csvExportService.csvFileExists());

        String content = Files.readString(csvExportService.getCsvFilePath());
        assertTrue(content.contains("Timestamp"));
        assertTrue(content.contains("Bid"));
        assertTrue(content.contains("Ask"));
        assertTrue(content.contains("Volume"));
    }

    @Test
    void exportBatchShouldWriteDataToCsv() throws IOException {
        csvExportService.initialize();

        List<MarketData> testData = Arrays.asList(
                new MarketData(100.50, 100.75, 1500, LocalDateTime.now()),
                new MarketData(100.60, 100.85, 2000, LocalDateTime.now())
        );

        csvExportService.exportBatch(testData);

        String content = Files.readString(csvExportService.getCsvFilePath());
        String[] lines = content.split("\n");

        assertEquals(3, lines.length); // Header + 2 data lines
        assertTrue(lines[1].contains("100.5"));
        assertTrue(lines[2].contains("100.6"));
    }

    @Test
    void exportSingleShouldWriteSingleRecord() throws IOException {
        csvExportService.initialize();

        MarketData testData = new MarketData(99.75, 100.00, 800, LocalDateTime.now());
        csvExportService.exportSingle(testData);

        String content = Files.readString(csvExportService.getCsvFilePath());
        assertTrue(content.contains("99.75"));
        assertTrue(content.contains("100.0"));
        assertTrue(content.contains("800"));
    }
}
