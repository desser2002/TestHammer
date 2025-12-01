package org.dzianisbova.domain.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JsonExporter {
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    private JsonExporter() {
    }

    public static void exportJson(String folderPath, List<StatsSnapshot> finalReport) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String filename = "report_" + timestamp + ".json";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            if (!folder.mkdirs() && !folder.exists()) {
                throw new IOException("Failed to create directory: " + folder.getAbsolutePath());
            }
        }

        File file = new File(folder, filename);
        objectMapper.writeValue(file, finalReport);
    }
}
