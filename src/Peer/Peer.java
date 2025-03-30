package Peer;

import Protocol.MessageHandler;
import Protocol.MessageHelper;
import logger.Logger;
import logger.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Peer implements Runnable{
    private final String ip;
    private final int port;
    private final HashMap<String, PeerInfo> neighbors;
    private final String neighborsFilePath;
    private int clock = 0;
    private Path path;

    private static final Logger log = LoggerFactory.getLogger(Peer.class);

    public Peer(String ip, int port, String neighborsFilePath, String sharedDirPath){
        this.ip = ip;
        this.port = port;
        this.neighborsFilePath = neighborsFilePath;
        this.neighbors = new HashMap<>();
        this.path = Path.of(sharedDirPath);

        vizinhosDiscovery();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)){
            log.log("Servidor iniciado em " + ip + ":" + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> MessageHandler.handleReceiveMessage(this, clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String sendMessage(PeerInfo pi, String message, String... arguments) {
        String fullMessage = MessageHelper.createMessage(ip, port, clock, message, arguments);

        log.log("Encaminhando mensagem \"%s\" para %s", fullMessage, pi);

        String answer = MessageHandler.handleSendMessage(fullMessage, pi);

        if (!answer.isBlank()) {
            log.log("Resposta recebida: \"%s\"", answer);
        }

        return answer;
    }

    public void getPeers() {
        ArrayList<PeerInfo> newPeers = new ArrayList<>(neighbors.values());
        for (PeerInfo v : newPeers) {
            String getPeersAnswer = sendMessage(v, "GET_PEERS");

            String[] rawMessage = getPeersAnswer.split(" ");

            String source = rawMessage[0];

            addNeighborByAddress(source);

            int peerQtt = Integer.parseInt(rawMessage[3]);

            if (peerQtt > 0) {
                for (int j = 4; j <= 3 + peerQtt; j++) {
                    String[] arg = rawMessage[j].split(":");

                    String ip = arg[0];
                    int port = Integer.parseInt(arg[1]);
                    String status = arg[2];
                    String endNumber = arg[3];

                    if (status.equals("ONLINE")) {
                        addNeighborByAddress(String.format("%s:%s", ip, port));
                    }
                }
            }
        }
    }

    private void vizinhosDiscovery() {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(neighborsFilePath))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                String[] parts = line.trim().split(":");
                if (parts.length == 2) {
                    String ip = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    neighbors.put(String.format("%s:%s",ip,port), new PeerInfo(ip, port, "OFFLINE"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void addNeighborByAddress(String address) {
        PeerInfo pi = neighbors.get(address);
        if (pi == null) {
            neighbors.put(address, new PeerInfo(
                    address.split(":")[0],
                    Integer.parseInt(address.split(":")[1]),
                    "ONLINE"
            ));
        }
        else if (pi.getStatus().equals("OFFLINE")){
            pi.setStatus("ONLINE");
        }
    }

    public PeerInfo listarPeers() {
        HashMap<Integer, PeerInfo> tempPeerMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        AtomicInteger n = new AtomicInteger();
        sb.append(String.format("[%s] voltar para o menu anterior %n", n));

        neighbors.forEach((k,i) -> {
            n.getAndIncrement();
            tempPeerMap.put(n.get(), i);
            sb.append(String.format("[%s] %s %s %n", n, i, i.getStatus()));
        });

        System.out.println(sb);
        Scanner in = new Scanner(System.in);
        int escolha;
        PeerInfo pi;
        do {
            escolha = in.nextInt();
            pi = tempPeerMap.get(escolha);
        } while (pi == null && escolha != 0);


        return pi;
    }

    public void listarPeersConhecidos(Socket clientSocket, String source) {
        ArrayList<PeerInfo> neighborToSend = new ArrayList<PeerInfo>();

        neighbors.forEach((k, v) -> {
            if (!k.equals(source)) neighborToSend.add(v);
        });

        StringBuilder sb = new StringBuilder();

        neighborToSend.forEach(n ->
                sb.append(n.toString())
                .append(":")
                .append(n.getStatus())
                .append(":")
                .append(0)
                .append(" "));

        String answer = MessageHelper.createMessage(
                ip,
                port,
                clock,
                "PEER_LIST",
                String.valueOf(neighborToSend.size()),
                sb.toString().trim()
        );

        MessageHandler.handleAnswerMessage(clientSocket, answer);
    }

    public void bye() {

    }
}
