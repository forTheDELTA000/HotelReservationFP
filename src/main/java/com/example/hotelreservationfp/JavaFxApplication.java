package com.example.hotelreservationfp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;

public class JavaFxApplication extends Application {

    private static ConfigurableApplicationContext context;

    private static final String GLOBAL_CSS = "/static/admin/assets/css/style.css";

    @Override
    public void init() {
        context = new SpringApplicationBuilder(HotelReservationFpApplication.class).run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        loader.setControllerFactory(context::getBean);
        Parent root = loader.load();

        Scene scene = new Scene(root);

        applyGlobalStyles(scene);

        stage.setTitle("Hotel Admin - Login");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        context.close();
        Platform.exit();
    }

    public static void switchScene(String fxml, String title, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(JavaFxApplication.class.getResource(fxml));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root);

            applyGlobalStyles(scene);

            stage.setTitle(title);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void applyGlobalStyles(Scene scene) {
        URL cssResource = JavaFxApplication.class.getResource(GLOBAL_CSS);
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.err.println("Global CSS not found at: " + GLOBAL_CSS);
        }
    }
}