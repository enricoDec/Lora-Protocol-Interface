package htw.ai.application.model;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 08-06-2021
 **/
public class UserMessage extends ClientMessage {
    /**
     * Represents a received message
     *
     * @param data               data to send
     * @param sourceAddress      source address of message
     * @param destinationAddress destination address of message
     */
    public UserMessage(String data, int sourceAddress, int destinationAddress) {
        super(data, sourceAddress, destinationAddress);
    }
}
