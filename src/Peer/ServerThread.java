package Peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread implements Runnable{
    private PeerInfo peer;

    public ServerThread(PeerInfo peer){
        this.peer = peer;
    }

    @Override
    public void run(){
        try(ServerSocket socketServer = new ServerSocket(peer.getPort());
            Socket clientSocket = socketServer.accept()){

            new Thread(new ClientThread(clientSocket, peer)).start();
        }catch (IOException e){

        }
    }
}
