package htw.ai;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 28-05-2021
 **/
public class ChatsController {
    private int pressed = 0;
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

    public void chatButtonClicked(MouseEvent mouseEvent) {
    }

    public void groupButtonClicked(MouseEvent mouseEvent) {
    }

    public void settingsButtonClicked(MouseEvent mouseEvent) {
    }

    public void powerToggleClicked(MouseEvent mouseEvent) {
        if (powerToggleButton.isSelected()) {
            powerToggleButton.setText("On");
            // Start stuff
        } else {
            powerToggleButton.setText("Off");
            // Stop stuff
        }
    }

    public void cmdTextEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER && !cmdInputTextField.getText().isEmpty()) {
            String userInput = cmdInputTextField.getText();
            cmdInputTextField.setText("");
            sendCmd(userInput);
        } else {
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
        // TODO: Pass to controller
    }
}
