package com.puhavik.controller;

import com.puhavik.model.DDIEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AlertController {
    @FXML
    public ListView<String> lvInteractions;
    @FXML
    public ListView<String> lvRecommendations;
    @FXML
    public Button btnCancelFirst;
    @FXML
    public Button btnCancelSecond;
    @FXML
    private Label drug1Field;
    @FXML
    private Label drug2Field;
    @FXML
    private Label alertLabel;

    private List<DDIEntry> alertInteractions;

    private final Map<Integer, String> severityColorMap = Map.of(
            2, "yellow", 3, "red");

    private final Map<Integer, String> severityTextMap = Map.of(
            2, "Significant Interaction", 3, "Critical Interaction");

    @FXML
    public void initialize() {

    }

    private void reinitializeAlerts() {
        var maxSeverity = alertInteractions.stream().max(Comparator.comparing(DDIEntry::getSeverity)).get().getSeverity();
        alertLabel.setBackground(Background.fill(Paint.valueOf(severityColorMap.get(maxSeverity))));
        alertLabel.setText(severityTextMap.get(maxSeverity));
        drug1Field.setText(alertInteractions.getFirst().getDrug1());
        drug2Field.setText(alertInteractions.getFirst().getDrug2());

        lvInteractions.setItems(
                FXCollections.observableArrayList(alertInteractions.stream().map(DDIEntry::getInteraction)
                        .filter(item -> !item.equals("None")).toList()));

        lvRecommendations.setItems(
                FXCollections.observableArrayList(alertInteractions.stream().map(DDIEntry::getRecommendation)
                        .filter(item -> !item.equals("None")).toList()));


        btnCancelFirst.setText("Cancel " + drug1Field.getText());
        btnCancelSecond.setText("Cancel " + drug2Field.getText());

        btnCancelFirst.setOnAction(e -> {
            System.out.println("Cancelled " + btnCancelFirst.getText());
            ((Stage) btnCancelFirst.getScene().getWindow()).close();
        });

        btnCancelSecond.setOnAction(e -> {
            System.out.println("Cancelled " + btnCancelSecond.getText());
            ((Stage) btnCancelSecond.getScene().getWindow()).close();
        });
    }

    public void setAlertInteractions(List<DDIEntry> alertInteractions) {
        this.alertInteractions = alertInteractions;
        reinitializeAlerts();
    }
}
