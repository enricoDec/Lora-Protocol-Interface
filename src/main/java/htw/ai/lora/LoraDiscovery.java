package htw.ai.lora;

import htw.ai.ChatsController;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.HashSet;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol
 * @version : 1.0
 * @since : 20-05-2021
 **/
public class LoraDiscovery {
    private HashSet<Integer> discoveryList = new HashSet<>();
    private IntegerProperty newClient = new SimpleIntegerProperty();

    /**
     * Add a client address to the discovery list
     *
     * @param clientId address to be added
     */
    public void addClientAddress(int clientId) {
        if (!discoveryList.contains(clientId)) {
            ChatsController.writeToLog("New Client discovered " + clientId);
            discoveryList.add(clientId);
            newClient.set(clientId);
        }
    }

    /**
     * Get the discovery list
     *
     * @return discovery list reference
     */
    public HashSet<Integer> getDiscoveryList() {
        return discoveryList;
    }

    public int getNewClient() {
        return newClient.get();
    }

    public IntegerProperty newClientProperty() {
        return newClient;
    }

    public void setNewClient(int newClient) {
        this.newClient.set(newClient);
    }
}
