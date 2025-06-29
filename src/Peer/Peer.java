package Peer;

import Protocol.MessageHandler;
import Protocol.MessageHelper;
import logger.Logger;
import logger.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static Peer.PeerHandler.*;

public class Peer implements Runnable{
    private final String ip;
    private final int port;
    private final String neighborsFilePath;
    private final LamportClock lamportClock;
    private final Path path;
    private final HashMap<String, PeerFile> ownFiles;
    private int chunk;
    private final HashMap<String, PeerStats> stats;
    private final Timer timer;

    private static final Logger log = LoggerFactory.getLogger(Peer.class);

    public Peer(String ip, int port, String neighborsFilePath, String sharedDirPath) {
        this.ip = ip;
        this.port = port;
        this.neighborsFilePath = neighborsFilePath;
        this.path = Path.of(sharedDirPath);
        this.lamportClock = new LamportClock();
        this.ownFiles = new HashMap<>();
        this.chunk = 256;
        this.stats = new HashMap<String, PeerStats>();
        this.timer = new Timer();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.log("Servidor iniciado em " + ip + ":" + port, true);
            neighborsDiscovery();
            filesDiscovery();
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

    private void filesDiscovery() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(getPathDir())) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    byte[] fileBytes = Files.readAllBytes(entry);
                    PeerFile data = new PeerFile(
                            entry.getFileName().toString(),
                            Files.size(entry),
                            fileBytes
                    );
                    this.ownFiles.put(entry.getFileName().toString(), data);
                }
            }
        } catch (IOException e) {
            log.logDebug(e.getMessage());
        }
    }

    public synchronized void addNeighborByAddress(String address, int clock) {
        addNeighborByAddress(address, "ONLINE", clock);
    }

    public synchronized void addNeighborByAddress(String address, String status, int clock) {
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
        getNeighborsAsList().forEach(v ->
                {
                    if (!v.toString().equals(source)) {
                        sb.append(v)
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

    public List<PeerFile> ls() {
        HashMap<String, List<PeerFile>> files = new HashMap<>();
        HashMap<Integer, String> indexFileByKey = new HashMap<>();
        AtomicInteger n = new AtomicInteger();

        getNeighborsAsList().forEach(v ->
        {
            if (v.getStatus().equals("ONLINE")) {
                String answer = sendMessage(v, "LS");

                if (!answer.isBlank()) {
                    String[] rawMessage = answer.split(" ");

                    String source = rawMessage[0];
                    int externalClock = Integer.parseInt(rawMessage[1]);
                    incrementClock(externalClock);

                    addNeighborByAddress(source, externalClock);

                    int fileQtt = Integer.parseInt(rawMessage[3]);

                    if (fileQtt > 0) {
                        for (int j = 4; j <= 3 + fileQtt; j++) {
                            String[] arg = rawMessage[j].split(":");

                            String fileName = arg[0];
                            int fileSize = Integer.parseInt(arg[1]);
                            String key = fileName+":"+fileSize;

                            if (!files.containsKey(key)) {
                                files.put(key, new ArrayList<>());
                                indexFileByKey.put(n.incrementAndGet(), key);
                            }
                            files.get(key).add(new PeerFile(fileName, fileSize, v));
                        }
                    }
                }
            }
        });

        String message = buildLsListMessage(indexFileByKey, files);

        log.log(message);

        Scanner in = new Scanner(System.in);
        int escolha;
        String piKey;
        do {
            log.log("> ", false);
            escolha = in.nextInt();
            piKey = indexFileByKey.get(escolha);
        } while (piKey == null && escolha != 0);
        timer.start();
        return files.get(piKey);
    }

    public void setChunk() {
        Scanner in = new Scanner(System.in);
        this.chunk = in.nextInt();
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
        this.ownFiles.forEach((k, f) ->
                log.log(f.getFileName()));
    }

    public void changeStatusPeer(String address, int clock){
        changePeerStatus(address, clock);
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

    public void ls_list(Socket clientSocket, String source) {
        StringBuilder sb = new StringBuilder();
        this.ownFiles.forEach((k, f) ->
                sb.append(f.getFileName()).append(":").append(f.getFileSize()).append(" "));
        String answer = MessageHelper.createMessage(
                ip,
                port,
                lamportClock.getClock(),
                "LS_LIST",
                String.valueOf(this.ownFiles.size()),
                sb.toString().trim()
        );

        MessageHandler.handleAnswerMessage(clientSocket, answer, source);

    }

    public void dl(List<PeerFile> lsResult) throws InterruptedException {
        if (lsResult != null && !lsResult.isEmpty()) {
            long size = lsResult.get(0).getFileSize();
            int indexes = Math.toIntExact(size / this.chunk);
            if (size % this.chunk != 0) indexes++;
            String[] fileBytes = new String[indexes];
            String fileName = "";
            
            List<Runnable> threads = new ArrayList<>();
            for (int i = 0; i < indexes; i++) {
                int index = i % lsResult.size();
                PeerFile pf = lsResult.get(index);
                fileName = pf.getFileName();
                Runnable r = getRunnable(pf, i, fileBytes);
                threads.add(r);
            }

            for (Runnable r : threads) {
                Thread t = new Thread(r);
                t.start();
                t.join();
            }

            long finish = timer.finish();

            String key = this.chunk+":"+lsResult.size()+":"+size;

            if (!stats.containsKey(key)) {
                stats.put(key, new PeerStats(String.valueOf(this.chunk), String.valueOf(lsResult.size()), String.valueOf(size)));
            }

            stats.get(key).novaAmostra(finish);

            Boolean success = writeFileToPath(this.path, fileName, fileBytes);

            if (success) {
                //TODO add listener for new files
                //this.ownFiles.put(fileName, pf);
                log.log("Download do arquivo %s finalizado.", fileName);
            }


        }
    }

    private Runnable getRunnable(PeerFile pf, int i, String[] fileBytes) {
        PeerInfo pi = pf.getFileSource();
        return () -> {
            String dlAnswer = sendMessage(pi, "DL", pf.getFileName(), String.valueOf(this.chunk), String.valueOf(i));

            if (!dlAnswer.isBlank()) {
                String[] rawMessage = dlAnswer.split(" ");

                String source = rawMessage[0];

                int externalClock = Integer.parseInt(rawMessage[1]);
                incrementClock(externalClock);

                addNeighborByAddress(source, externalClock);

                int chunk = Integer.parseInt(rawMessage[4]);
                int indexMsg = Integer.parseInt(rawMessage[5]);
                String base64Encoded = rawMessage[6];

                fileBytes[indexMsg] = base64Encoded;

            }
        };
    }

    public void dlFile(Socket clientSocket, String source, String args) {
        String[] arguments = args.split(" ");
        String fileName = arguments[0];
        String chunk = arguments[1];
        String index = arguments[2];

        PeerFile pf = this.ownFiles.get(fileName);

        byte[] bytes = pf.getBytesByChunk(Integer.parseInt(chunk), Integer.parseInt(index));

        String answer = MessageHelper.createMessage(
                ip,
                port,
                lamportClock.getClock(),
                "FILE",
                fileName,
                String.valueOf(bytes.length),
                index,
                Base64.getEncoder().encodeToString(bytes)
        );

        MessageHandler.handleAnswerMessage(clientSocket, answer, source);
    }

    public void showStatistics() {

        StringBuilder sb = new StringBuilder();
        sb.append(" Tam. chunk | N peers | Tam. arquivo | N | Tempo [s] | Desvio\n");

        stats.forEach((s, ps) -> sb.append(ps.buildMessage()));

        log.log(sb.toString());
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

    private static class Timer {
        private long startTime;

        public void start() {
            startTime = System.nanoTime();
        }

        public long finish() {
            return System.nanoTime() - startTime;
        }
    }
}
