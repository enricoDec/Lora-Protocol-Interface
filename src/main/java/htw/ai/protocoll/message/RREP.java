package htw.ai.protocoll.message;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public class RREP extends Message {
    private byte hopCount;
    private byte originAddress;
    private byte destinationAddress;
    private byte destinationSequenceNumber;
    private byte lifetime;

    public RREP(String actualATDestination, byte hopCount, byte originAddress, byte destinationAddress, byte destinationSequenceNumber, byte lifetime) {
        super(Type.RREP, actualATDestination);
        this.hopCount = hopCount;
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.destinationSequenceNumber = destinationSequenceNumber;
        this.lifetime = lifetime;
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

    public byte getLifetime() {
        return lifetime;
    }

    public void setLifetime(byte lifetime) {
        this.lifetime = lifetime;
    }

    @Override
    public byte[] toMessage() {
        return new byte[]{getTYPE(), hopCount, originAddress, destinationAddress, destinationSequenceNumber, lifetime};
    }

    @Override
    public String toString() {
        return "RREP{" + "Hop Count: " + hopCount +
                ", Origin Address: " + originAddress +
                ", Destination Address: " + destinationAddress +
                ", Destination Sequence Number: " + destinationSequenceNumber +
                ", Lifetime: " + Byte.toUnsignedInt(lifetime) +
                '}';
    }
}