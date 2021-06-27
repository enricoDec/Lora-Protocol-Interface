package htw.ai.protocoll.message;

import java.util.LinkedList;

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
    private LinkedList<Byte> additionalAddresses;
    private LinkedList<Byte> additionalSequenceNumber;

    public RERR(String actualATDestination, byte destinationCount, byte unreachableDestinationAddress,
                byte unreachableDestinationSequenceNumber, LinkedList<Byte> additionalAddresses, LinkedList<Byte> additionalSequenceNumber) {
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

    public LinkedList<Byte> getAdditionalAddresses() {
        return additionalAddresses;
    }

    public void setAdditionalAddresses(LinkedList<Byte> additionalAddresses) {
        this.additionalAddresses = additionalAddresses;
    }

    public LinkedList<Byte> getAdditionalSequenceNumber() {
        return additionalSequenceNumber;
    }

    public void setAdditionalSequenceNumber(LinkedList<Byte> additionalSequenceNumber) {
        this.additionalSequenceNumber = additionalSequenceNumber;
    }

    @Override
    public byte[] toMessage() {
        int offset = 4;
        byte[] message = new byte[offset + additionalAddresses.size() + additionalSequenceNumber.size()];

        message[0] = getTYPE();
        message[1] = destinationCount;
        message[2] = unreachableDestinationAddress;
        message[3] = unreachableDestinationSequenceNumber;

        for (int i = 0; offset + i + 1 < message.length; i = i + 2) {
            message[offset + i] = additionalAddresses.get(i);
            message[offset + i + 1] = additionalSequenceNumber.get(i);
        }
        return message;
    }

    @Override
    public String toString() {
        return "RERR{" + "Destination Count: " + Byte.toUnsignedInt(destinationCount) +
                ", Unreachable Destination Address: " + Byte.toUnsignedInt(unreachableDestinationAddress) +
                ", Unreachable Destination Sequence Number: " + Byte.toUnsignedInt(unreachableDestinationSequenceNumber) +
                ", Additional Addresses: " + additionalAddresses +
                ", Additional Sequence Number: " + additionalSequenceNumber +
                '}';
    }
}
