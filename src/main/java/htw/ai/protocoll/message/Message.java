package htw.ai.protocoll.message;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public abstract class Message {
    private final byte TYPE;

    public Message(byte type) {
        this.TYPE = type;
    }

    public byte getTYPE() {
        return TYPE;
    }

    public abstract byte[] toMessage();
}
