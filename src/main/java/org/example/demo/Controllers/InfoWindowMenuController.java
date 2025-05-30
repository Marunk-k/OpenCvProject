package org.example.demo.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class InfoWindowMenuController {
    @FXML
    private Button buttonReturn;

    public void initialize() {
        buttonReturn.setOnAction(event ->{
            Stage stage = (Stage) buttonReturn.getScene().getWindow();
            stage.close();
        });
    }
}
