package htw.ai.protocoll.message;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public class RERR extends Message {
    private byte destinationCount;
    private byte unreachableDestinationAddress;
    private byte unreachableDestinationSequenceNumber;
    private byte additionalAddresses;
    private byte additionalSequenceNumber;

    public RERR(String actualATDestination ,byte destinationCount, byte unreachableDestinationAddress, byte unreachableDestinationSequenceNumber, byte additionalAddresses, byte additionalSequenceNumber) {
        super(Type.RERR, actualATDestination);
        this.destinationCount = destinationCount;
        this.unreachableDestinationAddress = unreachableDestinationAddress;
        this.unreachableDestinationSequenceNumber = unreachableDestinationSequenceNumber;
        this.additionalAddresses = additionalAddresses;
        this.additionalSequenceNumber = additionalSequenceNumber;
    }

    public byte getDestinationCount() {
        return destinationCount;
    }

    public void setDestinationCount(byte destinationCount) {
        this.destinationCount = destinationCount;
    }

    public byte getUnreachableDestinationAddress() {
        return unreachableDestinationAddress;
    }

    public void setUnreachableDestinationAddress(byte unreachableDestinationAddress) {
        this.unreachableDestinationAddress = unreachableDestinationAddress;
    }

    public byte getUnreachableDestinationSequenceNumber() {
        return unreachableDestinationSequenceNumber;
    }

    public void setUnreachableDestinationSequenceNumber(byte unreachableDestinationSequenceNumber) {
        this.unreachableDestinationSequenceNumber = unreachableDestinationSequenceNumber;
    }

    public byte getAdditionalAddresses() {
        return additionalAddresses;
    }

    public void setAdditionalAddresses(byte additionalAddresses) {
        this.additionalAddresses = additionalAddresses;
    }

    public byte getAdditionalSequenceNumber() {
        return additionalSequenceNumber;
    }

    public void setAdditionalSequenceNumber(byte additionalSequenceNumber) {
        this.additionalSequenceNumber = additionalSequenceNumber;
    }

    @Override
    public byte[] toMessage() {
        return new byte[]{getTYPE(), destinationCount, unreachableDestinationAddress, unreachableDestinationSequenceNumber, additionalAddresses, additionalSequenceNumber};
    }
}
