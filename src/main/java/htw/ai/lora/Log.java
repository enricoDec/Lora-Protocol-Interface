package htw.ai.lora;

import javafx.scene.paint.Color;

/**
 * @author : Enrico Gamil Toros
 * Project name : LoraProtocollInterface
 * @version : 1.0
 * @since : 28.06.21
 **/
public class Log {
    private Color color;
    private String message;

    public Log(Color color, String message) {
        this.color = color;
        this.message = message;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
