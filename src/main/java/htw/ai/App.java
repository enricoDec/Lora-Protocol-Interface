package htw.ai;
/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol-Interface
 * @version : 1.0
 * @since : 26-05-2021
 **/

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Scene scene;
    private String base = "view";

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML(base + "/chats"));
        scene.getStylesheets().add(getClass().getResource(base + "/css/chats.css").toExternalForm());
        stage.setScene(scene);
        stage.setMinWidth(640);
        stage.setMinHeight(360);
        stage.setTitle("Lora Interface");
        Image icon = new Image(getClass().getResource("icon.png").toExternalForm());
        stage.getIcons().add(icon);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
