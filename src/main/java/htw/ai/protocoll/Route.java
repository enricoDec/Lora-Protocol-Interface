package htw.ai.protocoll;

import java.util.LinkedList;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public class Route {
    private int destinationAddress;
    private long destinationSequenceNumber;
    private boolean isValidRoute;
    private int hopCount;
    private int nextHop;
    private LinkedList<Node> precursorsList = new LinkedList<>();
    private long lifetime;

    public Route(int destinationAddress, long destinationSequenceNumber, boolean isValidRoute, int hopCount, int nextHop, long lifetime) {
        this.destinationAddress = destinationAddress;
        this.destinationSequenceNumber = destinationSequenceNumber;
        this.isValidRoute = isValidRoute;
        this.hopCount = hopCount;
        this.nextHop = nextHop;
        this.lifetime = lifetime;
    }

    public int getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(int destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public long getDestinationSequenceNumber() {
        return destinationSequenceNumber;
    }

    public void setDestinationSequenceNumber(long destinationSequenceNumber) {
        this.destinationSequenceNumber = destinationSequenceNumber;
    }

    public boolean isValidRoute() {
        return isValidRoute;
    }

    public void setValidRoute(boolean validRoute) {
        isValidRoute = validRoute;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
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

    public long getLifetime() {
        return lifetime;
    }

    public void setLifetime(long lifetime) {
        this.lifetime = lifetime;
    }
}
