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
    private byte rreqID;

    /**
     * Route request
     *
     * @param uFlag                     uFlag
     * @param hopCount                  hopCount
     * @param rreqID                    rreqID
     * @param originAddress             originAddress
     * @param originSequenceNumber      originSequenceNumber
     * @param destinationAddress        destinationAddress
     * @param destinationSequenceNumber destinationSequenceNumber
     */
    public RREQ(byte uFlag, byte hopCount, byte rreqID, byte originAddress, byte originSequenceNumber, byte destinationAddress, byte destinationSequenceNumber) {
        super(Type.RREQ, "FFFF");
        this.uFlag = uFlag;
        this.hopCount = hopCount;
        this.originAddress = originAddress;
        this.originSequenceNumber = originSequenceNumber;
        this.destinationAddress = destinationAddress;
        this.destinationSequenceNumber = destinationSequenceNumber;
        this.rreqID = rreqID;
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

    public byte getRreqID() {
        return rreqID;
    }

    public void setRreqID(byte rreqID) {
        this.rreqID = rreqID;
    }

    @Override
    public byte[] toMessage() {
        return new byte[]{getTYPE(), uFlag, hopCount, rreqID, originAddress, originSequenceNumber, destinationAddress, destinationSequenceNumber};
    }
}
