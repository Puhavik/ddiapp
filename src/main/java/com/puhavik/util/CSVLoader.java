package com.puhavik.util;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.puhavik.model.DDIEntry;
import org.apache.commons.collections.comparators.ComparatorChain;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVLoader {

    private static final Map<String, Integer> severityOrderingMap = Map.of(
            "None", 0, "", 0, "1", 1,
            "2", 2, "3", 3, "Significant", 2, "Critical", 3
    );

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
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String header = reader.readLine(); // skip header
            System.out.println("[DEBUG] CSV header columns: " + header.split("\t").length);
            System.out.println("[DEBUG] Header: " + header);
            String previousLine = reader.readLine();

            var count = 0;

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                count++;
                String[] colsBefore = previousLine.split("\t");
                String[] colsNow = line.split("\t");
                Object[] cols = null;
                // some lines have a linebreak in the interaction column and start with \t at next line.
                if (line.stripLeading().length() != line.length() && colsBefore.length < 10) {
                    cols = Stream.concat(Arrays.stream(colsBefore), Arrays.stream(colsNow)).toArray();
                } else if (colsNow.length > 17) {
                    cols = colsNow;
                }
                previousLine = line;

                if (cols != null) {
                    String drug1 = (String) cols[1];// object
                    String drug2 = (String) cols[3];  // precipitant
                    String interaction = (String) cols[11];  // label
                    String effect = (String) cols[9]; // pkEffect
                    String recommendation = (String) cols[15]; // precaution
                    String severity = (String) cols[17];       // severity

                    list.add(new DDIEntry(drug1, drug2, interaction, effect,
                            severityOrderingMap.getOrDefault(severity, 0), recommendation));
                }
            }

            reader.close();

            list.stream().filter(item -> item.getSeverity() > 1)
                    .filter(item -> !Objects.equals(item.getInteraction(), "None") &&
                            !Objects.equals(item.getRecommendation(), "None"))
                    .sorted(Comparator.comparing(DDIEntry::getDrug1).thenComparing(DDIEntry::getDrug2))
                    .forEach(item -> System.out.println(item.getDrug1() + " -- " + item.getDrug2()));


            System.out.printf("[DEBUG] Finished processing %d rows. Valid DDI entries: %d%n", count, list.size());

        } catch (Exception e) {
            System.err.println("[DEBUG] Error loading CSV: " + e.getMessage());
            e.printStackTrace();
        }

        return list.stream().distinct().collect(Collectors.toList());
    }

    private static String safeGet(String[] line, int index) {
        return (index < line.length) ? line[index].trim() : "";
    }
}
