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

import static Peer.PeerHandler.*;

public class Peer implements Runnable{
    private final String ip;
    private final int port;
    private final String neighborsFilePath;
    private final LamportClock lamportClock;
    private final Path path;

    private static final Logger log = LoggerFactory.getLogger(Peer.class);

    public Peer(String ip, int port, String neighborsFilePath, String sharedDirPath) {
        this.ip = ip;
        this.port = port;
        this.neighborsFilePath = neighborsFilePath;
        this.path = Path.of(sharedDirPath);
        this.lamportClock = new LamportClock();
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
        incrementClock(0);
        String fullMessage = MessageHelper.createMessage(ip, port, lamportClock.getClock(), message, arguments);

        log.log("Encaminhando mensagem \"%s\" para %s", fullMessage, pi);

        String answer = MessageHandler.handleSendMessage(fullMessage, pi);

        if (!answer.isBlank()) {
            log.log("Resposta recebida: \"%s\"", answer);
        }

        return answer;
    }

    public void getPeers() {
        ArrayList<PeerInfo> newPeers = new ArrayList<>(getNeighborsAsList());
        for (PeerInfo v : newPeers) {
            String getPeersAnswer = sendMessage(v, "GET_PEERS");
            if (!getPeersAnswer.isBlank()) {
                String[] rawMessage = getPeersAnswer.split(" ");

                String source = rawMessage[0];
                int externalClock = Integer.parseInt(rawMessage[1]);
                incrementClock(externalClock);

                addNeighborByAddress(source, externalClock);

                int peerQtt = Integer.parseInt(rawMessage[3]);

                if (peerQtt > 0) {
                    for (int j = 4; j <= 3 + peerQtt; j++) {
                        String[] arg = rawMessage[j].split(":");

                        String ip = arg[0];
                        int port = Integer.parseInt(arg[1]);
                        String status = arg[2];
                        int clock = Integer.parseInt(arg[3]);

                        addNeighborByAddress(String.format("%s:%s", ip, port), status, clock);

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
                    addNeighbor(String.format("%s:%s",ip,port), new PeerInfo(ip, port, "OFFLINE", 0));
                }
            }
        } catch (IOException e) {
            log.logDebug(e.getMessage());
            log.logDebug("Arquivo não encontrado");
            log.logDebug("Programa se encerrando...");
            System.exit(1);
        }
    }

    public void addNeighborByAddress(String address, int clock) {
        addNeighborByAddress(address, "ONLINE", clock);
    }

    public void addNeighborByAddress(String address, String status, int clock) {
        PeerInfo pi = getNeighbor(address);
        if (pi == null) {
            addNeighbor(address, new PeerInfo(
                    address.split(":")[0],
                    Integer.parseInt(address.split(":")[1]),
                    status,
                    clock
            ));

        }
        else if (clock > pi.getClock()) {
            pi.setClock(clock);
            pi.setStatus(status);
        }
    }

    public void hello(PeerInfo listarPeersResult) {
        if (listarPeersResult !=null) {
            try {
                sendMessage(listarPeersResult, "HELLO");
            } catch (RuntimeException re){
                log.log("Não deu certo mandar a mensagem", true);
            }
        }
    }

    public PeerInfo listarPeers() {
        HashMap<Integer, PeerInfo> tempPeerMap = new HashMap<>();

        String sb = String.format("[%s] voltar para o menu anterior %n", 0) +
                buildGetPeersMessage(tempPeerMap);

        log.log(sb, true);
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
        StringBuilder sb = new StringBuilder();
        AtomicInteger neighborsSize = new AtomicInteger();
        getNeighbors().forEach((k,v) ->
                {
                    if (!k.equals(source)) {
                        sb.append(k)
                                .append(":")
                                .append(v.getStatus())
                                .append(":")
                                .append(v.getClock())
                                .append(" ");
                        neighborsSize.getAndIncrement();
                    }
                }
        );

        String answer = MessageHelper.createMessage(
                ip,
                port,
                lamportClock.getClock(),
                "PEER_LIST",
                String.valueOf(neighborsSize.get()),
                sb.toString().trim()
        );

        MessageHandler.handleAnswerMessage(clientSocket, answer, source);
    }

    public synchronized void incrementClock(int externalClock) {
        int newClock = Math.max(externalClock, getClock());
        lamportClock.incrementClock(newClock);
    }

    public synchronized int getClock() {
        return lamportClock.getClock();
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
            log.logDebug(io.getMessage());
        }
    }

    public void changeStatusPeer(String address, int clock){
        PeerHandler.changePeerStatus(address, clock);
    }

    public void bye() {
        log.log("Saindo...", true);

        exit(this);

    }

    public static Peer createAndStartPeer(String[] args) throws InterruptedException {
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
        Thread p = new Thread(peer);
        p.start();

        //Único jeito que pensei para a mensagem de início printar na ordem certa
        Thread.sleep(100);

        return peer;
    }

    private static class LamportClock {
        private int clock;

        public void incrementClock(int clock) {
            this.clock = clock+1;
            log.log("=> Atualizando relogio para %s", getClock());
        }

        public int getClock() {
            return clock;
        }
    }
}
