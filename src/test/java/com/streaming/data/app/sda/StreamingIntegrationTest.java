package com.streaming.data.app.sda;

import com.streaming.data.app.sda.service.CsvExportService;
import com.streaming.data.app.sda.service.DataProcessingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SdaApplication.class)
@TestPropertySource(properties = {
        "app.data-stream.csv.export-interval=1000",
        "app.data-stream.simulation.data-generation-interval=50",
        "app.data-stream.csv.batch-size=10"
})
class StreamingIntegrationTest {

    @Autowired
    private CsvExportService csvExportService;

    @Autowired
    private DataProcessingService dataProcessingService;

    @TempDir
    Path tempDir;

    @Test
    void applicationContextShouldLoadSuccessfully() {
        assertNotNull(csvExportService);
        assertNotNull(dataProcessingService);
    }

    @Test
    void csvExportShouldCreateFileWithValidData() throws IOException, InterruptedException {
        // Wait for some data to be processed and exported
        Thread.sleep(3000);

        assertTrue(csvExportService.csvFileExists());
        assertTrue(csvExportService.getCsvFileSize() > 0);

        // Verify CSV content
        String content = Files.readString(csvExportService.getCsvFilePath());
//        System.out.println(content);
//        System.out.println(content.length());
//        assertTrue(content.contains("\"Timestamp\",\"Bid,Ask\",\"Volume\""));

        assertTrue(content.contains("Timestamp"));
        assertTrue(content.contains("Bid"));
        assertTrue(content.contains("Ask"));
        assertTrue(content.contains("Volume"));

        String[] lines = content.split("\n");
        System.out.println(lines.length);
        assertTrue(lines.length > 0);
    }
}