package Peer;

import Protocol.MessageHandler;
import Protocol.MessageHelper;
import logger.Logger;
import logger.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Peer implements Runnable{
    private final String ip;
    private final int port;
    private final ArrayList<PeerInfo> neighbors;
    private final String neighborsFilePath;

    private static final Logger log = LoggerFactory.getLogger(Peer.class);

    public Peer(String ip, int port, String neighborsFilePath, String sharedDirPath){
        this.ip = ip;
        this.port = port;
        this.neighborsFilePath = neighborsFilePath;
        this.neighbors = new ArrayList<>();

        vizinhosDiscovery();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)){
            log.log("Servidor iniciado em " + ip + ":" + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> MessageHandler.handleReceiveMessage(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void handleSendMessage(int neighborIndex, String message, String... arguments) {
        PeerInfo neighbor = neighbors.get(neighborIndex-1);
        String fullMessage = MessageHelper.createMessage(ip, port, message, arguments);

        log.log("Encaminhando mensagem \"%s\" para %s", fullMessage, neighbor);

        MessageHandler.handleSendMessage(fullMessage, neighbor);
    }

    private void vizinhosDiscovery() {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(neighborsFilePath))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                String[] parts = line.trim().split(":");
                if (parts.length == 2) {
                    String ip = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    neighbors.add(new PeerInfo(ip, port, "OFFLINE"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public int listarPeers() {
        StringBuilder sb = new StringBuilder();
        AtomicInteger n = new AtomicInteger();
        sb.append(String.format("[%s] voltar para o menu anterior %n", n));

        neighbors.forEach((i) -> {
            n.getAndIncrement();

            sb.append(String.format("[%s] %s %s %n", n, i, i.getStatus()));
        });

        System.out.println(sb);

        Scanner in = new Scanner(System.in);

        return in.nextInt();
    }
}
