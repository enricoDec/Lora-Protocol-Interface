package htw.ai.protocoll;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public class Route {
    private byte destinationAddress;
    private byte destinationSequenceNumber;
    private boolean validRoute;
    private byte hopCount;
    private byte nextHop;
    private LinkedList<Node> precursorsList = new LinkedList<>();
    private int precursor = 0;
    private LocalDateTime lifetime;

    public Route(byte destinationAddress, byte destinationSequenceNumber, boolean validRoute, byte hopCount, byte nextHop, long lifetime) {
        this.destinationAddress = destinationAddress;
        this.destinationSequenceNumber = destinationSequenceNumber;
        this.validRoute = validRoute;
        this.hopCount = hopCount;
        this.nextHop = nextHop;
        this.lifetime = LocalDateTime.now().plusSeconds(lifetime);
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

    public boolean getValidRoute() {
        return validRoute;
    }

    public void setValidRoute(boolean validRoute) {
        this.validRoute = validRoute;
    }

    public byte getHopCount() {
        return hopCount;
    }

    public void setHopCount(byte hopCount) {
        this.hopCount = hopCount;
    }

    public byte getNextHop() {
        return nextHop;
    }

    public void setNextHop(byte nextHop) {
        this.nextHop = nextHop;
    }

    public LinkedList<Node> getPrecursorsList() {
        return precursorsList;
    }

    public void setPrecursorsList(LinkedList<Node> precursorsList) {
        this.precursorsList = precursorsList;
    }

    public byte getLifetime() {
        long diff = ChronoUnit.SECONDS.between(this.lifetime, LocalDateTime.now());
        System.out.println(diff);
        if (diff < 0)
            return 0;
        else
            return (byte) diff;
    }

    public void setLifetime(LocalDateTime lifetime) {
        this.lifetime = lifetime;
    }

    public int getPrecursor() {
        return precursor;
    }

    public void setPrecursor(int precursor) {
        this.precursor = precursor;
    }

    @Override
    public String toString() {
        return "Route{" +
                "destinationAddress=" + destinationAddress +
                ", destinationSequenceNumber=" + destinationSequenceNumber +
                ", isValidRoute=" + validRoute +
                ", hopCount=" + hopCount +
                ", nextHop=" + nextHop +
                ", precursorsList=" + precursorsList +
                ", lifetime=" + lifetime +
                '}';
    }
}
