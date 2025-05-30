package org.example.demo.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.example.demo.SceneManager;

import java.io.IOException;

public class StartWindowController {

    @FXML
    private Button buttonStart;
    @FXML
    private Button buttonExit;
    @FXML
    private Button buttonInfo;

    public void initialize() { // Это метод определен в интерфейсе javafx.fxml.Initializable, при загрузке FXML файла, JavaFX framework ищет в классе контроллера метод с названием initialize() и вызывает его
        buttonStart.setOnAction(event -> {
            try {
                Stage stage = (Stage) buttonStart.getScene().getWindow();
                SceneManager sceneManager = new SceneManager(stage);
                sceneManager.showMainMenu();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        buttonExit.setOnAction(event -> {
           System.exit(0);
        });

        buttonInfo.setOnAction(event ->{
            try{
                Stage stage = (Stage) buttonInfo.getScene().getWindow();
                SceneManager sceneManager = new SceneManager(stage);
                sceneManager.showInfoWindow();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}