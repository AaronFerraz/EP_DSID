package Peer;

import Protocol.MessageHandler;
import Protocol.MessageHelper;
import logger.Logger;
import logger.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Peer implements Runnable{
    private final String ip;
    private final int port;
    private final HashMap<String, PeerInfo> neighbors;
    private final String neighborsFilePath;
    private int clock = 0;
    private final Path path;

    private static final Logger log = LoggerFactory.getLogger(Peer.class);

    public Peer(String ip, int port, String neighborsFilePath, String sharedDirPath) {
        this.ip = ip;
        this.port = port;
        this.neighborsFilePath = neighborsFilePath;
        this.neighbors = new HashMap<>();
        this.path = Path.of(sharedDirPath);

    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.log("Servidor iniciado em " + ip + ":" + port, true);
            neighborsDiscovery();
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> MessageHandler.handleReceiveMessage(this, clientSocket)).start();
            }
        } catch (IOException e) {
            log.logDebug("Erro ao iniciar o Server socket do peer");
            log.logDebug(e.getMessage());
            System.exit(1);
        }
    }

    public String sendMessage(PeerInfo pi, String message, String... arguments) {
        incrementClock();
        String fullMessage = MessageHelper.createMessage(ip, port, clock, message, arguments);

        log.log("Encaminhando mensagem \"%s\" para %s", fullMessage, pi);

        String answer = MessageHandler.handleSendMessage(fullMessage, pi);

        if (!answer.isBlank()) {
            log.log("Resposta recebida: \"%s\"", answer);
            incrementClock();
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

    private void neighborsDiscovery() {
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
            log.logDebug("Arquivo não encontrado");
            log.logDebug("Programa se encerrando...");
            System.exit(1);
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

        log.log(sb.toString(), true);
        Scanner in = new Scanner(System.in);
        int escolha;
        PeerInfo pi;
        do {
            log.log("> ", false);
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

    public synchronized void incrementClock() {
        clock++;
        log.log("=> Atualizando relogio para %s", clock);
    }

    public Path getPathDir(){
        return this.path;
    }

    public void showDirectoryShared(){
        try(Stream<Path> paths = Files.list(getPathDir())){
            paths.map(Path::getFileName).forEach(
                    System.out::println
            );
        } catch(IOException io){
            io.printStackTrace();
        }
    }

    public void changeStatusPeer(String address){
        if (neighbors.containsKey(address)) {
            PeerInfo pi = neighbors.get(address);
            pi.setStatus("OFFLINE");
        }
    }

    public void bye() {
        log.log("Saindo...", true);

        neighbors.forEach((k, p) -> {
            if (p.getStatus().equals("ONLINE")){
                sendMessage(p, "BYE");
            }
        });

//        Thread.currentThread().interrupt();
    }

    public static Peer createAndStartPeer(String[] args) {
        String ip,portString;
        int port;

        String address = "127.0.0.1:9001";
        String nomeArquivo = "files/peer1.txt";
        String dirCompartilhado = "files/p1/";

        if (args.length == 3) {
            address = args[0];
            nomeArquivo = args[1];
            dirCompartilhado = args[2];
        }

        if (address.split(":").length != 2) {
            throw new RuntimeException("Endereço deve seguir o padrão ip:porta");
        }
        ip = address.split(":")[0];
        portString = address.split(":")[1];

        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            throw new RuntimeException("A porta deve ser um número inteiro maior que zero.");
        } catch (Exception e) {
            //Como vc chegou aqui?
            throw new RuntimeException("Parabens");
        }

        Peer peer = new Peer(ip, port, nomeArquivo, dirCompartilhado);
        new Thread(peer).start();

        return peer;
    }
}
