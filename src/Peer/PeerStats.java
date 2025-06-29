package Peer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PeerStats {
    private final String tamChunk;
    private final String nPeers;
    private final String tamArquivo;
    private int nAmostra = 0;
    private double tempoMedio = 0;
    private double desvPadrao = 0;
    private List<Long> total;

    public PeerStats(String tamChunk, String nPeers, String tamArquivo) {
        this.tamChunk = tamChunk;
        this.nPeers = nPeers;
        this.tamArquivo = tamArquivo;
        this.total = new ArrayList<>();
    }

    public void novaAmostra(long tempo) {
        this.nAmostra++;
        this.total.add(tempo);

        calcMedia();
        calcDesvPadrao();

    }

    private void calcMedia() {
        AtomicLong total = new AtomicLong();
        this.total.forEach(total::addAndGet);

        this.tempoMedio = ((double) total.get() / this.nAmostra);
    }
    private void calcDesvPadrao() {
        List<Double> desvs = new ArrayList<>();

        for (Long t : this.total) {
            desvs.add(Math.pow(t - this.tempoMedio, 2));
        }
        double desvsTotais = 0;
        for (Double d : desvs) {
            desvsTotais+=d;
        }
        this.desvPadrao = Math.sqrt(desvsTotais);
    }
    public String buildMessage() {
        return String.format("%s | %s | %s | %s | %.5f | %.5f %n", this.tamChunk, this.nPeers, this.tamArquivo, this.nAmostra, this.tempoMedio / 1_000_000_000.0, this.desvPadrao / 1_000_000_000.0);
    }
}
