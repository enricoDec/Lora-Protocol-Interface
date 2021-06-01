package htw.ai.lora.config;

import com.fazecast.jSerialComm.SerialPort;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 01-06-2021
 **/
public enum StopBits {
    ONE_STOP_BIT(SerialPort.ONE_STOP_BIT),
    ONE_POINT_FIVE_STOP_BITS(SerialPort.ONE_POINT_FIVE_STOP_BITS),
    TWO_STOP_BITS(SerialPort.TWO_STOP_BITS);

    public final int CODE;

    StopBits(int CODE) {
        this.CODE = CODE;
    }

    /**
     * Do not use ordinal() to obtain the numeric representation of StopBits. Use getValue() instead.
     *
     * @return ordinal
     */
    public int getValue() {
        return ordinal() + 1;
    }
}
