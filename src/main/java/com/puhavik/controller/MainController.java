package com.puhavik.controller;

import com.puhavik.model.DDIEntry;
import com.puhavik.util.CSVLoader;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainController {

    @FXML
    private TextField drug1Field;
    @FXML
    private TextField drug2Field;
    @FXML
    private TableView<DDIEntry> resultsTable;
    @FXML
    private TableColumn<DDIEntry, String> drug1Col;
    @FXML
    private TableColumn<DDIEntry, String> drug2Col;
    @FXML
    private TableColumn<DDIEntry, String> effectCol;
    @FXML
    private TableColumn<DDIEntry, String> interactionCol;
    @FXML
    private TableColumn<DDIEntry, String> severityCol;
    @FXML
    private TableColumn<DDIEntry, String> recommendationCol;

    private List<DDIEntry> allEntries;
    private List<String> allDrugNames;

    @FXML
    public void initialize() throws IOException {
        System.out.println("[DEBUG] Initializing controller");

        // Setup table columns
        System.out.println("[DEBUG] Setting up table columns");

        // Set column widths to make content visible
        drug1Col.setPrefWidth(150);
        drug2Col.setPrefWidth(150);
        interactionCol.setPrefWidth(200);
        effectCol.setPrefWidth(150);
        severityCol.setPrefWidth(100);
        recommendationCol.setPrefWidth(200);

        // Configure cell value factories
        drug1Col.setCellValueFactory(cell -> {
            String value = cell.getValue().getDrug1();
            // Extract just the drug name without the ID
            if (value.contains("|")) {
                value = value.split("\\|")[0].trim();
            }
            return new javafx.beans.property.SimpleStringProperty(value);
        });

        drug2Col.setCellValueFactory(cell -> {
            String value = cell.getValue().getDrug2();
            // Extract just the drug name without the ID
            if (value.contains("|")) {
                value = value.split("\\|")[0].trim();
            }
            return new javafx.beans.property.SimpleStringProperty(value);
        });

        effectCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEffect()));
        interactionCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getInteraction()));
        severityCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getSeverity().toString()));
        recommendationCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getRecommendation()));

        // Configure cell factories with text wrapping
        setupWrappingCellFactory(drug1Col);
        setupWrappingCellFactory(drug2Col);
        setupWrappingCellFactory(effectCol);
        setupWrappingCellFactory(interactionCol);
        setupWrappingCellFactory(severityCol);
        setupWrappingCellFactory(recommendationCol);

        // Make sure table is visible and properly sized
        resultsTable.setVisible(true);
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        System.out.println("[DEBUG] Table columns configured");

        // Load CSV data
        System.out.println("[DEBUG] Loading CSV data from /CombinedDatasetConservativeTWOSIDES.csv");
        allEntries = CSVLoader.loadDDIEntries("/CombinedDatasetConservativeTWOSIDES.csv");
        System.out.println("[DEBUG] CSV data loaded, entries: " + allEntries.size());

        // Prepare drug name list for autocomplete (strip DrugBank IDs)
        allDrugNames = allEntries.stream()
                .flatMap(entry -> java.util.stream.Stream.of(entry.getDrug1(), entry.getDrug2()))
                .map(name -> name.split("\\|")[0].trim().toLowerCase())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        System.out.println("[DEBUG] Loaded entries: " + allEntries.size());
        System.out.println("[DEBUG] Unique drug names: " + allDrugNames.size());


        setupAutoComplete(drug1Field);
        setupAutoComplete(drug2Field);
    }

    @FXML
    public void onCheckInteraction() throws IOException {
        String drug1 = drug1Field.getText().trim().toLowerCase();
        String drug2 = drug2Field.getText().trim().toLowerCase();

        System.out.println("[DEBUG] Button clicked");
        System.out.println("[DEBUG] Searching for: '" + drug1 + "' + '" + drug2 + "'");

        if (drug1.isEmpty() || drug2.isEmpty()) {
            System.out.println("[DEBUG] One or both drug fields are empty");
            return;
        }

        System.out.println("[DEBUG] Total entries to search through: " + allEntries.size());

        System.out.println("[DEBUG] Starting search with improved matching logic");

        // First try exact match
        var filteredEntries = filterEntries(drug1, drug2);

        // Update the table with results
        resultsTable.setItems(FXCollections.observableArrayList(filteredEntries.stream().sorted(Comparator.comparing(entry -> -entry.getSeverity())).toList()));
        System.out.println("[DEBUG] Table items set, count: " + resultsTable.getItems().size());

        // Show a message if no results were found
        if (filteredEntries.isEmpty()) {
            System.out.println("[DEBUG] Showing no results message");
            // Create a placeholder message for the table
            Label noResultsLabel = new Label("No drug interactions found for " + drug1 + " and " + drug2);
            noResultsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
            resultsTable.setPlaceholder(noResultsLabel);
        }

        openAlertWindow(filteredEntries);

    }

    private List<DDIEntry> filterEntries(String drug1, String drug2) {
        var filtered = allEntries.stream()
                .filter(entry -> {
                    String entryDrug1 = entry.getDrug1().split("\\|")[0].trim().toLowerCase();
                    String entryDrug2 = entry.getDrug2().split("\\|")[0].trim().toLowerCase();

                    // Try exact match first
                    boolean exactMatch = (entryDrug1.equals(drug1) && entryDrug2.equals(drug2)) ||
                            (entryDrug1.equals(drug2) && entryDrug2.equals(drug1));

                    if (exactMatch) {
                        System.out.println("[DEBUG] Found exact match - Drug1: '" + entryDrug1 +
                                "', Drug2: '" + entryDrug2 + "'");
                    }

                    return exactMatch;
                })
                .collect(Collectors.toList());

        System.out.println("[DEBUG] Found exact matches: " + filtered.size());

        // If no exact matches, try word-based matching (more accurate than contains)
        if (filtered.isEmpty()) {
            System.out.println("[DEBUG] No exact matches found. Trying word-based matching...");

            filtered = allEntries.stream()
                    .filter(entry -> {
                        String entryDrug1 = entry.getDrug1().split("\\|")[0].trim().toLowerCase();
                        String entryDrug2 = entry.getDrug2().split("\\|")[0].trim().toLowerCase();

                        // Check if all words in user input are in the drug name
                        boolean drug1MatchesEntry1 = containsAllWords(entryDrug1, drug1);
                        boolean drug1MatchesEntry2 = containsAllWords(entryDrug2, drug1);
                        boolean drug2MatchesEntry1 = containsAllWords(entryDrug1, drug2);
                        boolean drug2MatchesEntry2 = containsAllWords(entryDrug2, drug2);

                        boolean match = (drug1MatchesEntry1 && drug2MatchesEntry2) ||
                                (drug1MatchesEntry2 && drug2MatchesEntry1);

                        if (match) {
                            System.out.println("[DEBUG] Found word match - Drug1: '" + entryDrug1 +
                                    "', Drug2: '" + entryDrug2 + "'");
                        }

                        return match;
                    })
                    .limit(20)  // Limit to avoid too many results
                    .collect(Collectors.toList());

            System.out.println("[DEBUG] Found with word-based matching: " + filtered.size());
        }

        // If still no matches, try substring matching as a last resort
        if (filtered.isEmpty()) {
            System.out.println("[DEBUG] No word matches found. Trying substring matching...");

            filtered = allEntries.stream()
                    .filter(entry -> {
                        String entryDrug1 = entry.getDrug1().split("\\|")[0].trim().toLowerCase();
                        String entryDrug2 = entry.getDrug2().split("\\|")[0].trim().toLowerCase();

                        boolean match = (entryDrug1.contains(drug1) && entryDrug2.contains(drug2)) ||
                                (entryDrug1.contains(drug2) && entryDrug2.contains(drug1));

                        if (match) {
                            System.out.println("[DEBUG] Found substring match - Drug1: '" + entryDrug1 +
                                    "', Drug2: '" + entryDrug2 + "'");
                        }

                        return match;
                    })
                    .limit(10)  // Limit to avoid too many results
                    .collect(Collectors.toList());

            System.out.println("[DEBUG] Found with flexible search: " + filtered.size());
        }
        return filtered;
    }

    private void openAlertWindow(List<DDIEntry> entriesWithInteractions) throws IOException {

        var alertInteractions = entriesWithInteractions.stream().filter(item -> item.getSeverity() > 1).toList();

        if (!alertInteractions.isEmpty()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/alert_view.fxml"));
            Stage stage = new Stage();
            System.out.println("SET Scene");


            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Interaction Alert");

            AlertController alertController = loader.getController();
            alertController.setAlertInteractions(alertInteractions);

            stage.show();
        }
    }

    private void setupAutoComplete(TextField field) {
        ContextMenu suggestions = new ContextMenu();
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 2) {
                suggestions.hide();
                return;
            }

            var matched = allDrugNames.stream()
                    .filter(name -> name.contains(newVal.toLowerCase()))
                    .limit(5)
                    .toList();

            if (matched.isEmpty()) {
                suggestions.hide();
                return;
            }

            List<MenuItem> entries = matched.stream().map(name -> {
                MenuItem item = new MenuItem(name);
                item.setOnAction(e -> {
                    field.setText(name);
                    suggestions.hide();
                });
                return item;
            }).toList();

            suggestions.getItems().setAll(entries);
            if (!suggestions.isShowing()) {
                suggestions.show(field, Side.BOTTOM, 0, 0);
            }
        });
    }

    /**
     * Checks if all words in the search term are contained in the target string.
     * This provides more accurate matching than simple substring matching.
     *
     * @param target     The string to search in
     * @param searchTerm The term to search for
     * @return true if all words in searchTerm are found in target
     */
    private boolean containsAllWords(String target, String searchTerm) {
        if (searchTerm.isEmpty()) return false;
        if (target.isEmpty()) return false;

        // Split search term into words
        String[] searchWords = searchTerm.split("\\s+");

        // Check if all words are contained in the target
        for (String word : searchWords) {
            if (word.length() < 2) continue; // Skip very short words
            if (!target.contains(word)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sets up a cell factory for a TableColumn that enables text wrapping.
     *
     * @param column The TableColumn to set up with text wrapping
     */
    private <T> void setupWrappingCellFactory(TableColumn<DDIEntry, T> column) {
        column.setCellFactory(tc -> {
            TableCell<DDIEntry, T> cell = new TableCell<>() {
                private final Label label = new Label();
                private final VBox box = new VBox(label);

                {
                    // Configure the label for text wrapping
                    label.setWrapText(true);
                    label.setMaxWidth(Double.MAX_VALUE);

                    // Configure the VBox container
                    box.setAlignment(Pos.TOP_LEFT);
                    box.setPrefWidth(Region.USE_COMPUTED_SIZE);
                    box.setPrefHeight(Region.USE_COMPUTED_SIZE);

                    // Remove fixed minimum height to allow row to shrink to content
                    box.setFillWidth(true);
                }

                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        label.setText(item.toString());
                        setGraphic(box);

                        // Request layout to ensure proper sizing
                        box.requestLayout();
                    }
                }
            };

            return cell;
        });
    }
}
