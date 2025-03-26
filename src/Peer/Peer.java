package Peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Peer implements Runnable{
    private String ip;
    private int port;
    private String status;
    private ArrayList<String> neighbors;
    private NeighborManager neighborManager;
    private FileManager fileManager;

    public Peer(String ip, int port, String neighborsFilePath, String sharedDirPath){
        this.ip = ip;
        this.port = port;
        this.status = "ONLINE";
        this.neighborManager = new NeighborManager(neighborsFilePath);
        this.fileManager = new FileManager(sharedDirPath);
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado em " + ip + ":" + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClientMessage(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void handleClientMessage(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Mensagem recebida: \""+clientSocket.getInetAddress() + "\" CLOCK "+message);

                switch (message) {
                    case "HELLO":
                        ClienteHandler.helloMessage(clientSocket, message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleSendMessage(String message, String... arguments) {

    }
}
