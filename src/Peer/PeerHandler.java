package Peer;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PeerHandler {

    private static final HashMap<String, PeerInfo> neighbors = new HashMap<>();

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
            sb.append(String.format("[%s] %s %s %n", n, i, i.getStatus()));
        });

        return sb.toString();
    }

    public static Collection<PeerInfo> getNeighborsToSend(String source) {
        HashMap<String, PeerInfo> toSend = ((HashMap<String, PeerInfo>) neighbors.clone());
        toSend.remove(source);

        return toSend.values();
    }

    public static synchronized void changePeerStatus(String address){
        if (neighbors.containsKey(address)) {
            PeerInfo pi = neighbors.get(address);
            pi.setStatus("OFFLINE");
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
