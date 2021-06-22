package htw.ai.protocoll;

import java.time.LocalDateTime;
import java.util.LinkedList;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public class Route {
    private int destinationAddress;
    private byte destinationSequenceNumber;
    private boolean isValidRoute;
    private byte hopCount;
    private int nextHop;
    private LinkedList<Node> precursorsList = new LinkedList<>();
    private LocalDateTime lifetime;

    public Route(int destinationAddress, byte destinationSequenceNumber, boolean isValidRoute, byte hopCount, int nextHop, long lifetime) {
        this.destinationAddress = destinationAddress;
        this.destinationSequenceNumber = destinationSequenceNumber;
        this.isValidRoute = isValidRoute;
        this.hopCount = hopCount;
        this.nextHop = nextHop;
        this.lifetime = LocalDateTime.now().plusSeconds(lifetime);
    }

    public int getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(int destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public byte getDestinationSequenceNumber() {
        return destinationSequenceNumber;
    }

    public void setDestinationSequenceNumber(byte destinationSequenceNumber) {
        this.destinationSequenceNumber = destinationSequenceNumber;
    }

    public boolean isValidRoute() {
        return isValidRoute;
    }

    public void setValidRoute(boolean validRoute) {
        isValidRoute = validRoute;
    }

    public byte getHopCount() {
        return hopCount;
    }

    public void setHopCount(byte hopCount) {
        this.hopCount = hopCount;
    }

    public int getNextHop() {
        return nextHop;
    }

    public void setNextHop(int nextHop) {
        this.nextHop = nextHop;
    }

    public LinkedList<Node> getPrecursorsList() {
        return precursorsList;
    }

    public void setPrecursorsList(LinkedList<Node> precursorsList) {
        this.precursorsList = precursorsList;
    }

    public LocalDateTime getLifetime() {
        return lifetime;
    }

    public void setLifetime(LocalDateTime lifetime) {
        this.lifetime = lifetime;
    }

    @Override
    public String toString() {
        return "Route{" +
                "destinationAddress=" + destinationAddress +
                ", destinationSequenceNumber=" + destinationSequenceNumber +
                ", isValidRoute=" + isValidRoute +
                ", hopCount=" + hopCount +
                ", nextHop=" + nextHop +
                ", precursorsList=" + precursorsList +
                ", lifetime=" + lifetime +
                '}';
    }
}
