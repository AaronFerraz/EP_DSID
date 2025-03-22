package Peer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread implements Runnable{
    private PeerInfo peerInfo;

    ClientThread(PeerInfo peerinfo){
        this.peerInfo = peerinfo;
    }

    @Override
    public void run(){
        try{
            Socket socket = new Socket(peerInfo.getAddres(), peerInfo.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


        } catch(Exception e){

        }
    }
}
