package org.example.demo.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class ReadySchemesSceneController {
    @FXML
    public Button buttonReturn;

    @FXML
    private VBox imageListView; // список с готовыми схемами

    private MainMenuController mainMenuController; // чтобы вызвать опять главное меню

    private Stage primaryStage;

    public void setMainMenuController(MainMenuController mainMenuController) {
        this.mainMenuController = mainMenuController;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void initialize() {
        loadImages();

        buttonReturn.setOnAction(event -> {
            Stage stage = (Stage) buttonReturn.getScene().getWindow();
            stage.close();
        });
    }

    private void loadImages() {
        File dir = new File("ready_schemas");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((dir1, name) -> { // берём картинки из директории внутри проекта ready_schemas
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg");
            });

            if (files != null) { // если готовые схемы уже есть
                Arrays.stream(files).forEach(file -> {
                    try {
                        Image image = new Image(new FileInputStream(file));
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(100);
                        imageView.setFitHeight(100);

                        imageView.setOnMouseClicked(event -> { // чтобы можно было выбрать фотографию
                            if (event.getButton() == MouseButton.PRIMARY) {
                                loadImageToMainMenu(file);
                            }
                        });

                        Label label = new Label(file.getName());
                        VBox container = new VBox(imageView, label);
                        container.setAlignment(javafx.geometry.Pos.CENTER);
                        imageListView.getChildren().add(container);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private void loadImageToMainMenu(File file) {
        mainMenuController.displayPixelMatrix(file); // Передаём выбранную картинку
        primaryStage.close();
    }

}
