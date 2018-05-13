package core;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.net.URL;
import java.util.Optional;

public class PhotoEdiq extends Application {

    @Override
    public void start(Stage primaryStage) {

        BorderPane mainBorderPane = new BorderPane();

        GridPane mainGridPane = new GridPane();
        ColumnConstraints photoColumn = new ColumnConstraints();
        photoColumn.setPercentWidth(70);
        ColumnConstraints controllsColumn = new ColumnConstraints();
        controllsColumn.setPercentWidth(30);

        RowConstraints mainRow = new RowConstraints();
        mainRow.setPercentHeight(100);

        mainGridPane.getColumnConstraints().addAll(photoColumn,controllsColumn);
        mainGridPane.getRowConstraints().addAll(mainRow);

        mainBorderPane.setCenter(mainGridPane);
        mainGridPane.add(mainPictureCanvas,0,0);

        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("Plik");
        Menu menuEdit = new Menu("Edycja");
        menuBar.getMenus().addAll(menuFile,menuEdit);
        MenuItem importPicture = new MenuItem("Otwórz");
        importPicture.setAccelerator(new KeyCodeCombination(KeyCode.O,KeyCombination.CONTROL_DOWN));
        importPicture.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                Image sourceImage = new Image(file.toURI().toString());
                mainImage = new WritableImage(sourceImage.getPixelReader(),(int)sourceImage.getWidth(),(int)sourceImage.getHeight());
                backupImage = new WritableImage(sourceImage.getPixelReader(),(int)sourceImage.getWidth(),(int)sourceImage.getHeight());
                backupRotatedImage = new WritableImage(sourceImage.getPixelReader(),(int)sourceImage.getWidth(),(int)sourceImage.getHeight());
                resetSliders();
                drawFrame();
                enableAll();
                }
                catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Błąd wczytywania pliku");
                    alert.setHeaderText("Nie można było otworzyć pliku");
                    alert.setContentText("Wygląda na to, że plik który próbowałeś otworzyć nie jest zdjęciem");

                    alert.showAndWait();
                }
            }
        });

        MenuItem exportPicture = new MenuItem("Zapisz");
        exportPicture.setAccelerator(new KeyCodeCombination(KeyCode.S,KeyCombination.CONTROL_DOWN));
        exportPicture.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("edited.png");
            File file = fileChooser.showSaveDialog(mainStage);
            if(file!=null) {
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(mainImage, null);
                try {
                    ImageIO.write(renderedImage, "png", file);
                } catch (Exception ignored) {

                }
            }
        });

        MenuItem closePhoto = new MenuItem("Zamknij zdjęcie");
        closePhoto.setOnAction(event -> {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Potwierdzenie zamknięcia zdjęcia");
            alert.setHeaderText("Czy na pewno zamknąć otwarte zdjęcie?");
            alert.setContentText("Wszelkie niezapisane zmiany zostaną utracone.");
            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK) {
                mainImage = null;
                backupRotatedImage = null;
                backupImage = null;
                drawFrame();
                disableAll();
            }
        });

        MenuItem close = new MenuItem("Opuść program");
        close.setAccelerator(new KeyCodeCombination(KeyCode.Q,KeyCombination.CONTROL_DOWN));
        close.setOnAction(event -> System.exit(0));

        menuFile.getItems().addAll(importPicture,exportPicture,closePhoto,new SeparatorMenuItem(),close);

        rotateLeftMenuItem = new MenuItem("Obróc w lewo");
        rotateLeftMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.L,KeyCombination.CONTROL_DOWN));
        rotateLeftMenuItem.setOnAction(event -> {
            if(mainImage!=null) {
                WritableImage rotatedImage = new WritableImage((int) mainImage.getHeight(), (int) mainImage.getWidth());

                for (int i = 0; i < mainImage.getHeight(); i++) {
                    for (int j = 0; j < mainImage.getWidth(); j++) {

                        rotatedImage.getPixelWriter().setColor(i, j, mainImage.getPixelReader().getColor((int) mainImage.getWidth() - 1 - j, i));
                    }
                }

                mainImage = new WritableImage((int) rotatedImage.getWidth(),(int)rotatedImage.getHeight());
                backupRotatedImage = new WritableImage((int) rotatedImage.getWidth(),(int)rotatedImage.getHeight());

                for(int i = 0 ; i < rotatedImage.getWidth();i++) {
                    for(int j = 0 ; j < rotatedImage.getHeight();j++) {
                        mainImage.getPixelWriter().setColor(i,j,rotatedImage.getPixelReader().getColor(i,j));
                        backupRotatedImage.getPixelWriter().setColor(i,j,rotatedImage.getPixelReader().getColor(i,j));
                    }
                }

                drawFrame();
            }
        });

        rotateRightMenuItem = new MenuItem("Obróc w prawo");
        rotateRightMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R,KeyCombination.CONTROL_DOWN));
        rotateRightMenuItem.setOnAction(event -> {
            if(mainImage!=null) {
                WritableImage rotatedImage = new WritableImage((int) mainImage.getHeight(), (int) mainImage.getWidth());

                for (int i = 0; i < mainImage.getHeight(); i++) {
                    for (int j = 0; j < mainImage.getWidth(); j++) {

                        rotatedImage.getPixelWriter().setColor(i, j, mainImage.getPixelReader().getColor(j, (int) mainImage.getHeight() - 1 - i));
                    }
                }
                mainImage = new WritableImage((int) rotatedImage.getWidth(),(int)rotatedImage.getHeight());
                backupRotatedImage = new WritableImage((int) rotatedImage.getWidth(),(int)rotatedImage.getHeight());

                for(int i = 0 ; i < rotatedImage.getWidth();i++) {
                    for(int j = 0 ; j < rotatedImage.getHeight();j++) {
                        mainImage.getPixelWriter().setColor(i,j,rotatedImage.getPixelReader().getColor(i,j));
                        backupRotatedImage.getPixelWriter().setColor(i,j,rotatedImage.getPixelReader().getColor(i,j));
                    }
                }

                drawFrame();
            }

        });

        flipHorizontal = new MenuItem("Przerzuć w pionie");
        flipHorizontal.setOnAction(event -> {
            if(mainImage!=null) {
                for (int i = 0; i < mainImage.getWidth(); i++) {
                    for (int j = 0; j < mainImage.getHeight()/2.0; j++) {
                            Color tmp = mainImage.getPixelReader().getColor(i,j);
                            mainImage.getPixelWriter().setColor(i,j,mainImage.getPixelReader().getColor(i,(int)mainImage.getHeight()-1-j));
                            mainImage.getPixelWriter().setColor(i,(int)mainImage.getHeight()-1-j,tmp);
                            backupRotatedImage.getPixelWriter().setColor(i,j,mainImage.getPixelReader().getColor(i,j));
                        }
                    }
                    drawFrame();
            }
        });

        flipVertical = new MenuItem("Przerzuć w poziomie");
        flipVertical.setOnAction(event -> {
            if(mainImage!=null) {
                for (int i = 0; i < mainImage.getWidth()/2.0; i++) {
                    for (int j = 0; j < mainImage.getHeight(); j++) {
                        Color tmp = mainImage.getPixelReader().getColor(i,j);
                        mainImage.getPixelWriter().setColor(i,j,mainImage.getPixelReader().getColor((int)mainImage.getWidth()-1-i,j));
                        mainImage.getPixelWriter().setColor((int)mainImage.getWidth()-1-i,j,tmp);
                        backupRotatedImage.getPixelWriter().setColor(i,j,mainImage.getPixelReader().getColor(i,j));
                    }
                }
                drawFrame();
            }
        });

        restoreDefaultMenuItem = new MenuItem("Przywróć początkowy");
        restoreDefaultMenuItem.setOnAction(event -> {
            mainImage = new WritableImage((int)backupImage.getWidth(),(int)backupImage.getHeight());
            backupRotatedImage = new WritableImage((int)backupImage.getWidth(),(int)backupImage.getHeight());
            for(int i = 0 ; i < mainImage.getWidth();i++) {
                for(int j = 0; j < mainImage.getHeight();j++)  {
                    mainImage.getPixelWriter().setColor(i,j,backupImage.getPixelReader().getColor(i,j));
                    backupRotatedImage.getPixelWriter().setColor(i,j,backupImage.getPixelReader().getColor(i,j));
                }
            }
            resetSliders();

            drawFrame();
        });

        menuEdit.getItems().addAll(rotateLeftMenuItem,rotateRightMenuItem,new SeparatorMenuItem(),flipHorizontal,flipVertical, new SeparatorMenuItem(),restoreDefaultMenuItem);

        mainBorderPane.setTop(menuBar);

        GridPane controlsGridPane = new GridPane();

        ColumnConstraints mainColumnInControlsGridPane = new ColumnConstraints();
        mainColumnInControlsGridPane.setPercentWidth(50);
        controlsGridPane.getColumnConstraints().addAll(mainColumnInControlsGridPane,mainColumnInControlsGridPane);

        RowConstraints oneRowInControlsGridPane = new RowConstraints();
        oneRowInControlsGridPane.setPercentHeight(10);
        for(int i = 0; i < 10; i ++)
            controlsGridPane.getRowConstraints().add(oneRowInControlsGridPane);



        Label brightnessLabel = new Label("Jasność:");
        brightnessLabel.setMaxWidth(Double.MAX_VALUE);
        brightnessLabel.setAlignment(Pos.CENTER);

        brightnessSlider = new Slider();
        brightnessSlider.setMin(0.1);
        brightnessSlider.setMax(2.0);
        brightnessSlider.setValue(1.0);
        brightnessSlider.setMajorTickUnit(0.1);


        brightnessSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mainImage != null) {
                for (int i = 0; i < mainImage.getWidth(); i++) {
                    for (int j = 0; j < mainImage.getHeight(); j++) {
                        Color color = backupRotatedImage.getPixelReader().getColor(i, j);
                        double r = color.getRed();
                        double b = color.getBlue();
                        double g = color.getGreen();

                        double newR = r * brightnessSlider.getValue();
                        if(newR>1.0)newR=1.0;
                        double newB = b * brightnessSlider.getValue();
                        if(newB>1.0)newB=1.0;
                        double newG = g * brightnessSlider.getValue();
                        if(newG>1.0)newG=1.0;
                        mainImage.getPixelWriter().setColor(i, j, new Color(newR, newG, newB, 1));
                    }
                }
                drawFrame();
            }
        });

        Label redLabel = new Label("Czerwony:");
        redLabel.setMaxWidth(Double.MAX_VALUE);
        redLabel.setAlignment(Pos.CENTER);

        redSlider = new Slider();
        redSlider.setMin(0.1);
        redSlider.setMax(2.0);
        redSlider.setValue(1.0);
        redSlider.setMajorTickUnit(0.1);

        redSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mainImage != null) {
                for (int i = 0; i < mainImage.getWidth(); i++) {
                    for (int j = 0; j < mainImage.getHeight(); j++) {
                        Color color = backupRotatedImage.getPixelReader().getColor(i, j);
                        double r = color.getRed();
                        double b = color.getBlue();
                        double g = color.getGreen();

                        double newR = r * redSlider.getValue();
                        if(newR>1.0)newR=1.0;
                        mainImage.getPixelWriter().setColor(i, j, new Color(newR, b, g, 1));
                    }
                }
                drawFrame();
            }
        });

        Label greenLabel = new Label("Zielony:");
        greenLabel.setMaxWidth(Double.MAX_VALUE);
        greenLabel.setAlignment(Pos.CENTER);

        greenSlider = new Slider();
        greenSlider.setMin(0.1);
        greenSlider.setMax(2.0);
        greenSlider.setValue(1.0);
        greenSlider.setMajorTickUnit(0.1);

        greenSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mainImage != null) {
                for (int i = 0; i < mainImage.getWidth(); i++) {
                    for (int j = 0; j < mainImage.getHeight(); j++) {
                        Color color = backupRotatedImage.getPixelReader().getColor(i, j);
                        double r = color.getRed();
                        double b = color.getBlue();
                        double g = color.getGreen();

                        double newG = g * greenSlider.getValue();
                        if(newG>1.0)newG=1.0;
                        mainImage.getPixelWriter().setColor(i, j, new Color(r, newG, b, 1));
                    }
                }
                drawFrame();
            }
        });


        Label blueLabel = new Label("Niebieski:");
        blueLabel.setMaxWidth(Double.MAX_VALUE);
        blueLabel.setAlignment(Pos.CENTER);

        blueSlider = new Slider();
        blueSlider.setMin(0.1);
        blueSlider.setMax(2.0);
        blueSlider.setValue(1.0);
        blueSlider.setMajorTickUnit(0.1);

        blueSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mainImage != null) {
                for (int i = 0; i < mainImage.getWidth(); i++) {
                    for (int j = 0; j < mainImage.getHeight(); j++) {
                        Color color = backupRotatedImage.getPixelReader().getColor(i, j);
                        double r = color.getRed();
                        double b = color.getBlue();
                        double g = color.getGreen();

                        double newB = b * blueSlider.getValue();
                        if(newB>1.0)newB=1.0;
                        mainImage.getPixelWriter().setColor(i, j, new Color(r, g, newB, 1));
                    }
                }
                drawFrame();
            }
        });

        Label blackAndWhiteFiltersLabel = new Label("Filtry czarnobiałe:");
        blackAndWhiteFiltersLabel.setMaxWidth(Double.MAX_VALUE);
        blackAndWhiteFiltersLabel.setAlignment(Pos.CENTER);

        lightnessgreyScaleButton = new Button("Lightness");
        lightnessgreyScaleButton.setOnAction(event -> {
            if (mainImage != null) {
                for (int i = 0; i < mainImage.getWidth(); i++) {
                    for (int j = 0; j < mainImage.getHeight(); j++) {
                        Color color = backupRotatedImage.getPixelReader().getColor(i, j);
                        double r = color.getRed();
                        double b = color.getBlue();
                        double g = color.getGreen();


                        double newColor = (max(r, g, b) + min(r, g, b)) / 2.0;
                        mainImage.getPixelWriter().setColor(i, j, new Color(newColor, newColor, newColor, 1));
                    }
                }
                drawFrame();
            }
        });
        averagegreyScaleButton = new Button("Average");
        averagegreyScaleButton.setOnAction(event -> {
            if (mainImage != null) {
                for (int i = 0; i < mainImage.getWidth(); i++) {
                    for (int j = 0; j < mainImage.getHeight(); j++) {
                        Color color = backupRotatedImage.getPixelReader().getColor(i, j);
                        double r = color.getRed();
                        double b = color.getBlue();
                        double g = color.getGreen();

                        double newColor = (r + b + g) / 3.0;
                        mainImage.getPixelWriter().setColor(i, j, new Color(newColor, newColor, newColor, 1));
                    }
                }
                drawFrame();
            }
        });

        luminositygreyScaleButton = new Button("Luminosity");
        luminositygreyScaleButton.setOnAction(event -> {
            if (mainImage != null) {
                for (int i = 0; i < mainImage.getWidth(); i++) {
                    for (int j = 0; j < mainImage.getHeight(); j++) {
                        Color color = backupRotatedImage.getPixelReader().getColor(i, j);
                        double r = color.getRed();
                        double b = color.getBlue();
                        double g = color.getGreen();

                        double newColor = r * 0.21 + b * 0.07 + g * 0.72;
                        mainImage.getPixelWriter().setColor(i, j, new Color(newColor, newColor, newColor, 1));
                    }
                }
                drawFrame();
            }
        });
//
//        restoreDefault = new Button("Przywróć początkowy");
//        restoreDefault.setMaxWidth(Double.MAX_VALUE);
//        restoreDefault.setAlignment(Pos.CENTER);
//        restoreDefault.setOnAction(event -> {
//            mainImage = new WritableImage((int)backupImage.getWidth(),(int)backupImage.getHeight());
//            backupRotatedImage = new WritableImage((int)backupImage.getWidth(),(int)backupImage.getHeight());
//            for(int i = 0 ; i < mainImage.getWidth();i++) {
//                for(int j = 0; j < mainImage.getHeight();j++)  {
//                    mainImage.getPixelWriter().setColor(i,j,backupImage.getPixelReader().getColor(i,j));
//                    backupRotatedImage.getPixelWriter().setColor(i,j,backupImage.getPixelReader().getColor(i,j));
//                }
//            }
//            resetSliders();
//
//            drawFrame();
//        });

        negativeEffect = new Button("Negatyw");
        negativeEffect.setMaxWidth(Double.MAX_VALUE);
        negativeEffect.setAlignment(Pos.CENTER);

        negativeEffect.setOnAction(event -> {
            if (mainImage != null) {
                for (int i = 0; i < mainImage.getWidth(); i++) {
                    for (int j = 0; j < mainImage.getHeight(); j++) {
                        Color color = mainImage.getPixelReader().getColor(i, j);
                        double r = color.getRed();
                        double b = color.getBlue();
                        double g = color.getGreen();

                        mainImage.getPixelWriter().setColor(i, j, new Color(1-r, 1-g, 1-b, 1));
                    }
                }
                drawFrame();
            }
        });

        mainGridPane.add(controlsGridPane,1,0,1,10);

        controlsGridPane.add(brightnessLabel,0,0);
        controlsGridPane.add(brightnessSlider,1,0);

        controlsGridPane.add(redLabel,0,2);
        controlsGridPane.add(redSlider,1,2);
        controlsGridPane.add(greenLabel,0,3);
        controlsGridPane.add(greenSlider,1,3);
        controlsGridPane.add(blueLabel,0,4);
        controlsGridPane.add(blueSlider,1,4);

        controlsGridPane.add(negativeEffect,0,5,2,1);



        controlsGridPane.add(blackAndWhiteFiltersLabel,0,6,2,1);

        HBox bawHBox = new HBox();
        bawHBox.setAlignment(Pos.CENTER);
        bawHBox.getChildren().addAll(lightnessgreyScaleButton,averagegreyScaleButton,luminositygreyScaleButton);

        controlsGridPane.add(bawHBox,0,7,2,1);

//        HBox rotationHBox = new HBox();
//        rotationHBox.setAlignment(Pos.CENTER);
//        rotationHBox.getChildren().addAll(rotateRightButton,rotateLeftButton);
//        controlsGridPane.add(rotationHBox,0,8,2,1);

      //  controlsGridPane.add(restoreDefault,0,9,2,1);

        mainStage = primaryStage;
        mainStage.setTitle("PhotoEdiq");
        Scene mainScene = new Scene(mainBorderPane, 800, 600);
        mainStage.setScene(mainScene);
        mainStage.show();

        mainStage.setMinWidth(700);
        mainStage.setMinHeight(520);

        mainPictureCanvas.setWidth(0.7*mainStage.getWidth());
        mainPictureCanvas.setHeight(mainStage.getHeight());

        drawFrame();

        mainStage.heightProperty().addListener((observable, oldValue, newValue) -> drawFrame());

        mainStage.widthProperty().addListener((observable, oldValue, newValue) -> drawFrame());


        mainScene.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        mainScene.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                String filePath;
                for (File file:db.getFiles()) {
                    filePath = file.getAbsolutePath();

                    File fileFromDropped = new File(filePath);
                    try {
                        Image sourceImage = new Image(fileFromDropped.toURI().toString());
                        mainImage = new WritableImage(sourceImage.getPixelReader(), (int) sourceImage.getWidth(), (int) sourceImage.getHeight());
                        backupImage = new WritableImage(sourceImage.getPixelReader(), (int) sourceImage.getWidth(), (int) sourceImage.getHeight());
                        backupRotatedImage = new WritableImage(sourceImage.getPixelReader(), (int) sourceImage.getWidth(), (int) sourceImage.getHeight());
                        resetSliders();
                        drawFrame();
                        enableAll();
                    }
                    catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Błąd wczytywania pliku");
                        alert.setHeaderText("Nie można było otworzyć pliku");
                        alert.setContentText("Wygląda na to, że plik który próbowałeś otworzyć nie jest zdjęciem");

                        alert.showAndWait();
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
        disableAll();

        try {
            URL iconURL = PhotoEdiq.class.getResource("icon.png");
            java.awt.Image image = new ImageIcon(iconURL).getImage();
            com.apple.eawt.Application.getApplication().setDockIconImage(image);
        } catch (Exception ignored) {

        }

    }

    private static double max(double... n) {
        int i = 0;
        double max = n[i];

        while (++i < n.length)
            if (n[i] > max)
                max = n[i];

        return max;
    }

    private static double min(double... n) {
        int i = 0;
        double min = n[i];

        while (++i > n.length)
            if (n[i] > min)
                min = n[i];

        return min;
    }

    private void drawFrame() {


        mainPictureCanvas.setWidth(0.7*mainStage.getWidth());
        mainPictureCanvas.setHeight(mainStage.getHeight()-39.0);

        GraphicsContext gc = mainPictureCanvas.getGraphicsContext2D();

        if(mainImage!=null) {

            gc.setFill(Color.WHITE);
            gc.fillRect(0,0,mainPictureCanvas.getWidth(),mainPictureCanvas.getHeight());

            double height = mainPictureCanvas.getHeight();
            double width = mainImage.getWidth() * (mainPictureCanvas.getHeight()/mainImage.getHeight());

            if(width>mainPictureCanvas.getWidth()) {
                width = mainPictureCanvas.getWidth();
                height = mainImage.getHeight() * ((0.7 * mainStage.getWidth()) / mainImage.getWidth());
            }

            if (height > mainPictureCanvas.getHeight()) {
                double oldHeight = height;
                height = mainPictureCanvas.getHeight();
                width = (height / oldHeight) * width;
            }

            double posx = 0;
            double posy = 0;

            if(width<(0.7*mainStage.getWidth())) {
                posx = (mainPictureCanvas.getWidth() - width)/2.0;
            }

            if(height<mainPictureCanvas.getHeight()) {
                posy = (mainPictureCanvas.getHeight()-height)/2.0;
            }

            gc.drawImage(mainImage, posx, posy, width, height);
            }
            else {
            gc.setFill(Color.WHITE);
            gc.fillRect(0,0,mainPictureCanvas.getWidth(),mainPictureCanvas.getHeight());
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFill(Color.BLACK);
            gc.setStroke(Color.BLACK);
            gc.fillText(
                    "Upuść tu zdjęcie, lub wczytaj je przy użyciu opcji Plik/Otwórz",
                    Math.round(mainPictureCanvas.getWidth()  / 2),
                    Math.round(mainPictureCanvas.getHeight() / 2)
            );
        }

    }



    public static void main(String[] args) {
        launch(args);
    }

    private Canvas mainPictureCanvas = new Canvas();

    private Slider brightnessSlider;

    private Slider redSlider;
    private Slider greenSlider;
    private Slider blueSlider;

    private void resetSliders(){
        brightnessSlider.setValue(1.0);

        redSlider.setValue(1.0);
        greenSlider.setValue(1.0);
        blueSlider.setValue(1.0);
    }

    private void disableAll(){
        brightnessSlider.setDisable(true);
        redSlider.setDisable(true);
        greenSlider.setDisable(true);
        blueSlider.setDisable(true);

        rotateLeftMenuItem.setDisable(true);
        rotateRightMenuItem.setDisable(true);

        flipHorizontal.setDisable(true);
        flipVertical.setDisable(true);

        lightnessgreyScaleButton.setDisable(true);
        averagegreyScaleButton.setDisable(true);
        luminositygreyScaleButton.setDisable(true);

        negativeEffect.setDisable(true);

        restoreDefaultMenuItem.setDisable(true);

    }
    private void enableAll(){
        brightnessSlider.setDisable(false);
        redSlider.setDisable(false);
        greenSlider.setDisable(false);
        blueSlider.setDisable(false);

        rotateLeftMenuItem.setDisable(false);
        rotateRightMenuItem.setDisable(false);

        flipHorizontal.setDisable(false);
        flipVertical.setDisable(false);

        lightnessgreyScaleButton.setDisable(false);
        averagegreyScaleButton.setDisable(false);
        luminositygreyScaleButton.setDisable(false);

        negativeEffect.setDisable(false);

        restoreDefaultMenuItem.setDisable(false);
    }

    private WritableImage mainImage;
    private WritableImage backupImage;
    private WritableImage backupRotatedImage;
    private Stage mainStage;

    private MenuItem rotateLeftMenuItem;
    private MenuItem rotateRightMenuItem;

    private MenuItem flipHorizontal;
    private MenuItem flipVertical;

    private Button lightnessgreyScaleButton;
    private Button averagegreyScaleButton;
    private Button luminositygreyScaleButton;

    private Button negativeEffect;

    private MenuItem restoreDefaultMenuItem;
}
