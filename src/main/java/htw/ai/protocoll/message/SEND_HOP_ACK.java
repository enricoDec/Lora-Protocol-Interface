package htw.ai.protocoll.message;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public class SEND_HOP_ACK extends Message {
    private byte messageSequenceNumber;

    public SEND_HOP_ACK(String actualATDestination, byte messageSequenceNumber) {
        super(Type.SEND_HOP_ACK, actualATDestination);
        this.messageSequenceNumber = messageSequenceNumber;
    }

    public byte getMessageSequenceNumber() {
        return messageSequenceNumber;
    }

    public void setMessageSequenceNumber(byte messageSequenceNumber) {
        this.messageSequenceNumber = messageSequenceNumber;
    }

    @Override
    public byte[] toMessage() {
        return new byte[]{getTYPE(), messageSequenceNumber};
    }

    @Override
    public String toString() {
        return "SEND HOP ACK{" + "Message Seq: " + Byte.toUnsignedInt(messageSequenceNumber) +
                '}';
    }
}
