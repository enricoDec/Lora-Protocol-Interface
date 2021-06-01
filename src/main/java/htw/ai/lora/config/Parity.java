package htw.ai.lora.config;

import com.fazecast.jSerialComm.SerialPort;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 01-06-2021
 **/
public enum Parity {
    NO_PARITY(SerialPort.NO_PARITY),
    ODD_PARITY(SerialPort.ODD_PARITY),
    EVEN_PARITY(SerialPort.EVEN_PARITY),
    MARK_PARITY(SerialPort.MARK_PARITY),
    SPACE_PARITY(SerialPort.SPACE_PARITY);

    public final int CODE;

    Parity(int CODE) {
        this.CODE = CODE;
    }
}
