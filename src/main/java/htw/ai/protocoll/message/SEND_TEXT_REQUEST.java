package htw.ai.protocoll.message;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public class SEND_TEXT_REQUEST extends Message{
    private byte originAddress;
    private byte destinationAddress;
    private byte messageSequenceNumber;
    private byte[] payload;

    public SEND_TEXT_REQUEST(byte type, byte originAddress, byte destinationAddress, byte messageSequenceNumber, byte[] payload) {
        super((byte) 5);
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.messageSequenceNumber = messageSequenceNumber;
        this.payload = payload;
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

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
