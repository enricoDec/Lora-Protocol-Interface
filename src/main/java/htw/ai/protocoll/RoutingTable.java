package htw.ai.protocoll;

import java.util.HashMap;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 10-06-2021
 **/
public class RoutingTable {
    private HashMap<Integer, Route> routingTable;

    public RoutingTable() {
        routingTable = new HashMap<>();
    }

    public Route getRoute(int id) {
        return routingTable.get(id);
    }
}
