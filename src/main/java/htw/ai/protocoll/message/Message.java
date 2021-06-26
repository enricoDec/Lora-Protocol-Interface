package htw.ai.protocoll.message;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public abstract class Message {
    private final byte type;
    private byte prevHop;
    private String destination;

    /**
     * A Message wraps a type of AODV message
     *
     * @param type type of AODV Message (1-7)
     */
    public Message(byte type, String destination) {
        this.type = type;
        this.destination = destination;
    }

    /**
     * Get the type of the message
     *
     * @return type
     */
    public byte getTYPE() {
        return type;
    }

    /**
     * Get message as bytes in the right order to be send
     *
     * @return array of bytes following protocol
     */
    public abstract byte[] toMessage();

    /**
     * Prev Hop for Messages received is the origin of the message (LR,origin,...)
     *
     * @return previous hop
     */
    public byte getPrevHop() {
        return prevHop;
    }

    /**
     * Set the previous hop
     *
     * @param prevHop previous hop
     */
    public void setPrevHop(byte prevHop) {
        this.prevHop = prevHop;
    }

    /**
     * Actual destination of the message (not always same as message destination since some may be broadcast)
     * @return destination
     */
    public String getDestination() {
        return destination;
    }
}
