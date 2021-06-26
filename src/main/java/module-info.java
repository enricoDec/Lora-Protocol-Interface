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
    // Serial Comm
    requires com.fazecast.jSerialComm;
    requires Java.WebSocket;

    exports htw.ai;
    opens htw.ai to javafx.graphics, javafx.fxml;
    exports htw.ai.application.controller;
    opens htw.ai.application.controller to javafx.fxml, javafx.graphics;
}