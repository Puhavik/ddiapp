package com.puhavik.util;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.puhavik.model.DDIEntry;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVLoader {

    public static List<DDIEntry> loadDDIEntries(String resourcePath) {
        List<DDIEntry> list = new ArrayList<>();

        System.out.println("[DEBUG] Loading CSV from resource path: " + resourcePath);

        try {
            var inputStream = CSVLoader.class.getResourceAsStream(resourcePath);
            if (inputStream == null) {
                System.err.println("[DEBUG] ERROR: Resource not found: " + resourcePath);
                return list;
            }

            System.out.println("[DEBUG] Resource found, creating CSV reader");

            try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(inputStream))
                    .withCSVParser(new CSVParserBuilder().withSeparator('\t').build())
                    .build()) {

                String[] header = reader.readNext(); // skip header
                if (header != null) {
                    System.out.println("[DEBUG] CSV header columns: " + header.length);
                    System.out.println("[DEBUG] Header: " + String.join(", ", header));
                }

                String[] line;
                int count = 0;

                while ((line = reader.readNext()) != null) {
                    count++;
                    if (line.length < 18) continue; // minimal columns

                    String drug1 = safeGet(line, 1);  // object
                    String drug2 = safeGet(line, 3);  // precipitant
                    String interaction = safeGet(line, 11);  // label
                    String recommendation = safeGet(line, 15); // precaution
                    String severity = safeGet(line, 17);       // severity

                    list.add(new DDIEntry(drug1, drug2, interaction, severity, recommendation));

                    if (count <= 3) {
                        System.out.printf("[DEBUG] Entry %d: %s + %s → %s%n", count, drug1, drug2, interaction);
                    }
                }

                System.out.printf("[DEBUG] Finished processing %d rows. Valid DDI entries: %d%n", count, list.size());
            }

        } catch (Exception e) {
            System.err.println("[DEBUG] Error loading CSV: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    private static String safeGet(String[] line, int index) {
        return (index < line.length) ? line[index].trim() : "";
    }
}
