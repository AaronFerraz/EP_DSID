package Peer;

import java.util.ArrayList;
import java.util.HashMap;

public class Peer implements PeerInfo{
    private String address;
    private int port;
    private String status;
    private ArrayList<String> neighbors;
    private NeighborManager neighborManager;
    private FileManager fileManager;

    public Peer(String address, int port, String neighborsFilePath, String sharedDirPath){
        this.address = address;
        this.port = port;
        this.status = "ONLINE";
        this.neighborManager = new NeighborManager(neighborsFilePath);
        this.fileManager = new FileManager(sharedDirPath);
    }

    @Override
    public String getAddres() {
        return address;
    }
    @Override
    public int getPort() {
        return port;
    }

    public void start(){
        Thread serv = new Thread(new ServerThread());
        serv.start();

//        Thread cli = new Thread(new ClientThread(new Peer(getAddres(), getPort()));
    }
}
