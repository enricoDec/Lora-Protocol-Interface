package htw.ai.lora;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 31-05-2021
 **/
public class Chats {
    private HashMap<Integer, LinkedList<Message>> chatsList = new HashMap<>();
    private StringProperty newClientMessage = new SimpleStringProperty();

    public Chats() {
        this.newClientMessage.set("");
    }

    /**
     * Make chat for new Client
     *
     * @param id      client id (Address)
     * @param message client message to add
     */
    public void addChat(int id, Message message) {
        LinkedList<Message> messages = new LinkedList<>();
        messages.add(message);
        chatsList.put(id, messages);
        newClientMessage.set(message.getData());
    }

    /**
     * Add Message from a Client to the Chat
     *
     * @param id      client id (Address)
     * @param message client message to add
     */
    public void addMessageToChat(int id, Message message) {
        chatsList.get(id).add(message);
        newClientMessage.set(message.getData());
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
    public LinkedList<Message> getClientMessages(int id) {
        return chatsList.get(id);
    }

    /**
     * Get newClientMessage Property
     *
     * @return newClientMessageProperty
     */
    public StringProperty newClientMessageProperty() {
        return newClientMessage;
    }

    /**
     * Get all chats
     *
     * @return HashMap<Integer, LinkedList < Message>>
     */
    public HashMap<Integer, LinkedList<Message>> getChatsList() {
        return chatsList;
    }
}
