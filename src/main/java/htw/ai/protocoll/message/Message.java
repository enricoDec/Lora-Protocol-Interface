package htw.ai.protocoll.message;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public abstract class Message {
    private byte type;
    private byte prevHop;

    public Message(byte type) {
        this.type = type;
    }

    public byte getTYPE() {
        return type;
    }

    public abstract byte[] toMessage();

    public byte getPrevHop() {
        return prevHop;
    }

    public void setPrevHop(byte prevHop) {
        this.prevHop = prevHop;
    }
}
