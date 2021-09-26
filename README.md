# Lora-Protocol-Interface
This project implements a custom version of the [AODV](https://datatracker.ietf.org/doc/html/rfc3561) Protocol. It's much simpler and not complete.   
This programm should run on a machine connected via Serial Port to a LORA Module and implements an Ad-Hoc Multi-Hop-Routing Protocol with a graphical Interface. Furthermore this programm is able to discover other Nodes, send and receive messages.

## Protocol
**All AODV steps are implemented besides:**  
- Expanding Ring Search by RREQs (6.4)
- Gratuitous RREPs and Intermediate RREPs (6.6.2) & (6.6.3)
- Hello-Messages (6.9)
- Maintaining Local Connectivity (6.10)
- Local Repairs (6.12)
- Actions after Reboot (6.13)
- Interfaces (6.14)
- Subnetworks

**Restrictions:**  
- Max Tries: 3
- Timeout RREQ: 30s
- Timeout ACK: 4s - 6s
- Timeout Payload: TimeoutACK.max * RouteHopCount
- Route Lifetime = Route Deletion Time = 3 min = 180 s
- Blacklist duration  = 3 min = 180 s
- Adressen 1-20 (z.B. 144 -> 14)

## Packets
**All Packets are encoded as Bytes**  
(5,11,13,1,Hello)  
[05 0B 0D 01 48 65 6C 6C 6f] 

RREQ (broadcast):
1. Type: 1
2. U-flag
3. Hop Count
4. ID / Broadcast ID / RREQ ID
5. Origin Address
6. Origin Sequence Number
7. Destination Address
8. Destination Sequence Number

RREP: (Are always acknowledged, AT+DEST=PreviousHopAddr)
1. Type: 2
2. Hop Count
3. Origin Address
4. Destination Address
5. Destination Sequence Number
6. Lifetime in s (Lifetime left)

RERR (sent if no SEND-HOP-ACK received):
1. Type: 3
2. Destination Count
3. unreachable Destination Address
4. unreachable Destination Sequence Number
5. additional Addresses
6. additional Sequence Number

RREP-ACK (AT+DEST=PreviousHopAddr):
1. Type: 4

SEND-TEXT-REQUEST (STR, AT+DEST=NextHopAddr)
1. Type: 5
2. Origin Address
3. Destination Address
4. Message Sequence Number
5. Payload (max. 30 Byte)

SEND-HOP-ACK (AT+DEST=PreviousHopAddr)
1. Type 6
2. Message Sequence Number

SEND-TEXT-REQUEST-ACK (STR-ACK, AT+DEST=NextHopAddr)
1. Type: 7
2. Origin Address
3. Destination Address
4. Message Sequence Number

# Deployment 

## Requirements
- [JavaFX](https://openjfx.io/)
## Build yourself

1. Clone Repository
2. Run: `mvn clean package`
3. Run: `java -jar --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml LoraProtocollInterface-1.0-SNAPSHOT-jar-with-dependencies.jar`

## Use Release

1. Unzip
2. Run: `java -jar --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml LoraProtocollInterface-1.0-SNAPSHOT-jar-with-dependencies.jar`

# Screenshots

![GUI](../../blob/main/Github/Images/GUI.png)

![Chat](../../blob/main/Github/Images/Chat.jpg)

![Settings 1](../../blob/main/Github/Images/Settings1.png)

![Settings 2](../../blob/main/Github/Images/Settings2.png)

![REQ](../../blob/main/Github/Images/REQ.jpg)
