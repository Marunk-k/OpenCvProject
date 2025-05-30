package org.example.demo.Controllers;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainMenuController {

    @FXML
    public TextField deleteLeftField;
    @FXML
    public TextField deleteRightField;
    @FXML
    public Button deleteColumnsButton;
    @FXML
    public Label threadsCountLabel;
    @FXML
    public MenuItem infoMenuItem;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public TextField circleSize;
    @FXML
    public Button changeCircleSizeButton;
    @FXML
    public Button upButton;
    @FXML
    public Button downButton;
    @FXML
    public TextField deleteTopField;
    @FXML
    public TextField deleteBottomField;
    @FXML
    private Button buttonCreateSchema;

    @FXML
    private Button buttonReadySchemes;

    @FXML
    private TilePane imageTilePane;
    @FXML
    private HBox previewHBox;

    @FXML
    private int deleteLeftIndex = 0;
    @FXML
    private int deleteRightIndex = 0;
    @FXML
    private int deleteTopIndex = 0;
    @FXML
    private int deleteBottomIndex = 0;
    private double intCircleSize = 1;

    private double startX;
    private double startY;

    @FXML
    private MenuItem createSchemeMenuItem;
    @FXML
    private MenuItem openSchemeMenuItem;
    @FXML
    private MenuItem exitMenuItem;

    private File currentImageFile;

    private int currentRow = 0;
    private final int itemsInRow = 5;
    private final Map<StackPane, Double> stackPaneOriginalRadius = new HashMap<>();
    private Group group = null;
    private Pane circlePane = null;


    public void initialize() {

        if (deleteColumnsButton != null) {
            deleteColumnsButton.setOnAction(event -> {
                try {
                    int deleteLeft = Integer.parseInt(deleteLeftField.getText());
                    int deleteRight = Integer.parseInt(deleteRightField.getText());
                    removeColumns(deleteLeft, deleteRight);
                    int deleteTop = Integer.parseInt(deleteTopField.getText());
                    int deleteBottom = Integer.parseInt(deleteBottomField.getText());
                    removeRows(deleteTop, deleteBottom);
                } catch (NumberFormatException e) {
                }
            });
        }

        if (changeCircleSizeButton != null) {
            changeCircleSizeButton.setOnAction(event -> {
                try {
                    intCircleSize = Integer.parseInt(circleSize.getText());
                    changeSize();
                } catch (NumberFormatException e) {
                }
            });
        }

        if (createSchemeMenuItem != null) {
            createSchemeMenuItem.setOnAction(event -> {
                try {
                    showCreateSchemaScene();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        if (openSchemeMenuItem != null) {
            openSchemeMenuItem.setOnAction(event -> {
                try {
                    showReadySchemesScene();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        if (exitMenuItem != null) {
            exitMenuItem.setOnAction(event -> {
                System.exit(0);
            });
        }

        if (infoMenuItem != null) {
            infoMenuItem.setOnAction(event -> {
                try {
                    showInfoScene();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        if (buttonReadySchemes != null) {
            buttonReadySchemes.setOnAction(event -> {
                try {
                    showReadySchemesScene();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        if (buttonCreateSchema != null) {
            buttonCreateSchema.setOnAction(event -> {
                try {
                    showCreateSchemaScene();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        setupMouseScrolling();
        upButton.setOnAction(event -> moveSelection(-1));
        downButton.setOnAction(event -> moveSelection(1));
    }

    private void removeRows(int deleteTop, int deleteBottom) {
        this.deleteTopIndex = deleteTop;
        this.deleteBottomIndex = deleteBottom;
        if (currentImageFile != null) {
            displayPixelMatrix(currentImageFile);
        }
    }

    private void moveSelection(int direction) {
        int newRow = currentRow + direction;
        if (circlePane != null) {
            int rowCount = circlePane.getChildren().size() / itemsInRow;
            if (newRow >= 0 && newRow < rowCount) {
                clearRowHighlight(currentRow); // Сбрасываем текущую строку
                currentRow = newRow;
                updateRowHighlight(); // Выделяем новую строку
                updatePreview(); // Обновляем окно с текущими узелками
            }
        }
    }

    private void clearRowHighlight(int row) { // очистить выделение предыдущих узелков
        if (circlePane != null) {
            int startIndex = row * itemsInRow;
            int endIndex = Math.min(startIndex + itemsInRow, circlePane.getChildren().size());
            for (int i = startIndex; i < endIndex; i++) {
                if (i < circlePane.getChildren().size()) {
                    StackPane stackPane = (StackPane) circlePane.getChildren().get(i);
                    if (stackPane != null) {
                        Circle circle = (Circle) stackPane.getChildren().get(0);
                        stackPane.setPrefWidth(stackPaneOriginalRadius.get(stackPane));
                        stackPane.setPrefHeight(stackPaneOriginalRadius.get(stackPane));
                        animateScale(stackPaneOriginalRadius.get(stackPane), circle, stackPane);
                        stackPane.setBorder(null);
                    }
                }

            }
        }
    }

    private void updateRowHighlight() {
        if (circlePane != null) {
            int startIndex = currentRow * itemsInRow;
            int endIndex = Math.min(startIndex + itemsInRow, circlePane.getChildren().size());
            for (int i = startIndex; i < endIndex; i++) {
                if (i < circlePane.getChildren().size()) {
                    StackPane stackPane = (StackPane) circlePane.getChildren().get(i);
                    if (stackPane != null) {
                        Circle circle = (Circle) stackPane.getChildren().get(0);
                        double newSize = stackPaneOriginalRadius.get(stackPane) * 2; // Увеличиваем размер
                        stackPane.setPrefWidth(newSize);
                        stackPane.setPrefHeight(newSize);
                        animateScale(newSize, circle, stackPane);
                        stackPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                    }
                }
            }
        }
    }

    private void updatePreview() {
        if (previewHBox == null || circlePane == null) return;

        previewHBox.getChildren().clear();

        int startIndex = currentRow * itemsInRow;
        int endIndex = Math.min(startIndex + itemsInRow, circlePane.getChildren().size());
        for (int i = startIndex; i < endIndex; i++) {
            if (i < circlePane.getChildren().size()) {
                StackPane originalStackPane = (StackPane) circlePane.getChildren().get(i);
                if (originalStackPane != null) {
                    Circle originalCircle = (Circle) originalStackPane.getChildren().get(0);
                    Circle previewCircle = new Circle(8);
                    previewCircle.setFill(originalCircle.getFill());
                    StackPane previewStackPane = new StackPane(previewCircle);
                    previewStackPane.setPrefWidth(originalStackPane.getWidth());
                    previewStackPane.setPrefHeight(originalStackPane.getHeight());
                    previewHBox.getChildren().add(previewStackPane);
                }
            }

        }
    }

    private void setupMouseScrolling() {
        if (scrollPane != null) {
            scrollPane.setPannable(false); // Отключаем автоматическую прокрутку, управляем вручную

            imageTilePane.setOnMousePressed(event -> {
                startX = event.getSceneX();
                startY = event.getSceneY();
            });

            imageTilePane.setOnMouseDragged(event -> {
                double deltaX = startX - event.getSceneX();
                double deltaY = startY - event.getSceneY();

                startX = event.getSceneX();
                startY = event.getSceneY();

                scrollPane.setHvalue(scrollPane.getHvalue() + deltaX / scrollPane.getWidth());
                scrollPane.setVvalue(scrollPane.getVvalue() + deltaY / scrollPane.getHeight());
            });
        }

    }

    private void removeColumns(int deleteLeft, int deleteRight) {
        this.deleteLeftIndex = deleteLeft;
        this.deleteRightIndex = deleteRight;
        if (currentImageFile != null) {
            displayPixelMatrix(currentImageFile);
        }
    }

    private void changeSize() {
        if (currentImageFile != null) {
            displayPixelMatrix(currentImageFile);
        }
    }

    public void updateThreadsCount(int threadsCount) {
        if (threadsCountLabel != null) {
            threadsCountLabel.setText("Количество нитей: " + threadsCount);
        }
    }

    private void showCreateSchemaScene() throws IOException {
        URL location = getClass().getResource("/org/example/demo/create_schema_scene.fxml");
        FXMLLoader loader = new FXMLLoader(location);
        Parent root = loader.load();
        CreateSchemaSceneController controller = loader.getController();
        controller.setMainMenuController(this);
        Scene scene = new Scene(root, 1500, 600);
        Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.setScene(scene);
        controller.setPrimaryStage(newStage);
        newStage.getIcons().add(new Image("file:src/main/resources/org/example/demo/img.png"));
        newStage.setTitle("Создание схемы");
        newStage.show();
    }


    private void showReadySchemesScene() throws IOException {
        URL location = getClass().getResource("/org/example/demo/ready_schemas.fxml");
        FXMLLoader loader = new FXMLLoader(location);
        Parent root = loader.load();
        ReadySchemesSceneController controller = loader.getController();
        controller.setMainMenuController(this);
        Scene scene = new Scene(root, 1300, 600);
        Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.setScene(scene);
        controller.setPrimaryStage(newStage);
        newStage.getIcons().add(new Image("file:src/main/resources/org/example/demo/img.png"));
        newStage.setTitle("Готовые схемы");
        newStage.show();
    }

    private void showInfoScene() throws IOException {
        URL location = getClass().getResource("/org/example/demo/infoFromMenu.fxml");
        FXMLLoader loader = new FXMLLoader(location);
        Parent root = loader.load();
        Scene scene = new Scene(root, 1300, 600);
        Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.setScene(scene);
        newStage.show();
    }

    public void addImageToTilePane(Image image, double pixelSize, javafx.scene.paint.Color color) {
        ImageView imageView = new ImageView();
        double fitWidth = pixelSize * 10;
        double fitHeight = pixelSize * 10;
        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        imageView.setImage(image);
        imageTilePane.getChildren().add(imageView);
    }

    public void displayPixelMatrix(File imageFile) {
        try {
            currentImageFile = imageFile; //картинка, выбранная в готовых схемах

            // Извлекаем размер пикселя из названия файла
            String fileName = imageFile.getName();
            int pixelSize = extractPixelSizeFromFileName(fileName);

            // Загружаем изображение
            Mat image = Imgcodecs.imread(imageFile.getAbsolutePath());
            if (image.empty()) {
                return;
            }

            int rows = image.rows(); // Высота изображения в пикселях
            int cols = image.cols(); // Ширина изображения в пикселях
            updateThreadsCount(cols / pixelSize - deleteLeftIndex - deleteRightIndex); // количество нитей основы в строке состояния

            // Создаём контейнер для узелков
            circlePane = new Pane();
            circlePane.setPrefSize(cols, rows);
            stackPaneOriginalRadius.clear();

            for (int row = 0; row < rows; row += pixelSize) {
                int rowIndex = row / pixelSize;

                // Проверяем, входит ли строка в диапазон разрешенных
                if (rowIndex >= deleteTopIndex && rowIndex < (rows / pixelSize) - deleteBottomIndex) {
                    for (int col = 0; col < cols; col += pixelSize) {
                        int columnIndex = col / pixelSize;

                        // Проверяем, входит ли столбец в диапазон разрешенных
                        if (columnIndex >= deleteLeftIndex && columnIndex < (cols / pixelSize) - deleteRightIndex) {

                            // Берём цвет текущего пикселя
                            double[] pixelColor = image.get(row, col);
                            Color color;

                            if (pixelColor.length == 1) {
                                int intensity = (int) pixelColor[0];
                                color = Color.rgb(intensity, intensity, intensity);
                            } else {
                                // Цветное изображение (BGR в RGB)
                                color = Color.rgb((int) pixelColor[2], (int) pixelColor[1], (int) pixelColor[0]);
                            }

                            // Создаём узелок (круг)
                            Circle circle = new Circle(pixelSize / 2.0);
                            circle.setFill(color);

                            // Оборачиваем узелок в StackPane
                            StackPane stackPane = new StackPane(circle);
                            stackPaneOriginalRadius.put(stackPane, pixelSize / 2.0);

                            stackPane.setPrefWidth(pixelSize);
                            stackPane.setPrefHeight(pixelSize);

                            // Устанавливаем координаты для StackPane
                            stackPane.setLayoutX(col);
                            stackPane.setLayoutY(row);

                            circlePane.getChildren().add(stackPane);
                        }
                    }
                }
            }

            group = new Group(circlePane);
            Scale scaleTransform = new Scale(intCircleSize, intCircleSize);
            group.getTransforms().add(scaleTransform);

            double scaledWidth = circlePane.getPrefWidth() * intCircleSize; // изменение размера кружков по кнопке
            double scaledHeight = circlePane.getPrefHeight() * intCircleSize;

            group.prefWidth(scaledWidth);
            group.prefHeight(scaledHeight);

            // Замена содержимого mainPane
            imageTilePane.getChildren().clear();
            imageTilePane.getChildren().add(group);
            scrollPane.setContent(group);

            // Перевод выделения
            currentRow = 0;
            scrollPane.setVvalue(0); // Сбрасываем вертикальную прокрутку
            scrollPane.setHvalue(0); // Сбрасываем горизонтальную прокрутку
            updateRowHighlight();
            updatePreview();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateScale(double newScale, Circle circle, StackPane stackPane) { // изменение размера узелков
        double originalRadius = circle.getRadius();
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), circle);
        scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
        scaleTransition.setToX(newScale / originalRadius);
        scaleTransition.setToY(newScale / originalRadius);
        scaleTransition.play();
    }

    private int extractPixelSizeFromFileName(String fileName) {
        try {
            // Пример имени файла: "pixelated_10_imageName.png", где 10 - это размер пикселей
            String[] parts = fileName.split("_");
            return Integer.parseInt(parts[1]); // Второй элемент - размер пикселя
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return 10; // Значение по умолчанию
        }
    }
}