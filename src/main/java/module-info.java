module LoraProtocolInterface {
    // JavaFX
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    // JFoenix
    requires com.jfoenix;
    // Icons
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.bootstrapicons;

    exports htw.ai;
    opens htw.ai to javafx.graphics, javafx.fxml;
}