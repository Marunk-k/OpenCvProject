package org.example.demo.Controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CreateSchemaSceneController {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @FXML
    public Button buttonReturn;
    @FXML
    public Button buttonCreate;
    @FXML
    private Button buttonSelectImage;
    @FXML
    private Slider pixelSizeSlider;
    @FXML
    private Button buttonApply;
    @FXML
    private Canvas previewCanvas;  // область предпросмотра
    @FXML
    private Label pixelSizeLabel;
    @FXML
    private Label colorLabel;
    @FXML
    private HBox colorPalette;

    private MainMenuController mainMenuController; // чтобы вызвать главное меню после закрытия или сохранения
    private Stage primaryStage;
    private Image selectedImage;
    private List<Color> selectedColors = new ArrayList<>();
    private List<Button> colorButtons = new ArrayList<>();

    public void setMainMenuController(MainMenuController mainMenuController) {
        this.mainMenuController = mainMenuController;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void initialize() {
        buttonCreate.setOnAction(event -> {
            if (selectedImage != null) {
                try {
                    double pixelSize = pixelSizeSlider.getValue(); // размер пикселей для пикселизации изображения
                    List<Scalar> defaultColors = new ArrayList<>(); // список выбранных цветов в вид 4-элементных (brg + a) векторов для передачи значений пикселей
                    for (Color color : selectedColors) {
                        defaultColors.add(new Scalar(color.getBlue() * 255, color.getGreen() * 255, color.getRed() * 255));
                    }

                    File file = new File(new URI(selectedImage.getUrl()).getPath());
                    Mat image = Imgcodecs.imread(file.getAbsolutePath());
                    if (image.empty()) {
                        return;
                    }
                    Mat pixelatedImage = pixelateImage(image, (int) pixelSize); // сохраняет пиксельное изображение в Mat (базовый контейнер изображения: название (размер, тип и тд) + ссылка на матрицу)
                    Mat quantizedImage = quantizeColors(pixelatedImage, defaultColors.isEmpty() ? getDefaultColors() : defaultColors); // квантование изображения

                    Image processedImage = matToImage(quantizedImage); // Преобразование обработанного Mat в Image для отображения
                    displayImageOnCanvas(processedImage); // отображение
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        buttonSelectImage.setOnAction(event -> {
            try {
                openFileChooser();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        buttonReturn.setOnAction(event -> {
            Stage stage = (Stage) buttonReturn.getScene().getWindow();
            stage.close();
        });

        buttonApply.setOnAction(event -> applyChanges());

        pixelSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            updatePixelSizeLabel(newValue.intValue());
            updatePreview();
        });
        createColorPalette();
    }

    private void displayImageOnCanvas(Image image) {
        GraphicsContext gc = previewCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        gc.drawImage(image, 0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
    }

    private void createColorPalette() {
        Color[] colors = {
                Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE,
                Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.PURPLE,
                Color.LIGHTCORAL, Color.DARKRED, Color.LIME, Color.DARKGREEN,
                Color.AQUA, Color.DARKBLUE, Color.KHAKI, Color.DARKGOLDENROD,
                Color.DARKMAGENTA, Color.BURLYWOOD, Color.DEEPPINK, Color.MEDIUMVIOLETRED,
                Color.MEDIUMSEAGREEN, Color.ROYALBLUE, Color.FIREBRICK, Color.SANDYBROWN,
                Color.TEAL, Color.MAROON, Color.OLIVEDRAB, Color.SPRINGGREEN, Color.BROWN, Color.GREY,
                Color.INDIANRED, Color.DARKSALMON, Color.LIGHTPINK, Color.MEDIUMORCHID,
                Color.DARKORANGE, Color.GOLD, Color.DARKSEAGREEN, Color.DARKSLATEGRAY,
                Color.LIGHTGRAY, Color.LIGHTSALMON, Color.FORESTGREEN, Color.MEDIUMBLUE,
                Color.NAVY, Color.CHARTREUSE, Color.MEDIUMTURQUOISE, Color.SLATEGREY,
                Color.CADETBLUE, Color.SILVER, Color.MEDIUMAQUAMARINE, Color.LIGHTSKYBLUE,
                Color.DARKCYAN, Color.PERU, Color.DARKGRAY
        };

        for (int i = 0; i < colors.length; i++) {
            Color color = colors[i];
            Button colorButton = new Button();
            colorButton.setStyle(String.format("-fx-background-color: #%02X%02X%02X;",
                    (int) (color.getRed() * 255),
                    (int) (color.getGreen() * 255),
                    (int) (color.getBlue() * 255)));
            colorButton.setOnAction(event -> {
                toggleColor(color);
            });
            colorPalette.getChildren().add(colorButton);
            colorButtons.add(colorButton);
        }
    }

    private void toggleColor(Color color) { // обработка взаимодействия с палеткой (удаление и добавление + лимит в 10 цветов)
        if (selectedColors.contains(color)) {
            selectedColors.remove(color);
        } else {
            if (selectedColors.size() < 10) {
                selectedColors.add(color);
            }
        }
        updateColorLabel();
        updatePreview();
    }

    private void updateColorLabel() {
        StringBuilder sb = new StringBuilder();
        for (Color color : selectedColors) {
            String colorString = String.format("#%02X%02X%02X",
                    (int) (color.getRed() * 255),
                    (int) (color.getGreen() * 255),
                    (int) (color.getBlue() * 255));

            sb.append(colorString).append(" ");
        }
        colorLabel.setText(sb.toString());
    }

    private void updatePixelSizeLabel(Number value) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        pixelSizeLabel.setText(decimalFormat.format(value));
    }

    private void openFileChooser() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            selectedImage = new Image(selectedFile.toURI().toString());
            updatePreview();
        }
    }

    private void applyChanges() {
        if (selectedImage != null) {
            try {
                double pixelSize = pixelSizeSlider.getValue();
                List<Scalar> defaultColors = new ArrayList<>();
                for (Color color : selectedColors) {
                    defaultColors.add(new Scalar(color.getBlue() * 255, color.getGreen() * 255, color.getRed() * 255));
                }

                File file = new File(new URI(selectedImage.getUrl()).getPath());
                Mat image = Imgcodecs.imread(file.getAbsolutePath());
                if (image.empty()) {
                    return;
                }
                Mat pixelatedImage = pixelateImage(image, (int) pixelSize);
                Mat quantizedImage = quantizeColors(pixelatedImage, defaultColors.isEmpty() ? getDefaultColors() : defaultColors);

                File outputDir = new File("ready_schemas");
                if (!outputDir.exists()) { // Создание папки ready_schemas, если ее нет
                    outputDir.mkdirs();
                }

                String outputFileName = "pixelated_" + (int) pixelSize + "_" + file.getName(); // пишем в название созданной схемы размер пикселей, чтобы рассчитать количество узелков в готовой схеме на главной странице
                File outputFile = new File(outputDir, outputFileName);

                BufferedImage bufferedImage = matToBufferedImage(quantizedImage);
                ImageIO.write(bufferedImage, "png", outputFile);

                Mat resizedImage = resizeImage(quantizedImage, 150, 150); // Меняем размер изображения

                Image imageToPass = matToImage(resizedImage);

                mainMenuController.addImageToTilePane(imageToPass, pixelSize, selectedColors.isEmpty() ? javafx.scene.paint.Color.BLACK : selectedColors.get(0));
                primaryStage.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Mat resizeImage(Mat image, int targetWidth, int targetHeight) {
        Mat resizedImage = new Mat();
        Size size = new Size(targetWidth, targetHeight);
        Imgproc.resize(image, resizedImage, size, 0, 0, Imgproc.INTER_AREA); // Imgproc модуль для обработки изображений, который включает линейную и нелинейную фильтрацию, геометрические преобразования и тд
        return resizedImage;
    }

    private BufferedImage matToBufferedImage(Mat matrix) throws Exception { // переход из OpenCV в Java
        // сохраняет матрицу в byte массив в формате .bmp
        // считывает этот массив в BufferedImage
        // циклически перебирает все пиксели, преобразуя их в формат RGB и записывая в новый BufferedImage
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".bmp", matrix, mob);
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = javax.imageio.ImageIO.read(in);
        } catch (Exception e) {
            throw e;
        }
        BufferedImage outputImage = new BufferedImage(bufImage.getWidth(), bufImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < bufImage.getHeight(); y++) {
            for (int x = 0; x < bufImage.getWidth(); x++) {
                int rgb = bufImage.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int newRgb = (r << 16) | (g << 8) | b;
                outputImage.setRGB(x, y, newRgb);

            }
        }
        return outputImage;
    }

    private Image matToImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double[] pixel = mat.get(y, x);
                if (pixel != null && pixel.length == 3) {
                    int r = (int) pixel[2];
                    int g = (int) pixel[1];
                    int b = (int) pixel[0];
                    pixelWriter.setColor(x, y, Color.rgb(r, g, b));
                }
            }
        }
        return writableImage;
    }

    private void updatePreview() {
        if (selectedImage != null) {
            double pixelSize = pixelSizeSlider.getValue();
            double canvasWidth = previewCanvas.getWidth();
            double canvasHeight = previewCanvas.getHeight();

            GraphicsContext gc = previewCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvasWidth, canvasHeight); // очистка области для превью
            gc.drawImage(selectedImage, 0, 0, canvasWidth, canvasHeight);
            if (!selectedColors.isEmpty()) { // разноцветная сетка
                for (int i = 0; i < selectedColors.size(); i++) {
                    gc.setStroke(selectedColors.get(i));
                    gc.setLineWidth(1);
                    for (int x = 0; x < canvasWidth; x += pixelSize) {
                        if (i % 2 == 0) gc.strokeLine(x, 0, x, canvasHeight);
                    }
                    for (int y = 0; y < canvasHeight; y += pixelSize) {
                        if (i % 2 != 0) gc.strokeLine(0, y, canvasWidth, y);
                    }
                }
            } else { // иначе чёрная сетка
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
                for (int x = 0; x < canvasWidth; x += pixelSize) {
                    gc.strokeLine(x, 0, x, canvasHeight);
                }
                for (int y = 0; y < canvasHeight; y += pixelSize) {
                    gc.strokeLine(0, y, canvasWidth, y);
                }
            }
        }
    }

    public Mat pixelateImage(Mat image, int pixelSize) { // делает изображение пиксельным: берёт пиксель с размером, указанным при создании пользователем, и заполняет его средним цветом из всей это области пикселя
        int rows = image.rows();
        int cols = image.cols();
        Mat result = new Mat(rows, cols, image.type());
        for (int y = 0; y < rows; y += pixelSize) {
            for (int x = 0; x < cols; x += pixelSize) {
                int regionEndY = Math.min(rows, y + pixelSize);
                int regionEndX = Math.min(cols, x + pixelSize);

                double[] avgColor = calculateAverageColor(image, x, y, regionEndX, regionEndY);
                Scalar color = new Scalar(avgColor[0], avgColor[1], avgColor[2]);
                Imgproc.rectangle(result, new Point(x, y), new Point(regionEndX, regionEndY), color, Core.FILLED);

            }
        }
        return result;
    }

    private double[] calculateAverageColor(Mat image, int x, int y, int endX, int endY) { // подсчёт среднего цвета пикселя
        double sumB = 0;
        double sumG = 0;
        double sumR = 0;
        int count = 0;

        for (int row = y; row < endY; row++) {
            for (int col = x; col < endX; col++) {
                double[] pixel = image.get(row, col);
                sumB += pixel[0];
                sumG += pixel[1];
                sumR += pixel[2];
                count++;
            }
        }
        return new double[]{sumB / count, sumG / count, sumR / count};
    }

    public Mat quantizeColors(Mat image, List<Scalar> defaultColors) { // замена пикселей на выбранные пользователем цвета (какой цвет ближе, тот и заменяет)
        Mat result = image.clone();

        for (int y = 0; y < image.rows(); y++) {
            for (int x = 0; x < image.cols(); x++) {
                if (isBorderPixel(image, x, y)) {
                    Imgproc.rectangle(result, new Point(x, y), new Point(x + 1, y + 1), defaultColors.get(0), Core.FILLED); // Черный для границы
                } else {
                    double[] pixelColor = image.get(y, x);
                    Scalar closestColor = findClosestColor(pixelColor, defaultColors); // поиск ближайшего
                    Imgproc.rectangle(result, new Point(x, y), new Point(x + 1, y + 1), closestColor, Core.FILLED);
                }
            }
        }
        return result;
    }

    private boolean isBorderPixel(Mat image, int x, int y) {
        int rows = image.rows();
        int cols = image.cols();
        return x == 0 || y == 0 || x == cols - 1 || y == rows - 1;
    }

    private Scalar findClosestColor(double[] pixelColor, List<Scalar> defaultColors) {
        Scalar closestColor = null;
        double minDistance = Double.MAX_VALUE;

        for (Scalar defaultColor : defaultColors) {
            double distance = calculateColorDistance(pixelColor, defaultColor.val);
            if (distance < minDistance) {
                minDistance = distance;
                closestColor = defaultColor;
            }
        }
        return closestColor;
    }

    private double calculateColorDistance(double[] color1, double[] color2) {
        double bDiff = color1[0] - color2[0];
        double gDiff = color1[1] - color2[1];
        double rDiff = color1[2] - color2[2];
        return Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff); // евклидово расстояние
    }

    private List<Scalar> getDefaultColors() {
        List<Scalar> defaultColors = new ArrayList<>();
        defaultColors.add(new Scalar(0, 0, 0));        // Черный
        defaultColors.add(new Scalar(255, 255, 255)); // Белый
        defaultColors.add(new Scalar(255, 0, 0));    // Красный
        defaultColors.add(new Scalar(50, 0, 100));    // Темно-красный
        defaultColors.add(new Scalar(0, 180, 0));     // Зеленый
        defaultColors.add(new Scalar(100, 50, 150));    // Коричневый
        return defaultColors;
    }
}