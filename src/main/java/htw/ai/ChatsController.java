package htw.ai;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import htw.ai.lora.LoraController;
import htw.ai.lora.LoraDiscovery;
import htw.ai.lora.LoraState;
import htw.ai.lora.config.Config;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 28-05-2021
 **/
public class ChatsController {
    private int pressed = 0;
    private LoraDiscovery loraDiscovery;
    private LoraController loraController;
    private BlockingQueue<String> userInputQueue;
    private BooleanProperty isRunning;
    private IntegerProperty state;
    private StringProperty newMessage;
    private LinkedList<GridPane> userMessages = new LinkedList<>();
    private int currentMessage = 0;
    private FontIcon prevCheck;
    private Config config;

    @FXML
    JFXButton btnChat;
    @FXML
    JFXButton btnGroup;
    @FXML
    JFXButton btnSettings;
    @FXML
    JFXTextField searchBar;
    @FXML
    JFXListView chatsList;
    @FXML
    Label chatName;
    @FXML
    JFXToggleButton powerToggleButton;
    @FXML
    ScrollPane chatScrollPane;
    @FXML
    JFXTextField cmdInputTextField;
    @FXML
    JFXButton btnSendCmd;
    @FXML
    VBox messageBox;


    public void initialize() {
        chatsList.setFocusTraversable(false);
        cmdInputTextField.setDisable(true);
        btnSendCmd.setDisable(true);
    }

    public void start() {
        userInputQueue = new ArrayBlockingQueue<>(20);

        // Read Config
        config = new Config();
        try {
            config.readConfig();
            writeToLog("Config read successfully.");
            writeToLog(config.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        loraDiscovery = new LoraDiscovery();
        loraController = new LoraController(config, userInputQueue, loraDiscovery);
        Thread lora_thread = new Thread(loraController, "Lora_Thread");
        lora_thread.start();

        isRunning = loraController.runningProperty();
        isRunning.addListener((observableValue, oldValue, newValue) -> Platform.runLater(() -> {
            if (newValue) {
                powerToggleButton.setText("On");
                powerToggleButton.setSelected(true);
                cmdInputTextField.setDisable(false);
                btnSendCmd.setDisable(false);
            } else {
                powerToggleButton.setText("Off");
                powerToggleButton.setSelected(false);
                cmdInputTextField.setDisable(true);
                btnSendCmd.setDisable(true);
            }
        }));

        state = loraController.stateProperty();
        state.addListener((observableValue, oldValue, newValue) -> Platform.runLater(() -> {
            if (!powerToggleButton.isSelected() || userMessages.isEmpty())
                return;
            if (newValue.intValue() == LoraState.SENDING.ordinal()) {
                FontIcon fontIcon = new FontIcon("bi-check2");
                fontIcon.setIconColor(Color.WHITE);
                fontIcon.setFont(new Font(12));
                GridPane.setValignment(fontIcon, VPos.BOTTOM);
                GridPane.setHalignment(fontIcon, HPos.LEFT);
                userMessages.get(currentMessage).add(fontIcon, 2, 0);
                prevCheck = fontIcon;
            } else if (newValue.intValue() == LoraState.SENDED.ordinal()) {
                FontIcon fontIcon = new FontIcon("bi-check2-all");
                fontIcon.setFont(new Font(12));
                fontIcon.setIconColor(Color.WHITE);
                GridPane.setValignment(fontIcon, VPos.BOTTOM);
                GridPane.setHalignment(fontIcon, HPos.LEFT);
                userMessages.get(currentMessage).getChildren().remove(prevCheck);
                userMessages.get(currentMessage).add(fontIcon, 2, 0);
                currentMessage++;
            }
        }));

        newMessage = loraController.newMessageProperty();
        newMessage.addListener((observableValue, oldValue, newValue) -> Platform.runLater(() -> {
            if (!powerToggleButton.isSelected() || newMessage.getValue().isEmpty())
                return;

            displayNotUserMessage(newValue, "not-user-message", new Font(16));
        }));
    }

    public void stop() {
        loraController.stop();
    }

    public void chatButtonClicked(MouseEvent mouseEvent) {
    }

    public void groupButtonClicked(MouseEvent mouseEvent) {
    }

    public void settingsButtonClicked(MouseEvent mouseEvent) {
    }

    public void powerToggleClicked(MouseEvent mouseEvent) {
        if (powerToggleButton.isSelected()) {
            powerToggleButton.setText("On");
            cmdInputTextField.setDisable(false);
            btnSendCmd.setDisable(false);
            powerToggleButton.setDisable(true);
            start();
        } else {
            powerToggleButton.setDisable(true);
            stop();
        }
        powerToggleButton.setDisable(false);
    }

    public void cmdTextEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER && !cmdInputTextField.getText().isEmpty()) {
            String userInput = cmdInputTextField.getText().trim();
            cmdInputTextField.setText("");
            sendCmd(userInput);
        } else if (keyEvent.getCode() == KeyCode.ENTER) {
            pressed++;
            if (pressed > 20) {
                alert("Hey Stop (ㆆ_ㆆ)", Alert.AlertType.WARNING);
                pressed = 0;
            }
        }
    }

    public void buttonSendClicked(MouseEvent mouseEvent) {
        if (!cmdInputTextField.getText().isEmpty()) {
            String userInput = cmdInputTextField.getText().trim();
            cmdInputTextField.setText("");
            sendCmd(userInput);
        } else {
            pressed++;
            if (pressed > 20) {
                alert("Hey Stop (ㆆ_ㆆ)", Alert.AlertType.WARNING);
                pressed = 0;
            }
        }
    }

    public void alert(String message, Alert.AlertType alertType) {
        Alert a = new Alert(alertType);
        a.setContentText(message);
        a.show();
    }

    public void sendCmd(String cmd) {
        try {
            userInputQueue.put(cmd);

            displayUserMessage(cmd, "user-message", new Font(16));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void displayUserMessage(String cmd, String cssClass, Font font) {
        // Display message
        Label message = new Label(cmd);
        message.setAlignment(Pos.CENTER);
        message.getStyleClass().add(cssClass);
        message.setWrapText(true);
        message.setFont(font);

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER_RIGHT);

        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getRowConstraints().add(new RowConstraints());
        gridPane.getColumnConstraints().get(0).setPercentWidth(0);
        gridPane.getColumnConstraints().get(1).setPercentWidth(0);
        gridPane.getColumnConstraints().get(2).setPercentWidth(0);
        gridPane.add(message, 1, 0);
        GridPane.setHalignment(message, HPos.RIGHT);
        gridPane.setPadding(new Insets(4, 6, 4, 4));

        Label user = new Label(String.valueOf(config.getAddress()));
        user.setFont(new Font(10));
        user.setTextFill(Color.color(1,1,1));
        GridPane.setHalignment(user, HPos.RIGHT);
        gridPane.add(user, 0, 0);

        userMessages.add(gridPane);
        messageBox.getChildren().add(gridPane);
    }

    public void displayNotUserMessage(String cmd, String cssClass, Font font) {
        // Display message
        Label message = new Label(cmd);
        message.setAlignment(Pos.CENTER);
        message.getStyleClass().add(cssClass);
        message.setWrapText(true);
        message.setFont(font);

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER_LEFT);

        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getRowConstraints().add(new RowConstraints());
        gridPane.getColumnConstraints().get(0).setPercentWidth(0);
        gridPane.getColumnConstraints().get(1).setPercentWidth(0);
        gridPane.getColumnConstraints().get(2).setPercentWidth(0);
        gridPane.add(message, 1, 0);
        GridPane.setHalignment(message, HPos.LEFT);
        gridPane.setPadding(new Insets(4, 4, 4, 6));
        messageBox.getChildren().add(gridPane);

        FontIcon fontIcon = new FontIcon("bi-check2-all");
        fontIcon.setFont(new Font(12));
        fontIcon.setIconColor(Color.WHITE);
        GridPane.setValignment(fontIcon, VPos.BOTTOM);
        GridPane.setHalignment(fontIcon, HPos.LEFT);
        gridPane.add(fontIcon, 0, 0);
    }

    public static void writeToLog(String message, Color color) {
        System.out.println(message);
    }

    public static void writeToLog(String message) {
        System.out.println(message);
    }
}
