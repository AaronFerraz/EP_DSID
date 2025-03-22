package Peer;

import java.util.ArrayList;
import java.util.HashMap;

public class Peer {
    private String address;
    private int port;
    private ArrayList<String> neighbors;
    private NeighborManager neighborManager;

    public Peer(String address, int port){
        this.address = address;
        this.port = port;
    }
}
