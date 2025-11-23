package com.example.hotelreservationfp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {

    private static ConfigurableApplicationContext context;

    @Override
    public void init() {
        // Start Spring Boot
        context = new SpringApplicationBuilder(HotelReservationFpApplication.class).run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Load the Login Screen first
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        loader.setControllerFactory(context::getBean); // Allow @Autowired in Controllers
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle("Hotel Admin - Login");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        context.close();
        Platform.exit();
    }

    // Helper to switch scenes globally
    public static void switchScene(String fxml, String title, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(JavaFxApplication.class.getResource(fxml));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}