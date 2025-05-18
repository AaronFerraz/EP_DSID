package Peer;

import logger.Logger;
import logger.LoggerFactory;

public class PeerInfo {
    private String ip;
    private int port;
    private String status;
    private int clock;

    private static final Logger log = LoggerFactory.getLogger(PeerInfo.class);

    public PeerInfo(String ip, int port, String status, int clock) {
        this.ip = ip;
        this.port = port;
        this.status = status;
        this.clock = clock;

        log.log("Adicionando novo peer %s:%s status %s", ip,port,status);
    }

    @Override
    public String toString() {

        return ip + ":" + port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        log.log("Atualizando peer %s status %s", this, status);
        this.status = status;
    }

    public int getClock() {
        return clock;
    }

    public void setClock(int clock) {
        this.clock = clock;
    }
}
