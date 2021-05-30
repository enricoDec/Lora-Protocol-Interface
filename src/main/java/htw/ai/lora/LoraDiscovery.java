package htw.ai.lora;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;

import java.util.HashSet;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol
 * @version : 1.0
 * @since : 20-05-2021
 **/
public class LoraDiscovery {
    private ListProperty<Message> messages = new SimpleListProperty<>();
    private HashSet<Integer> discoveryList = new HashSet<>();

    /**
     * Add a client address to the discovery list
     *
     * @param clientId address to be added
     */
    public void addClientAddress(int clientId) {
        discoveryList.add(clientId);
    }

    /**
     * Get the discovery list
     *
     * @return discovery list reference
     */
    public HashSet<Integer> getDiscoveryList() {
        return discoveryList;
    }

    public ObservableList<Message> getMessages() {
        return messages.get();
    }

    public ListProperty<Message> messagesProperty() {
        return messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }
}
