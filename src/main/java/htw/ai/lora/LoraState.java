package htw.ai.lora;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol
 * @version : 1.0
 * @since : 14-05-2021
 **/
public enum LoraState {
    /**
     * Start state
     */
    START,
    /**
     * Wait for input from user
     */
    USER_INPUT,
    /**
     * Wait for lora reply code
     */
    WAIT_REPLY,
    /**
     * Waiting for lora reply code AT,SENDING
     */
    SENDING,
    /**
     * Waiting for Lora reply code AT,SENDED
     */
    SENDED,
}
