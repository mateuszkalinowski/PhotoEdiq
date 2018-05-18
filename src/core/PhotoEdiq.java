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

        exportPicture = new MenuItem("Zapisz");
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

        closePhoto = new MenuItem("Zamknij zdjęcie");
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

        flipHorizontal = new MenuItem("Przerzuć w poziomie");
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

        flipVertical = new MenuItem("Przerzuć w pionie");
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
            mainImage = null;
            backupRotatedImage = null;
            mainImage = new WritableImage((int)backupImage.getWidth(),(int)backupImage.getHeight());
            backupRotatedImage = new WritableImage((int)backupImage.getWidth(),(int)backupImage.getHeight());
            for(int i = 0 ; i < mainImage.getWidth();i++) {
                for(int j = 0; j < mainImage.getHeight();j++)  {
                    mainImage.getPixelWriter().setColor(i,j,backupImage.getPixelReader().getColor(i,j));
                    backupRotatedImage.getPixelWriter().setColor(i,j,backupImage.getPixelReader().getColor(i,j));
                }
            }
            resetSliders();
//
            drawFrame();
        });

        menuEdit.getItems().addAll(rotateLeftMenuItem,rotateRightMenuItem,new SeparatorMenuItem(),flipHorizontal,flipVertical, new SeparatorMenuItem(),restoreDefaultMenuItem);

        mainBorderPane.setTop(menuBar);

        GridPane controlsGridPane = new GridPane();

        ColumnConstraints mainColumnInControlsGridPane = new ColumnConstraints();
        mainColumnInControlsGridPane.setPercentWidth(50);
        controlsGridPane.getColumnConstraints().addAll(mainColumnInControlsGridPane,mainColumnInControlsGridPane);

        RowConstraints oneRowInControlsGridPane = new RowConstraints();
        oneRowInControlsGridPane.setPercentHeight(6.5);
        for(int i = 0; i < 15; i ++)
            controlsGridPane.getRowConstraints().add(oneRowInControlsGridPane);



        Label basicOperationsLabel = new Label("Podstawowe operacje:");
        basicOperationsLabel.setMaxWidth(Double.MAX_VALUE);
        basicOperationsLabel.setAlignment(Pos.CENTER);


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

        Label constrastLabel = new Label("Kontrast:");
        constrastLabel.setMaxWidth(Double.MAX_VALUE);
        constrastLabel.setAlignment(Pos.CENTER);

        constastSlider = new Slider();
        constastSlider.setMin(-1.0);
        constastSlider.setMax(1.0);
        constastSlider.setValue(0.0);
        constastSlider.setMajorTickUnit(0.1);


        constastSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mainImage != null) {
                for (int i = 0; i < mainImage.getWidth(); i++) {
                    for (int j = 0; j < mainImage.getHeight(); j++) {
                        Color color = backupRotatedImage.getPixelReader().getColor(i, j);
                        double r = color.getRed();
                        double b = color.getBlue();
                        double g = color.getGreen();

                        double factor = (1.00 * (constastSlider.getValue() + 1.0)) / (1.0 * (1.00 - constastSlider.getValue()));

                        double newR = truncate(factor * (r-0.5) + 0.5);
                        double newB = truncate(factor * (b-0.5) + 0.5);
                        double newG = truncate(factor * (g-0.5) + 0.5);
                        mainImage.getPixelWriter().setColor(i, j, new Color(newR, newG, newB, 1));
                    }
                }
                drawFrame();
            }
        });


        Label colorsCorrectionLabel = new Label("Korekcja kolorów:");
        colorsCorrectionLabel.setMaxWidth(Double.MAX_VALUE);
        colorsCorrectionLabel.setAlignment(Pos.CENTER);



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
                        mainImage.getPixelWriter().setColor(i, j, new Color(newR, g, b, 1));
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

        Label blackAndWhiteFiltersLabel = new Label("Skala szarości:");
        blackAndWhiteFiltersLabel.setMaxWidth(Double.MAX_VALUE);
        blackAndWhiteFiltersLabel.setAlignment(Pos.CENTER);

        Label filtresLabel = new Label("Filtry:");
        filtresLabel.setMaxWidth(Double.MAX_VALUE);
        filtresLabel.setAlignment(Pos.CENTER);

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

        HBox effectsHBox = new HBox();
        effectsHBox.setSpacing(4);
        effectsHBox.setAlignment(Pos.CENTER);


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
                for(int i = 0; i < mainImage.getWidth();i++) {
                    for(int j = 0; j < mainImage.getHeight();j++) {
                        backupRotatedImage.getPixelWriter().setColor(i,j,mainImage.getPixelReader().getColor(i,j));
                    }
                }
                drawFrame();
            }
        });

        sepiaEffect = new Button("Sepia");
        sepiaEffect.setMaxWidth(Double.MAX_VALUE);
        sepiaEffect.setAlignment(Pos.CENTER);

        sepiaEffect.setOnAction(event -> {
            if (mainImage != null) {
                for (int i = 0; i < mainImage.getWidth(); i++) {
                    for (int j = 0; j < mainImage.getHeight(); j++) {
                        Color color = mainImage.getPixelReader().getColor(i, j);
                        double r = color.getRed();
                        double b = color.getBlue();
                        double g = color.getGreen();

                        double newR = 0.393 * r + 0.769 * g + 0.189 * b;
                        double newG = 0.349 * r + 0.686 * g + 0.168* b;
                        double newB = 0.272 * r + 0.534 * g + 0.131 * b;

                        if(newR>1.0)newR=1.0;
                        if(newG>1.0)newG=1.0;
                        if(newB>1.0)newB=1.0;

                        mainImage.getPixelWriter().setColor(i, j, new Color(newR, newG, newB, 1));
                    }
                }
                for(int i = 0; i < mainImage.getWidth();i++) {
                    for(int j = 0; j < mainImage.getHeight();j++) {
                        backupRotatedImage.getPixelWriter().setColor(i,j,mainImage.getPixelReader().getColor(i,j));
                    }
                }
                drawFrame();
            }
        });

        effectsHBox.getChildren().addAll(negativeEffect,sepiaEffect);


        HBox meanFilterHBox = new HBox();

        meanFilterHBox.setSpacing(4);
        meanFilterHBox.setAlignment(Pos.CENTER);

        meanFilterButton = new Button("Filtr uśredniający");
        meanFilterButton.setMaxWidth(Double.MAX_VALUE);
        meanFilterButton.setAlignment(Pos.CENTER);

        meanFilterButton.setOnAction(event -> {
            int mask = Integer.parseInt(maskComboBox.getSelectionModel().getSelectedItem().split("x")[0]);

            int maxJ = 0;
            int maxI = 0;

            for(int i = 0; i <= mainImage.getWidth()-mask;i+=mask){
                maxI = i;
                for(int j = 0; j <= mainImage.getHeight()-mask;j+=mask) {
                    maxJ = j;
                    double avgRed = 0;
                    double avgGreen = 0;
                    double avgBlue = 0;
                    for (int k = i; k < i + mask; k++) {
                        for (int l = j; l < j + mask; l++) {
                            avgRed += mainImage.getPixelReader().getColor(k, l).getRed();
                            avgGreen += mainImage.getPixelReader().getColor(k, l).getGreen();
                            avgBlue += mainImage.getPixelReader().getColor(k, l).getBlue();
                        }
                    }

                    avgRed = avgRed / (mask * mask * 1.0);
                    avgGreen = avgGreen / (mask * mask * 1.0);
                    avgBlue = avgBlue / (mask * mask * 1.0);
//                    System.out.println(avgRed + " " + avgGreen + " " + avgBlue);

                    for (int k = i; k < i + mask; k++) {
                        for (int l = j; l < j + mask; l++) {
                            Color newColor = new Color(avgRed, avgGreen, avgBlue, 1);
                            mainImage.getPixelWriter().setColor(k, l, newColor);
                        }
                    }
                }
            }

            for(int i = 0; i <= mainImage.getWidth()-mask;i+=mask){
                double avgRed = 0;
                double avgGreen = 0;
                double avgBlue = 0;
                double counter = 0.0;
                for (int k = i; k < i + mask; k++) {
                    for (int l = maxJ; l < (int)mainImage.getHeight(); l++) {
                        avgRed += mainImage.getPixelReader().getColor(k, l).getRed();
                        avgGreen += mainImage.getPixelReader().getColor(k, l).getGreen();
                        avgBlue += mainImage.getPixelReader().getColor(k, l).getBlue();
                        counter++;
                    }
                }

                avgRed = avgRed / counter;
                avgGreen = avgGreen / counter;
                avgBlue = avgBlue / counter;

                for (int k = i; k < i + mask; k++) {
                    for (int l = maxJ; l < (int)mainImage.getHeight(); l++) {
                        Color newColor = new Color(avgRed, avgGreen, avgBlue, 1);
                        mainImage.getPixelWriter().setColor(k, l, newColor);
                    }
                }
            }

            for(int j = 0; j <= mainImage.getHeight()-mask;j+=mask) {
                double avgRed = 0;
                double avgGreen = 0;
                double avgBlue = 0;
                double counter = 0.0;

                for (int k = maxI; k < (int)mainImage.getWidth(); k++) {
                    for (int l = j; l < j + mask; l++) {
                        avgRed += mainImage.getPixelReader().getColor(k, l).getRed();
                        avgGreen += mainImage.getPixelReader().getColor(k, l).getGreen();
                        avgBlue += mainImage.getPixelReader().getColor(k, l).getBlue();
                        counter++;
                    }
                }

                avgRed = avgRed / counter;
                avgGreen = avgGreen / counter;
                avgBlue = avgBlue / counter;

                for (int k = maxI; k < (int)mainImage.getWidth(); k++) {
                    for (int l = j; l < j + mask; l++) {
                        Color newColor = new Color(avgRed, avgGreen, avgBlue, 1);
                        mainImage.getPixelWriter().setColor(k, l, newColor);
                    }
                }
            }

            double avgRed = 0;
            double avgGreen = 0;
            double avgBlue = 0;
            double counter = 0.0;

            for (int k = maxI; k < (int)mainImage.getWidth(); k++) {
                for (int l = maxJ; l < (int)mainImage.getHeight(); l++) {
                    avgRed += mainImage.getPixelReader().getColor(k, l).getRed();
                    avgGreen += mainImage.getPixelReader().getColor(k, l).getGreen();
                    avgBlue += mainImage.getPixelReader().getColor(k, l).getBlue();
                    counter++;
                }
            }

            avgRed = avgRed / counter;
            avgGreen = avgGreen / counter;
            avgBlue = avgBlue / counter;

            for (int k = maxI; k < (int)mainImage.getWidth(); k++) {
                for (int l = maxJ; l < (int)mainImage.getHeight(); l++) {
                    Color newColor = new Color(avgRed, avgGreen, avgBlue, 1);
                    mainImage.getPixelWriter().setColor(k, l, newColor);
                }
            }

            for(int i = 0; i < mainImage.getWidth();i++) {
                for(int j = 0; j < mainImage.getHeight();j++) {
                    backupRotatedImage.getPixelWriter().setColor(i,j,mainImage.getPixelReader().getColor(i,j));
                }
            }

            drawFrame();
        });

        Label maskLabel = new Label("Maska:");
        maskLabel.setMaxWidth(Double.MAX_VALUE);
        maskLabel.setAlignment(Pos.CENTER);

        maskComboBox = new ComboBox<>();
        maskComboBox.getItems().addAll("3x3","5x5","7x7","10x10","25x25","50x50","75x75","100x100");
        maskComboBox.getSelectionModel().select(0);

        meanFilterHBox.getChildren().addAll(meanFilterButton,maskLabel,maskComboBox);

        mainGridPane.add(controlsGridPane,1,0,1,10);

        controlsGridPane.add(basicOperationsLabel,0,0,2,1);
        controlsGridPane.add(brightnessLabel,0,1);
        controlsGridPane.add(brightnessSlider,1,1);

        controlsGridPane.add(colorsCorrectionLabel,0,4,2,1);
        controlsGridPane.add(redLabel,0,5);
        controlsGridPane.add(redSlider,1,5);
        controlsGridPane.add(greenLabel,0,6);
        controlsGridPane.add(greenSlider,1,6);
        controlsGridPane.add(blueLabel,0,7);
        controlsGridPane.add(blueSlider,1,7);

        controlsGridPane.add(filtresLabel,0,10,2,1);

        controlsGridPane.add(meanFilterHBox,0,11,2,1);
        controlsGridPane.add(effectsHBox,0,12,2,1);

        controlsGridPane.add(blackAndWhiteFiltersLabel,0,13,2,1);

        HBox bawHBox = new HBox();
        bawHBox.setAlignment(Pos.CENTER);
        bawHBox.getChildren().addAll(lightnessgreyScaleButton,averagegreyScaleButton,luminositygreyScaleButton);

        controlsGridPane.add(bawHBox,0,14,2,1);

        mainStage = primaryStage;
        mainStage.setTitle("PhotoEdiq");
        Scene mainScene = new Scene(mainBorderPane, 1000, 700);
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

    private double truncate(double value) {
        if(value>=1.0)
            return value-1.0;
        else if(value<=0.0)
            return value+1.0;
        else
            return value;
    }

    private Canvas mainPictureCanvas = new Canvas();

    private Slider brightnessSlider;
    private Slider constastSlider;


    private Slider redSlider;
    private Slider greenSlider;
    private Slider blueSlider;


    private void resetSliders(){
        brightnessSlider.setValue(1.0);
        constastSlider.setValue(0.0);

        redSlider.setValue(1.0);
        greenSlider.setValue(1.0);
        blueSlider.setValue(1.0);
    }

    private void disableAll(){

        exportPicture.setDisable(true);
        closePhoto.setDisable(true);

        brightnessSlider.setDisable(true);
        constastSlider.setDisable(true);

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
        sepiaEffect.setDisable(true);

        meanFilterButton.setDisable(true);

        restoreDefaultMenuItem.setDisable(true);

        maskComboBox.setDisable(true);

    }
    private void enableAll(){

        exportPicture.setDisable(false);
        closePhoto.setDisable(false);

        brightnessSlider.setDisable(false);
        constastSlider.setDisable(false);

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
        sepiaEffect.setDisable(false);

        meanFilterButton.setDisable(false);

        restoreDefaultMenuItem.setDisable(false);

        maskComboBox.setDisable(false);
    }

    private MenuItem exportPicture;
    private MenuItem closePhoto;

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
    private Button sepiaEffect;

    private Button meanFilterButton;

    private MenuItem restoreDefaultMenuItem;

    private ComboBox<String> maskComboBox;
}
