package htw.ai.protocoll.message;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public class RREP_ACK extends Message {
    public RREP_ACK() {
        super(Type.RREP_ACK);
    }

    @Override
    public byte[] toMessage() {
        return new byte[]{getTYPE()};
    }
}
