package htw.ai.application.model;

import htw.ai.application.controller.ChatsController;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.HashSet;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol
 * @version : 1.0
 * @since : 20-05-2021
 **/
public class ChatsDiscovery {
    private HashSet<Integer> discoveryList = new HashSet<>();
    private IntegerProperty newClient = new SimpleIntegerProperty();
    private Chats chats;

    public ChatsDiscovery(Chats chats) {
        this.chats = chats;
    }

    /**
     * Add a client address to the discovery list
     *
     * @param message message to be added
     */
    public void newClient(ClientMessage message) {
        // User Message
        if (message instanceof UserMessage) {
            // If destination not yet in list
            if (!discoveryList.contains(message.getDestinationAddress())) {
                int destinationAddress = message.getDestinationAddress();
                discoveryList.add(destinationAddress);
                newClient.set(destinationAddress);
                chats.addChat(destinationAddress, message, false);
            } else
                chats.addMessageToChat(message.getDestinationAddress(), message);
        } else {
            // Client Message
            if (!discoveryList.contains(message.getSourceAddress())) {
                discoveryList.add(message.getSourceAddress());
                newClient.set(message.getSourceAddress());
                chats.addChat(message.getSourceAddress(), message, true);
            } else {
                chats.addMessageToChat(message.getSourceAddress(), message);
            }
        }
    }

    public HashSet<Integer> getDiscoveryList() {
        return discoveryList;
    }

    public int getNewClient() {
        return newClient.get();
    }

    public IntegerProperty newClientProperty() {
        return newClient;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Discovered Clients=").append(discoveryList);
        sb.append(", Chats=").append(chats.toString());
        return sb.toString();
    }
}
