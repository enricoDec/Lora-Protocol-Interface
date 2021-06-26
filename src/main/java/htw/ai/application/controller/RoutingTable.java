package htw.ai.application.controller;

import htw.ai.protocoll.Route;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashMap;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 26-06-2021
 **/
public class RoutingTable {
    private HashMap<Integer, Route> routingTable = ChatsController.aodvController.getRoutingTable();
    private ObservableList<Route> routingData = FXCollections.observableArrayList();

    @FXML
    public TableView<Route> routingTableView;
    @FXML
    public TableColumn<Route, Byte> destinationAddress;
    @FXML
    public TableColumn<Route, Byte> destinationSequenceNumber;
    @FXML
    public TableColumn<Route, Boolean> isValidRoute;
    @FXML
    public TableColumn<Route, Byte> hopCount;
    @FXML
    public TableColumn<Route, Integer> nextHop;
    @FXML
    public TableColumn<Route, Integer> precursorsList;
    @FXML
    public TableColumn<Route, Byte> lifetime;


    @FXML
    public void initialize() {
        routingTable.forEach((key, value) -> routingData.add(value));

        destinationAddress.setCellValueFactory(
                new PropertyValueFactory<>("destinationAddress")
        );

        destinationSequenceNumber.setCellValueFactory(
                new PropertyValueFactory<>("destinationSequenceNumber")
        );

        isValidRoute.setCellValueFactory(
                new PropertyValueFactory<>("validRoute")
        );

        hopCount.setCellValueFactory(
                new PropertyValueFactory<>("hopCount")
        );

        nextHop.setCellValueFactory(
                new PropertyValueFactory<>("nextHop")
        );

        precursorsList.setCellValueFactory(
                new PropertyValueFactory<>("precursor")
        );

        lifetime.setCellValueFactory(
                new PropertyValueFactory<>("lifetime")
        );

        routingTableView.setItems(routingData);
    }
}
