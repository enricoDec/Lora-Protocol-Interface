package htw.ai.protocoll.message;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public class RREQ extends Message {
    private byte uFlag;
    private byte hopCount;
    private byte originAddress;
    private byte originSequenceNumber;
    private byte destinationAddress;
    private byte destinationSequenceNumber;

    public RREQ(byte type, byte uFlag, byte hopCount, byte originAddress, byte originSequenceNumber, byte destinationAddress, byte destinationSequenceNumber) {
        super((byte) 5);
        this.uFlag = uFlag;
        this.hopCount = hopCount;
        this.originAddress = originAddress;
        this.originSequenceNumber = originSequenceNumber;
        this.destinationAddress = destinationAddress;
        this.destinationSequenceNumber = destinationSequenceNumber;
    }

    public byte getuFlag() {
        return uFlag;
    }

    public void setuFlag(byte uFlag) {
        this.uFlag = uFlag;
    }

    public byte getHopCount() {
        return hopCount;
    }

    public void setHopCount(byte hopCount) {
        this.hopCount = hopCount;
    }

    public byte getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(byte originAddress) {
        this.originAddress = originAddress;
    }

    public byte getOriginSequenceNumber() {
        return originSequenceNumber;
    }

    public void setOriginSequenceNumber(byte originSequenceNumber) {
        this.originSequenceNumber = originSequenceNumber;
    }

    public byte getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(byte destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public byte getDestinationSequenceNumber() {
        return destinationSequenceNumber;
    }

    public void setDestinationSequenceNumber(byte destinationSequenceNumber) {
        this.destinationSequenceNumber = destinationSequenceNumber;
    }

    @Override
    public byte[] toMessage() {
        return null;
    }
}
