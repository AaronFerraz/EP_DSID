package Peer;

import logger.Logger;
import logger.LoggerFactory;

import java.util.Arrays;
import java.util.Base64;

public class PeerFile {

    private static final Logger log = LoggerFactory.getLogger(PeerFile.class);

    private final long fileSize;
    private final String fileName;
    private PeerInfo fileSource;
    private byte[] bytes;

    public PeerFile(String fileName, long fileSize, byte[] bytes) {
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.bytes = bytes;
    }

    public PeerFile(String fileName, long fileSize, PeerInfo fileSource) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileSource = fileSource;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public PeerInfo getFileSource() {
        return fileSource;
    }


    public byte[] getBytesByChunk(int chunk, int index) {
        int start = chunk*index;
        log.logDebug(String.valueOf(start));
        int end = start+chunk;
        if (end > bytes.length) end = bytes.length;

        log.logDebug(String.valueOf(end));

        return Arrays.copyOfRange(bytes, start, end);
    }

    @Override
    public String toString(){
        return getFileName()+":"+getFileSize();
    }
}
