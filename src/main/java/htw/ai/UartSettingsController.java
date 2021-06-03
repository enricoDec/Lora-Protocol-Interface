package htw.ai;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import htw.ai.lora.config.Config;
import htw.ai.lora.config.Flowcontrol;
import htw.ai.lora.config.Parity;
import htw.ai.lora.config.StopBits;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 01-06-2021
 **/
public class UartSettingsController {
    private Config config = ChatsController.CONFIG;

    @FXML
    public JFXButton buttonBack;
    @FXML
    public JFXButton buttonForward;
    @FXML
    public JFXTextField baudrateInput;
    @FXML
    public ComboBox<Parity> parityInput;
    @FXML
    public ComboBox<Flowcontrol> flowcontrolInput;
    @FXML
    public ComboBox<StopBits> numberOfStopBits;
    @FXML
    public JFXTextField numberOfDataBits;
    @FXML
    public JFXTextField portInput;
    @FXML
    public JFXButton saveUART;

    @FXML
    public void initialize() {
        baudrateInput.setText(String.valueOf(config.getBaudRate()));
        // Parity
        Arrays.stream(Parity.values()).forEach(
                (parity) -> parityInput.getItems().add(parity)
        );
        // Flow Control
        parityInput.getSelectionModel().select(config.getParity());
        Arrays.stream(Flowcontrol.values()).forEach(
                (flowcontrol -> flowcontrolInput.getItems().add(flowcontrol))
        );
        flowcontrolInput.getSelectionModel().select(config.getParity());
        // Stop Bits
        Arrays.stream(StopBits.values()).forEach(
                (stopBits -> numberOfStopBits.getItems().add(stopBits)));
        numberOfStopBits.getSelectionModel().select(config.getParity());
        numberOfDataBits.setText(String.valueOf(config.getNumberOfDataBits()));
        portInput.setText(config.getPort());
    }

    public void back() throws IOException {
        App.setRoot("chats");
    }

    public void forward(MouseEvent mouseEvent) throws IOException {
        App.setRoot("loraSettings");
    }

    public void save(MouseEvent mouseEvent) {
        int baudrate;
        int dataBits;
        try {
            baudrate = Integer.parseInt(baudrateInput.getText().strip());
            dataBits = Integer.parseInt(numberOfDataBits.getText().strip());
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Baudrate is not a number.");
            alert.show();
            return;
        }
        config.setBaudRate(baudrate);
        config.setParity(parityInput.getSelectionModel().getSelectedItem().ordinal());
        config.setFlowControl(flowcontrolInput.getSelectionModel().getSelectedItem().ordinal());
        config.setNumberOfStopBits(numberOfStopBits.getSelectionModel().getSelectedItem().getValue());
        config.setNumberOfDataBits(dataBits);
        config.setPort(portInput.getText());

        try {
            config.saveConfig();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Lora Setting successfully saved.");
            alert.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
