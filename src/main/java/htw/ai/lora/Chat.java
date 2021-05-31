package htw.ai.lora;

import java.util.LinkedList;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 31-05-2021
 **/
public class Chat {
    private LinkedList<Message> chatmessages = new LinkedList<>();
    private int userId;

    public Chat(int userId) {
        this.userId = userId;
    }
}
