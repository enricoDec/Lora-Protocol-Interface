package htw.ai.lora;


/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol
 * @version : 1.0
 * @since : 14-05-2021
 **/
public enum Lora {

    // Lora Constant codes
    AT("AT"),
    LR("LR"),
    EOF("\r\n"),
    DIVIDER(","),
    UNKNOWN("UNKNOWN"),

    // Lora command codes
    AT_TEST("AT"),
    AT_RST("AT+RST"),
    AT_VER("AT+VER"),
    AT_IDLE("AT+IDLE"),
    AT_SLEEP("AT+SLEEP=1"),
    AT_RX("AT+RX"),
    AT_RSSI_GET("AT+RSSI?"),
    AT_ADDR_SET("AT+ADDR="),
    AT_ADDR_GET("AT+ADDR?"),
    AT_DEST("AT+DEST="),
    AT_DEST_GET("AT+DEST?"),
    AT_ADDREN("AT+ADDREN=1"),
    AT_ADDREN_GET("AT+ADDREN?"),
    AT_CFG("AT+CFG="),
    AT_SAVE("AT+SAVE"),
    AT_SEND("AT+SEND="),

    // Lora reply codes
    REPLY_OK("AT,OK"),
    REPLY_SENDING("AT,SENDING"),
    REPLY_SENDED("AT,SENDED"),

    // Lora error codes
    ERR_GENERAL("AT,ERR:"),
    ERR_CMD("AT,ERR:CMD"),
    ERR_CPU_BUSY("AT,ERR:CPU_BUSY"),
    ERR_RF_BUSY("AT,ERR:RF_BUSY"),
    ERR_SYMBLE("AT,ERR:SYMBLE"),
    ERR_PARA("AT,ERR:PARA");

    /**
     * String representation of lora codes
     */
    private final String CODE;

    Lora(String CODE) {
        this.CODE = CODE;
    }

    public String getCODE() {
        return CODE;
    }
}
