package htw.ai;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import htw.ai.lora.LoraController;
import htw.ai.lora.LoraDiscovery;
import htw.ai.lora.config.Config;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.io.IOException;
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


    public void initialize() {
        chatsList.setFocusTraversable(false);
    }

    public void start(){
        userInputQueue = new ArrayBlockingQueue<>(20);

        // Read Config
        Config config = new Config();
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
            start();
        } else {
            powerToggleButton.setText("Off");
            stop();
        }
    }

    public void cmdTextEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER && !cmdInputTextField.getText().isEmpty()) {
            String userInput = cmdInputTextField.getText();
            cmdInputTextField.setText("");
            sendCmd(userInput);
        } else if(keyEvent.getCode() == KeyCode.ENTER) {
            pressed++;
            if (pressed > 20) {
                alert("Hey Stop ಠ_ಠ", Alert.AlertType.WARNING);
                pressed = 0;
            }
        }
    }

    public void buttonSendClicked(MouseEvent mouseEvent) {
        if (!cmdInputTextField.getText().isEmpty()) {
            String userInput = cmdInputTextField.getText();
            cmdInputTextField.setText("");
            // TODO: Validate Input length etc
            sendCmd(userInput);
        } else {
            pressed++;
            if (pressed > 20) {
                alert("Hey Stop ಠ_ಠ", Alert.AlertType.WARNING);
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
            //TODO: If queue full main thread will wait, not sure how to avoid
            userInputQueue.put(cmd);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void writeToLog(String message, Color color) {
        System.out.println(message);
    }

    public static void writeToLog(String message) {
        System.out.println(message);
    }
}
