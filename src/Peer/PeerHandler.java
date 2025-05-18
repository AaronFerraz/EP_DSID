package Peer;

import logger.Logger;
import logger.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PeerHandler {

    private static final HashMap<String, PeerInfo> neighbors = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(PeerHandler.class);

    public static Collection<PeerInfo> getNeighborsAsList() {
        return neighbors.values();
    }

    public static synchronized void addNeighbor(String key, PeerInfo value) {
        neighbors.put(key, value);
    }

    public static PeerInfo getNeighbor(String key) {
        return neighbors.get(key);
    }

    public static String buildGetPeersMessage(HashMap<Integer, PeerInfo> tempPeerMap) {
        StringBuilder sb = new StringBuilder();
        AtomicInteger n = new AtomicInteger();

        neighbors.forEach((k,i) -> {
            n.getAndIncrement();
            tempPeerMap.put(n.get(), i);
            sb.append(String.format("[%s] %s %s - %s %n", n, i, i.getStatus(), i.getClock()));
        });

        return sb.toString();
    }

    public static String buildLsListMessage(HashMap<Integer, PeerFile> files) {
        StringBuilder sb = new StringBuilder();
        sb.append("     Nome | Tamanho | Peer %n");
        sb.append(String.format("[%s] %s | %s | %s %n",0,"<Cancelar>"," ", " "));
        files.forEach((k,v) ->
                sb.append(String.format("[%s] %s | %s | %s %n",k,v.getFileName(),v.getFileSize(), v.getFileSource())));

        return sb.toString().trim();
    }

    public static synchronized void changePeerStatus(String address, int clock){
        if (neighbors.containsKey(address)) {
            PeerInfo pi = neighbors.get(address);
            pi.setStatus("OFFLINE");
            if (clock > pi.getClock())
                pi.setClock(clock);
        }
    }

    public static synchronized Boolean writeFileToPath(Path path, String fileName, String base64Encoded) {
        try {
            byte[] fileBytes = Base64.getDecoder().decode(base64Encoded);
            Path outputFile = path.resolve(fileName);
            Files.write(outputFile, fileBytes);

            return true;
        } catch (IOException e) {
            log.logDebug(e.getMessage());

            return false;
        }
    }

    public static synchronized void exit(Peer peer) {
        neighbors.forEach((k, p) -> {
            if (p.getStatus().equals("ONLINE")){
                peer.sendMessage(p, "BYE");
            }
        });
    }
}
