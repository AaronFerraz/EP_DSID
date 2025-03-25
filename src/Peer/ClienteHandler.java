package Peer;

import Protocol.MessageHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClienteHandler implements Runnable{
    private Socket socket;
    private Peer peer;
    private MessageHandler messageHandler;

    public ClienteHandler(Socket socket, Peer peer){
        this.socket = socket;
        this.peer = peer;
    }

    @Override
    public void run() {
        try(BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
            String message;
            while((message = in.readLine()) != null){
                messageHandler.receiveMessage(mensagem);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
