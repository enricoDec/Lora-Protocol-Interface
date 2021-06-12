package htw.ai.application.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import htw.ai.App;
import htw.ai.application.model.Chats;
import htw.ai.application.model.ChatsDiscovery;
import htw.ai.application.model.ClientMessage;
import htw.ai.application.model.UserMessage;
import htw.ai.lora.config.Config;
import htw.ai.protocoll.AodvController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
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
    private ChatsDiscovery chatsDiscovery;
    private Chats chats;
    private IntegerProperty newClient;
    private ObjectProperty<ClientMessage> newMessage;
    private BlockingQueue<UserMessage> userInputQueue;
    private BooleanProperty isRunning;
    private LinkedList<GridPane> userMessages = new LinkedList<>();
    public static Config CONFIG = new Config();
    private int currentChat = -1;
    private AodvController aodvController;
    private Thread aodv_thread;

    @FXML
    JFXButton btnChat;
    @FXML
    JFXButton btnGroup;
    @FXML
    JFXButton btnSettings;
    @FXML
    JFXTextField searchBar;
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
    @FXML
    VBox chatList;
    @FXML
    ComboBox<Integer> destinationCombo;

    @FXML
    public void initialize() {
        cmdInputTextField.setDisable(true);
        btnSendCmd.setDisable(true);
        try {
            CONFIG.readConfig();
            CONFIG.saveConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        userInputQueue = new ArrayBlockingQueue<>(20);

        // Read Config
        try {
            CONFIG.readConfig();
            writeToLog("Config read successfully.");
            writeToLog(CONFIG.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize all Threads and start them
        // Main Thread -> AodvController -> LoraController -> UartController
        chats = new Chats();
        chatsDiscovery = new ChatsDiscovery(chats);
        aodvController = new AodvController(userInputQueue, CONFIG, chatsDiscovery);
        aodv_thread = new Thread(aodvController, "AODV_Thread");
        boolean portOpen = aodvController.initialize();
        if (!portOpen) {
            powerToggleButton.setText("Off");
            powerToggleButton.setSelected(false);
            cmdInputTextField.setDisable(true);
            btnSendCmd.setDisable(true);
            alert("Could not open port " + CONFIG.getPort(), Alert.AlertType.ERROR);
            return;
        }

        aodv_thread.start();

        // Create Listeners for properties

        // TODO: 12.06.2021 Fix this
        // Message sent property
//        state = loraController.stateProperty();
//        state.addListener((observableValue, oldValue, newValue) -> Platform.runLater(() -> {
//            if (!powerToggleButton.isSelected() || userMessages.isEmpty())
//                return;
//            if (newValue.intValue() == LoraState.SENDING.ordinal()) {
//                FontIcon fontIcon = new FontIcon("bi-check2");
//                fontIcon.setIconColor(Color.WHITE);
//                fontIcon.setFont(new Font(12));
//                GridPane.setValignment(fontIcon, VPos.BOTTOM);
//                GridPane.setHalignment(fontIcon, HPos.LEFT);
//                userMessages.get(currentMessage).add(fontIcon, 2, 0);
//                prevCheck = fontIcon;
//            } else if (newValue.intValue() == LoraState.SENDED.ordinal()) {
//                FontIcon fontIcon = new FontIcon("bi-check2-all");
//                fontIcon.setFont(new Font(12));
//                fontIcon.setIconColor(Color.WHITE);
//                GridPane.setValignment(fontIcon, VPos.BOTTOM);
//                GridPane.setHalignment(fontIcon, HPos.LEFT);
//                userMessages.get(currentMessage).getChildren().remove(prevCheck);
//                userMessages.get(currentMessage).add(fontIcon, 2, 0);
//                currentMessage++;
//            }
//        }));

        // New Message property
        newMessage = chats.newMessageProperty();
        newMessage.addListener((observableValue, oldValue, newValue) -> Platform.runLater(() -> {
            if (!powerToggleButton.isSelected())
                return;

            if (newValue instanceof UserMessage && newValue.getDestinationAddress() == currentChat)
                displayUserMessage(newValue.getData());
            else if (newValue.getSourceAddress() == currentChat)
                displayNotUserMessage(newValue.getData());
        }));

        // Make new Chats
        newClient = chatsDiscovery.newClientProperty();
        newClient.addListener((observableValue, oldValue, newValue) -> Platform.runLater(() -> {
            HBox clientBox = new HBox();
            clientBox.getStyleClass().add("client");
            clientBox.setPrefHeight(40);
            chatList.getChildren().add(clientBox);

            Label client = new Label(newValue.toString());
            client.setMinWidth(28);
            client.setTextFill(Color.WHITE);
            client.setPadding(new Insets(5));
            clientBox.getChildren().add(client);

            Label clientMessage = new Label("Client " + newValue + " discovered.");
            clientMessage.setAlignment(Pos.TOP_LEFT);
            clientMessage.setWrapText(true);
            clientMessage.setTextFill(Color.WHITE);
            clientBox.getChildren().add(clientMessage);

            loadChat(newValue.intValue());

            clientBox.setOnMouseReleased(e -> {
                if (currentChat != newValue.intValue())
                    loadChat(newValue.intValue());
            });
        }));

        // Destination Combo Box
        // TODO: 12.06.2021 Don't allow to set destination to yourself
        for (int i = 1; i < 21; i++) {
            destinationCombo.getItems().add(i);
        }
    }

    public void loadChat(int id) {
        currentChat = id;
        messageBox.getChildren().clear();
        chatName.setText("Chat " + id);

        chats.getClientMessages(id).forEach((message) -> {
            if (message instanceof UserMessage) {
                displayUserMessage(message.getData());
            } else {
                displayNotUserMessage(message.getData());
            }
        });
        destinationCombo.getSelectionModel().select(id - 1);
    }

    public void stop() {
        if (aodvController != null && aodvController.getIsRunning().get()) {
            aodvController.stop();
            try {
                aodv_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            writeToLog("AODV Controller Thread ended");
            currentChat = -1;
            messageBox.getChildren().clear();
            chatList.getChildren().clear();
        }
    }

    public void chatButtonClicked(MouseEvent mouseEvent) {
    }

    public void groupButtonClicked(MouseEvent mouseEvent) {
    }

    @FXML
    public void settingsButtonClicked(MouseEvent mouseEvent) throws IOException {
        stop();
        App.setRoot("view/uartSettings");
    }

    public void powerToggleClicked(MouseEvent mouseEvent) {
        if (powerToggleButton.isSelected()) {
            powerToggleButton.setText("On");
            cmdInputTextField.setDisable(false);
            btnSendCmd.setDisable(false);
            powerToggleButton.setDisable(true);
            start();
        } else {
            powerToggleButton.setText("Off");
            cmdInputTextField.setDisable(true);
            btnSendCmd.setDisable(true);
            powerToggleButton.setDisable(true);
            stop();
        }
        powerToggleButton.setDisable(false);
    }

    public void cmdTextEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER && !cmdInputTextField.getText().isEmpty()) {
            String userInput = cmdInputTextField.getText().trim();
            if (userInput.length() > 30){
                alert("Message longer then 30 characters!", Alert.AlertType.INFORMATION);
                return;
            }
            cmdInputTextField.setText("");
            sendData(userInput);
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
            if (userInput.length() > 30){
                alert("Message longer then 30 characters!", Alert.AlertType.INFORMATION);
                return;
            }
            cmdInputTextField.setText("");
            sendData(userInput);
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

    public void sendData(String data) {
        try {
            if (destinationCombo.getSelectionModel().isEmpty()) {
                alert("Please select a destination", Alert.AlertType.INFORMATION);
                return;
            }

            int destination = destinationCombo.selectionModelProperty().get().getSelectedItem();
            UserMessage userMessage = new UserMessage(data, CONFIG.getAddress(), destination);
            chatsDiscovery.newClient(userMessage);
            userInputQueue.put(userMessage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void displayUserMessage(String cmd) {
        // Display message
        Label message = new Label(cmd);
        message.setAlignment(Pos.CENTER);
        message.getStyleClass().add("user-message");
        message.setWrapText(true);
        message.setFont(new Font(16));

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

        Label user = new Label(String.valueOf(CONFIG.getAddress()));
        user.setFont(new Font(10));
        user.setTextFill(Color.color(1, 1, 1));

        GridPane.setHalignment(user, HPos.RIGHT);
        gridPane.add(user, 0, 0);

        userMessages.add(gridPane);
        messageBox.getChildren().add(gridPane);

        slowScrollToBottom(chatScrollPane);
    }

    public void displayNotUserMessage(String cmd) {
        // Display message
        Label message = new Label(cmd);
        message.setAlignment(Pos.CENTER);
        message.getStyleClass().add("not-user-message");
        message.setWrapText(true);
        message.setFont(new Font(16));

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

        slowScrollToBottom(chatScrollPane);
    }

    public static void writeToLog(String message, Color color) {
        if (color == Color.DARKRED)
            System.out.print("\033[1;31m");
        else if (color == Color.CYAN)
            System.out.print("\033[1;36m");
        else if (color == Color.YELLOW)
            System.out.print("\033[1;33m");
        System.out.println(message);
        System.out.print("\033[0m");
    }

    public static void writeToLog(String message) {
        System.out.println(message);
    }

    public void slowScrollToBottom(ScrollPane scrollPane) {
        Animation animation = new Timeline(
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(scrollPane.vvalueProperty(), 1)));
        animation.play();
    }
}
