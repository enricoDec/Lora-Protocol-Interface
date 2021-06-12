package htw.ai.protocoll.message;

import java.nio.charset.StandardCharsets;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public class SEND_TEXT_REQUEST extends Message {
    private byte originAddress;
    private byte destinationAddress;
    private byte messageSequenceNumber;
    private String payload;

    public SEND_TEXT_REQUEST(byte originAddress, byte destinationAddress, byte messageSequenceNumber, String payload) {
        super(Type.SEND_TEXT_REQUEST);
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

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public byte[] toMessage() {
        byte[] header = {getTYPE(), originAddress, destinationAddress, messageSequenceNumber};
        byte[] data = payload.getBytes(StandardCharsets.US_ASCII);
        byte[] buffer = new byte[header.length + data.length];
        System.arraycopy(header, 0, buffer, 0, header.length);
        System.arraycopy(data, 0, buffer, header.length, data.length);
        return buffer;
    }
}
