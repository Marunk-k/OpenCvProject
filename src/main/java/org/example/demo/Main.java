package org.example.demo;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.opencv.core.Core;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        primaryStage.setMaximized(true);
        primaryStage.getIcons().add(new Image("file:src/main/resources/org/example/demo/img.png"));
        SceneManager sceneManager = new SceneManager(primaryStage);
        sceneManager.showStartWindow();
    }

    public static void main(String[] args) {
        launch(args); // точка входа в приложение launch - метод вызывается из Application (это абстрактный класс - родитель для всех javaFx приложений)
    }
}