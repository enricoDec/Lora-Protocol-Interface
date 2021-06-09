package htw.ai.controller;

import com.jfoenix.controls.JFXTextField;
import htw.ai.App;
import htw.ai.lora.config.Config;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 01-06-2021
 **/
public class LoraSettingsController {
    private Config config = ChatsController.CONFIG;

    @FXML
    public JFXTextField carrierFrequencyInput;
    @FXML
    public JFXTextField powerInput;
    @FXML
    public JFXTextField modulationBandwidthInput;
    @FXML
    public JFXTextField spreadingFactorInput;
    @FXML
    public JFXTextField errorCodingInput;
    @FXML
    public ComboBox<Boolean> crcInput;
    @FXML
    public ComboBox<Boolean> implicitHeaderInput;
    @FXML
    public ComboBox<Boolean> rxSingleInput;
    @FXML
    public ComboBox<Boolean> frequencyHopInput;
    @FXML
    public JFXTextField hopPeriodInput;
    @FXML
    public JFXTextField packetTimeoutInput;
    @FXML
    public JFXTextField payloadLengthInput;
    @FXML
    public JFXTextField preambleLengthInput;

    @FXML
    public void initialize() {
        carrierFrequencyInput.setText(String.valueOf(config.getCarrierFrequency()));
        powerInput.setText(String.valueOf(config.getPower()));
        modulationBandwidthInput.setText(String.valueOf(config.getModulationBandwidth()));
        spreadingFactorInput.setText(String.valueOf(config.getSpreadingFactor()));
        errorCodingInput.setText(String.valueOf(config.getErrorCoding()));
        crcInput.getItems().add(false);
        crcInput.getItems().add(true);
        crcInput.getSelectionModel().select(config.getCrc());
        implicitHeaderInput.getItems().add(false);
        implicitHeaderInput.getItems().add(true);
        implicitHeaderInput.getSelectionModel().select(config.getImplicitHeaderOn());
        rxSingleInput.getItems().add(false);
        rxSingleInput.getItems().add(true);
        rxSingleInput.getSelectionModel().select(config.getRxSingleOn());
        frequencyHopInput.getItems().add(false);
        frequencyHopInput.getItems().add(true);
        frequencyHopInput.getSelectionModel().select(config.getFrequencyHopOn());
        hopPeriodInput.setText(String.valueOf(config.getHopPeriod()));
        packetTimeoutInput.setText(String.valueOf(config.getRxPacketTimeout()));
        payloadLengthInput.setText(String.valueOf(config.getPayloadLength()));
        preambleLengthInput.setText(String.valueOf(config.getPreambleLength()));
    }

    public void back() throws IOException {
        App.setRoot("uartSettings");
    }

    public void save(MouseEvent mouseEvent) {
        int carrierFrequency;
        int power;
        int modulationBandwidth;
        int spreadingFactor;
        int errorCoding;
        int hopPeriod;
        int packetTimeout;
        int payloadLength;
        int preambleLength;

        try {
            carrierFrequency = Integer.parseInt(carrierFrequencyInput.getText().strip());
            if (carrierFrequency < 410000000 || carrierFrequency > 470000000)
                showAlert("Carrier frequency not in valid range", Alert.AlertType.WARNING);

            power = Integer.parseInt(powerInput.getText().strip());
            if (power < 5 || power > 20)
                showAlert("Power not in valid range", Alert.AlertType.WARNING);

            modulationBandwidth = Integer.parseInt(modulationBandwidthInput.getText().strip());
            if (modulationBandwidth < 0 || modulationBandwidth > 9)
                showAlert("Modulation Bandwidth not in valid range", Alert.AlertType.WARNING);

            spreadingFactor = Integer.parseInt(spreadingFactorInput.getText().strip());
            if (spreadingFactor < 6 || spreadingFactor > 12)
                showAlert("Spreading Factor not in valid range", Alert.AlertType.WARNING);

            errorCoding = Integer.parseInt(errorCodingInput.getText().strip());
            if (errorCoding < 1 || errorCoding > 4)
                showAlert("Error Coding not in valid range", Alert.AlertType.WARNING);

            hopPeriod = Integer.parseInt(hopPeriodInput.getText().strip());
            if (hopPeriod < 0 || hopPeriod > 4)
                showAlert("I don't know what this is. It's not documented", Alert.AlertType.WARNING);

            packetTimeout = Integer.parseInt(packetTimeoutInput.getText().strip());
            if (packetTimeout < 1 || packetTimeout > 65535)
                showAlert("Packet timeout not in valid range", Alert.AlertType.WARNING);

            payloadLength = Integer.parseInt(payloadLengthInput.getText().strip());
            if (payloadLength < 5 || payloadLength > 255)
                showAlert("Payload length not in valid range", Alert.AlertType.WARNING);

            preambleLength = Integer.parseInt(preambleLengthInput.getText().strip());
            if (preambleLength < 4 || preambleLength > 65535)
                showAlert("Preamble length not in valid range", Alert.AlertType.WARNING);

        } catch (NumberFormatException e) {
            showAlert("Could not parse, check input.", Alert.AlertType.WARNING);
            return;
        }

        config.setCarrierFrequency(carrierFrequency);
        config.setPower(power);
        config.setModulationBandwidth(modulationBandwidth);
        config.setSpreadingFactor(spreadingFactor);
        config.setErrorCoding(errorCoding);
        config.setCrc(crcInput.getSelectionModel().getSelectedIndex());
        config.setImplicitHeaderOn(implicitHeaderInput.getSelectionModel().getSelectedIndex());
        config.setRxSingleOn(rxSingleInput.getSelectionModel().getSelectedIndex());
        config.setFrequencyHopOn(frequencyHopInput.getSelectionModel().getSelectedIndex());
        config.setHopPeriod(hopPeriod);
        config.setRxPacketTimeout(packetTimeout);
        config.setPayloadLength(payloadLength);
        config.setPreambleLength(preambleLength);

        try {
            config.saveConfig();
            showAlert("Lora Setting successfully saved.", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.show();
    }
}
