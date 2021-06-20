package htw.ai.protocoll.message;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public class SEND_TEXT_REQUEST_ACK extends Message {
    private byte originAddress;
    private byte destinationAddress;
    private byte messageSequenceNumber;

    public SEND_TEXT_REQUEST_ACK(String actualATDestination, byte originAddress, byte destinationAddress, byte messageSequenceNumber) {
        super(Type.SEND_TEXT_REQUEST_ACK, actualATDestination);
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.messageSequenceNumber = messageSequenceNumber;
    }

    public byte getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(byte originAddress) {
        this.originAddress = originAddress;
    }

    public byte getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(byte destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public byte getMessageSequenceNumber() {
        return messageSequenceNumber;
    }

    public void setMessageSequenceNumber(byte messageSequenceNumber) {
        this.messageSequenceNumber = messageSequenceNumber;
    }

    @Override
    public byte[] toMessage() {
        return new byte[]{getTYPE(), originAddress, destinationAddress, messageSequenceNumber};
    }
}
