package htw.ai.lora;

import htw.ai.lora.config.Config;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol
 * @version : 1.0
 * @since : 14-05-2021
 **/
public class Message {
    private int sourceAddress;
    private String data;
    private boolean isUserMessage;

    /**
     * Represents a received message
     *
     * @param data data received ex. LR,0012,05,Hello
     */
    public Message(String data, boolean isUserMessage) {
        this.data = data;
        if (!isUserMessage)
            interpret();
        else {
            // TODO: CHANGE
            this.sourceAddress = 12;
            this.data = data;
            this.isUserMessage = true;
        }
    }

    /**
     * Split data to source address and actual message
     */
    private void interpret() {
        String[] dataSplit = data.split(Lora.DIVIDER.CODE);
        // 0-> LR 1-> source address 2-> bytes number 3-> data
        sourceAddress = Integer.parseInt(dataSplit[1], 16);
        data = dataSplit[3];
    }

    public int getSourceAddress() {
        return sourceAddress;
    }

    public String getData() {
        return data;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }
}
