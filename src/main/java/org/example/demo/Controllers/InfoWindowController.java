package org.example.demo.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.example.demo.SceneManager;

import java.io.IOException;

public class InfoWindowController {
    @FXML
    private Button buttonReturn;

    public void initialize() {
        buttonReturn.setOnAction(event ->{
            try{
                Stage stage = (Stage) buttonReturn.getScene().getWindow();
                SceneManager sceneManager = new SceneManager(stage);
                sceneManager.showStartWindow();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
