package core;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;

import static jdk.nashorn.internal.objects.NativeMath.max;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        BorderPane mainBorderPane = new BorderPane();

        mainGridPane = new GridPane();
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
        menuBar.getMenus().addAll(menuFile);
        MenuItem importPicture = new MenuItem("Otwórz");
        importPicture.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    Image sourceImage = new Image(file.toURI().toString());
                    mainImage = new WritableImage(sourceImage.getPixelReader(),(int)sourceImage.getWidth(),(int)sourceImage.getHeight());
                    backupImage = new WritableImage(sourceImage.getPixelReader(),(int)sourceImage.getWidth(),(int)sourceImage.getHeight());
                    backupRotatedImage = new WritableImage(sourceImage.getPixelReader(),(int)sourceImage.getWidth(),(int)sourceImage.getHeight());
                    brightnessSlider.setValue(1.0);
                    drawFrame();
                }
            }
        });

        MenuItem exportPicture = new MenuItem("Zapisz");
        exportPicture.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showSaveDialog(mainStage);
                if(file!=null) {
                    RenderedImage renderedImage = SwingFXUtils.fromFXImage(mainImage, null);
                    try {
                        ImageIO.write(renderedImage, "png", file);
                    } catch (Exception ignored) {

                    }
                }
            }
        });

        MenuItem close = new MenuItem("Zamknij");
        close.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.exit(0);
            }
        });

        menuFile.getItems().addAll(importPicture,exportPicture,new SeparatorMenuItem(),close);

        mainBorderPane.setTop(menuBar);

        controlsGridPane = new GridPane();

        ColumnConstraints mainColumnInControlsGridPane = new ColumnConstraints();
        mainColumnInControlsGridPane.setPercentWidth(100);
        controlsGridPane.getColumnConstraints().addAll(mainColumnInControlsGridPane);

        RowConstraints oneRowInControlsGridPane = new RowConstraints();
        oneRowInControlsGridPane.setPercentHeight(10);
        for(int i = 0; i < 10; i ++)
            controlsGridPane.getRowConstraints().add(oneRowInControlsGridPane);



        Label brightnessLabel = new Label("Jasność:");

        brightnessSlider = new Slider();
        brightnessSlider.setMin(0.1);
        brightnessSlider.setMax(2.0);
        brightnessSlider.setValue(1.0);
        brightnessSlider.setMajorTickUnit(0.1);

        HBox brightnessHBox = new HBox();
        brightnessHBox.setAlignment(Pos.CENTER);
        brightnessHBox.getChildren().addAll(brightnessLabel,brightnessSlider);

        brightnessSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
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
            }
        });



        Label redLabel = new Label("Czerwony:");

        redSlider = new Slider();
        redSlider.setMin(0.1);
        redSlider.setMax(2.0);
        redSlider.setValue(1.0);
        redSlider.setMajorTickUnit(0.1);

        HBox redHBox = new HBox();
        redHBox.setAlignment(Pos.CENTER);
        redHBox.getChildren().addAll(redLabel,redSlider);

        redSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
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
            }
        });

        Label greenLabel = new Label("Zielony:");

        greenSlider = new Slider();
        greenSlider.setMin(0.1);
        greenSlider.setMax(2.0);
        greenSlider.setValue(1.0);
        greenSlider.setMajorTickUnit(0.1);

        HBox greenHBox = new HBox();
        greenHBox.setAlignment(Pos.CENTER);
        greenHBox.getChildren().addAll(greenLabel,greenSlider);

        greenSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (mainImage != null) {
                    for (int i = 0; i < mainImage.getWidth(); i++) {
                        for (int j = 0; j < mainImage.getHeight(); j++) {
                            Color color = backupRotatedImage.getPixelReader().getColor(i, j);
                            double r = color.getRed();
                            double b = color.getBlue();
                            double g = color.getGreen();

                            double newG = g * greenSlider.getValue();
                            if(newG>1.0)newG=1.0;
                            mainImage.getPixelWriter().setColor(i, j, new Color(r, b, newG, 1));
                        }
                    }
                    drawFrame();
                }
            }
        });


        Label blueLabel = new Label("Niebieski:");

        blueSlider = new Slider();
        blueSlider.setMin(0.1);
        blueSlider.setMax(2.0);
        blueSlider.setValue(1.0);
        blueSlider.setMajorTickUnit(0.1);

        HBox blueHBox = new HBox();
        blueHBox.setAlignment(Pos.CENTER);
        blueHBox.getChildren().addAll(blueLabel,blueSlider);

        greenSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (mainImage != null) {
                    for (int i = 0; i < mainImage.getWidth(); i++) {
                        for (int j = 0; j < mainImage.getHeight(); j++) {
                            Color color = backupRotatedImage.getPixelReader().getColor(i, j);
                            double r = color.getRed();
                            double b = color.getBlue();
                            double g = color.getGreen();

                            double newB = b * blueSlider.getValue();
                            if(newB>1.0)newB=1.0;
                            mainImage.getPixelWriter().setColor(i, j, new Color(r, newB, g, 1));
                        }
                    }
                    drawFrame();
                }
            }
        });


        Button rotateLeftButton = new Button("Obróc w prawo");
        rotateLeftButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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

            }
        });

        Button rotateRightButton = new Button("Obróć w lewo");
        rotateRightButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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

            }
        });


        Label blackAndWhiteFiltersLabel = new Label("Filtry czarnobiałe:");
        blackAndWhiteFiltersLabel.setMaxWidth(Double.MAX_VALUE);
        blackAndWhiteFiltersLabel.setAlignment(Pos.CENTER);

        Button lightnessgreyScaleButton = new Button("Lightness");
        lightnessgreyScaleButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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
            }
        });

        Button averagegreyScaleButton = new Button("Average");
        averagegreyScaleButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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
            }
        });

        Button luminositygreyScaleButton = new Button("Luminosity");
        luminositygreyScaleButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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
            }
        });

        Button restoreDefault = new Button("Przywróc początkowy");
        restoreDefault.setMaxWidth(Double.MAX_VALUE);
        restoreDefault.setAlignment(Pos.CENTER);
        restoreDefault.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mainImage = new WritableImage((int)backupImage.getWidth(),(int)backupImage.getHeight());
                for(int i = 0 ; i < mainImage.getWidth();i++) {
                    for(int j = 0; j < mainImage.getHeight();j++)  {
                        mainImage.getPixelWriter().setColor(i,j,backupImage.getPixelReader().getColor(i,j));
                    }
                }
                brightnessSlider.setValue(1.0);
                drawFrame();
            }
        });

        mainGridPane.add(controlsGridPane,1,0,1,10);

        controlsGridPane.add(brightnessHBox,0,1);

        controlsGridPane.add(redHBox,0,3);
        controlsGridPane.add(greenHBox,0,4);
        controlsGridPane.add(blueHBox,0,5);


        HBox rotationHBox = new HBox();
        rotationHBox.setAlignment(Pos.CENTER);
        rotationHBox.getChildren().addAll(rotateRightButton,rotateLeftButton);

        controlsGridPane.add(rotationHBox,0,6);

        controlsGridPane.add(blackAndWhiteFiltersLabel,0,7);

        HBox bawHBox = new HBox();
        bawHBox.setAlignment(Pos.CENTER);
        bawHBox.getChildren().addAll(lightnessgreyScaleButton,averagegreyScaleButton,luminositygreyScaleButton);

        controlsGridPane.add(bawHBox,0,8);
        controlsGridPane.add(restoreDefault,0,9);

        mainStage = primaryStage;
        mainStage.setTitle("PhotoEdiq");
        mainScene = new Scene(mainBorderPane,800,600);
        mainStage.setScene(mainScene);
        mainStage.show();

        mainStage.setMinWidth(600);
        mainStage.setMinHeight(450);

        mainPictureCanvas.setWidth(0.7*mainStage.getWidth());
        mainPictureCanvas.setHeight(mainStage.getHeight());

        drawFrame();

        mainStage.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                drawFrame();
            }
        });

        mainStage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                drawFrame();
            }
        });


        mainScene.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        mainScene.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    String filePath = null;
                    for (File file:db.getFiles()) {
                        filePath = file.getAbsolutePath();

                        File fileFromDropped = new File(filePath);
                        if (fileFromDropped != null) {
                            Image sourceImage = new Image(fileFromDropped.toURI().toString());
                            mainImage = new WritableImage(sourceImage.getPixelReader(),(int)sourceImage.getWidth(),(int)sourceImage.getHeight());
                            backupImage = new WritableImage(sourceImage.getPixelReader(),(int)sourceImage.getWidth(),(int)sourceImage.getHeight());
                            backupRotatedImage = new WritableImage(sourceImage.getPixelReader(),(int)sourceImage.getWidth(),(int)sourceImage.getHeight());
                            brightnessSlider.setValue(1.0);
                            drawFrame();
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });

    }

    public static double max(double... n) {
        int i = 0;
        double max = n[i];

        while (++i < n.length)
            if (n[i] > max)
                max = n[i];

        return max;
    }

    public static double min(double... n) {
        int i = 0;
        double min = n[i];

        while (++i > n.length)
            if (n[i] > min)
                min = n[i];

        return min;
    }

    public void drawFrame() {


        mainPictureCanvas.setWidth(0.7*mainStage.getWidth());
        mainPictureCanvas.setHeight(mainStage.getHeight()-39.0);

        GraphicsContext gc = mainPictureCanvas.getGraphicsContext2D();

        if(mainImage!=null) {

            gc.setFill(Color.WHITE);
            gc.fillRect(0,0,mainPictureCanvas.getWidth(),mainPictureCanvas.getHeight());

            if (mainImage.getWidth() > mainImage.getHeight()) {
                double height = mainImage.getHeight() * ((0.7 * mainStage.getWidth()) / mainImage.getWidth());
                double width = 0.7 * mainStage.getWidth();
                double posx = 0;
                double posy = 0;
                if (height > mainPictureCanvas.getHeight()) {
                    double oldHeight = height;
                    height = mainPictureCanvas.getHeight();
                    width = (height / oldHeight) * width;

                }
            if(width<(0.7*mainStage.getWidth())) {
                posx = (mainPictureCanvas.getWidth() - width)/2.0;
            }

            if(height<mainPictureCanvas.getHeight()) {
                   posy = (mainPictureCanvas.getHeight()-height)/2.0;
            }

                gc.drawImage(mainImage, posx, posy, width, height);
            }
            else {
              //  double height = mainImage.getHeight() * ((0.7 * mainStage.getWidth()) / mainImage.getWidth());
              //  double width = 0.7 * mainStage.getWidth();
                double height = mainPictureCanvas.getHeight();
                double width = mainImage.getWidth() * (mainPictureCanvas.getHeight()/mainImage.getHeight());

                double posx = 0;
                double posy = 0;
                if (height > mainPictureCanvas.getHeight()) {
                    double oldHeight = height;
                    height = mainPictureCanvas.getHeight();
                    width = (height / oldHeight) * width;

                }
                if(width<(0.7*mainStage.getWidth())) {
                    posx = (mainPictureCanvas.getWidth() - width)/2.0;
                }

                if(height<mainPictureCanvas.getHeight()) {
                    posy = (mainPictureCanvas.getHeight()-height)/2.0;
                }

                gc.drawImage(mainImage, posx, posy, width, height);
            }
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

    private Canvas mainPictureCanvas = new Canvas();

    private Scene mainScene;

    private GridPane mainGridPane;
    private GridPane controlsGridPane;

    private Slider brightnessSlider;

    private Slider redSlider;
    private Slider greenSlider;
    private Slider blueSlider;


    private WritableImage mainImage;
    private WritableImage backupImage;
    private WritableImage backupRotatedImage;
    private Stage mainStage;
}
