package htw.ai.protocoll.message;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 12-06-2021
 **/
public class Type {
    public static final byte CUSTOM = 0;
    public static final byte RREQ = 1;
    public static final byte RREP = 2;
    public static final byte RERR = 3;
    public static final byte RREP_ACK = 4;
    public static final byte SEND_TEXT_REQUEST = 5;
    public static final byte SEND_HOP_ACK = 6;
    public static final byte SEND_TEXT_REQUEST_ACK = 7;
}
