package htw.ai.protocoll.message;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public class RREP_ACK extends Message {
    public RREP_ACK(byte type) {
        super((byte) 4);
    }

    @Override
    public byte[] toMessage() {
        return null;
    }
}
