package htw.ai.application.model;

import htw.ai.lora.Lora;

import java.util.Arrays;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol
 * @version : 1.0
 * @since : 14-05-2021
 **/
public class ClientMessage{
    private int sourceAddress;
    private int destinationAddress;
    private String data;

    /**
     * Represents a received message
     *
     * @param data data received ex. LR,0012,05,Hello -> Hello
     */
    @Deprecated
    public ClientMessage(String data) {
        interpret(data);
    }

    public ClientMessage(String data, int sourceAddress, int destinationAddress) {
        this.data = data;
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
    }

    /**
     * Split data to source address and actual message
     */
    @Deprecated
    private void interpret(String data) {
        String[] dataSplit = data.split(Lora.DIVIDER.CODE);
        // 0-> LR 1-> source address 2-> bytes number 3-> data
        this.sourceAddress = Integer.parseInt(dataSplit[1]);
        this.data = Arrays.toString(Arrays.copyOfRange(dataSplit, 3, dataSplit.length));
    }

    public int getSourceAddress() {
        return sourceAddress;
    }
    public String getData() {
        return data;
    }
    public int getDestinationAddress() {
        return destinationAddress;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ClientMessage{");
        sb.append("sourceAddress=").append(sourceAddress);
        sb.append(", destinationAddress=").append(destinationAddress);
        sb.append(", data='").append(data).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
