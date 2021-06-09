package htw.ai.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 31-05-2021
 **/
public class Chats {
    private HashMap<Integer, LinkedList<ClientMessage>> chatsList = new HashMap<>();
    private ObjectProperty<ClientMessage> newMessage = new SimpleObjectProperty<>();

    /**
     * Make chat for new Client
     *
     * @param id      client id (Address)
     * @param message client message to add
     */
    public void addChat(int id, ClientMessage message, boolean triggerListener) {
        LinkedList<ClientMessage> messages = new LinkedList<>();
        messages.add(message);
        chatsList.put(id, messages);
        if (triggerListener)
            newMessage.set(message);
    }

    /**
     * Add Message from a Client to the Chat
     *
     * @param id      client id (Address)
     * @param message client message to add
     */
    public void addMessageToChat(int id, ClientMessage message) {
        chatsList.get(id).add(message);
        newMessage.set(message);
    }

    /**
     * Remove a client from the Chats
     *
     * @param id client id (Address)
     */
    public void removeChat(int id) {
        chatsList.remove(id);
    }

    /**
     * Get List of all Messages from given Client
     *
     * @param id client id (Address)
     * @return List of all Messages from given Client
     */
    public LinkedList<ClientMessage> getClientMessages(int id) {
        return chatsList.get(id);
    }

    /**
     * Get the new Message Property
     *
     * @return new Message Property
     */
    public ObjectProperty<ClientMessage> newMessageProperty() {
        return newMessage;
    }

    /**
     * Get all chats
     *
     * @return HashMap<Integer, LinkedList < Message>>
     */
    public HashMap<Integer, LinkedList<ClientMessage>> getChatsList() {
        return chatsList;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("chatsList=").append(chatsList);
        sb.append(", message=").append(newMessage.toString());
        return sb.toString();
    }
}
