package htw.ai.lora.config;

import com.fazecast.jSerialComm.SerialPort;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 01-06-2021
 **/
public enum Flowcontrol {
    FLOW_CONTROL_DISABLED(SerialPort.FLOW_CONTROL_DISABLED),
    FLOW_CONTROL_RTS_ENABLED(SerialPort.FLOW_CONTROL_RTS_ENABLED),
    FLOW_CONTROL_CTS_ENABLED(SerialPort.FLOW_CONTROL_CTS_ENABLED),
    FLOW_CONTROL_DSR_ENABLED(SerialPort.FLOW_CONTROL_DSR_ENABLED),
    FLOW_CONTROL_DTR_ENABLED(SerialPort.FLOW_CONTROL_DTR_ENABLED),
    FLOW_CONTROL_XONXOFF_IN(SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED),
    FLOW_CONTROL_XONXOFF_OUT(SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED);

    public final int CODE;

    Flowcontrol(int CODE) {
        this.CODE = CODE;
    }
}
