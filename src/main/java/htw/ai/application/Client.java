package htw.ai.application;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import htw.ai.lora.LoraUART;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 26-06-2021
 **/
public class Client extends WebSocketClient {
    private LoraUART loraUART;

    public Client(URI serverUri, LoraUART loraUART) {
        super(serverUri);
        this.loraUART = loraUART;
    }

    public Client(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
    }

    public Client(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    public Client(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders) {
        super(serverUri, protocolDraft, httpHeaders);
    }

    public Client(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
    }

    /**
     * This example demonstrates how to create a websocket connection to a server. Only the most
     * important callbacks are overloaded.
     */


    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("opened connection");
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void onMessage(String message) {
        if (message.startsWith("#start#")) {
            System.out.println(message);
            return;
        }
        this.loraUART.decode(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // The codecodes are documented in class org.java_websocket.framing.CloseFrame
        System.out.println(
                "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: "
                        + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }
}
