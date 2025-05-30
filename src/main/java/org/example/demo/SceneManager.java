package org.example.demo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneManager {

    private final Stage primaryStage;

    public SceneManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void showStartWindow() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/demo/start.fxml"));
        primaryStage.setMaximized(true);
        Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
        primaryStage.setTitle("Начальная страница");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showMainMenu() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/demo/main.fxml"));
        primaryStage.setMaximized(true);
        Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight() - 30);
        primaryStage.setTitle("Главное меню");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showInfoWindow() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/demo/info.fxml"));
        Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
        primaryStage.setTitle("Справочная информация");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
